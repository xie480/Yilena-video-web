package com.yilena.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.yilena.service.constant.DelMsgConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.ChatMessageMapper;
import com.yilena.service.dao.FollowMapper;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.entity.dto.ReadRequestDTO;
import com.yilena.service.entity.po.ChatMessage;
import com.yilena.service.entity.po.User;
import com.yilena.service.entity.vo.MessageShowVO;
import com.yilena.service.entity.vo.UserVO;
import com.yilena.service.service.ChatService;
import com.yilena.service.utils.CurrentHolder;
import com.yilena.service.utils.SnowFlake;
import com.yilena.service.webSocket.ChatEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatEndpoint chatEndpoint;
    private final SnowFlake snowFlake;
    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    @Transactional
    public List<ChatMessage> getHistoryMessage(Long currentUserId, Long targetUserId) {
        // 1. 从数据库获取历史消息
        List<ChatMessage> dbMessages = chatMessageMapper.getMessageByPair(currentUserId, targetUserId);

        // 2. 从Redis获取该对话的离线消息
        String redisKey = "offline:" + currentUserId + ":" + targetUserId;
        List<Object> redisMessages = redisTemplate.opsForList().range(redisKey, 0, -1);

        // 3. 转换并合并消息
        List<ChatMessage> allMessages = mergeMessages(dbMessages, redisMessages);

//         4. 标记这些消息为已读(错误，不该在此处标记已读)
//        chatMessageMapper.markMessagesAsRead(currentUserId, targetUserId);

        return allMessages;
    }

    @Override
    public void markMessagesAsRead(ReadRequestDTO requestDTO) {
        // 更新所有消息状态为已读
        chatMessageMapper.markMessagesAsRead(requestDTO.getCurrentUserId(), requestDTO.getTargetUserId());

        // 给对方发送消息表示自己已读
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setStatus(StatusConstant.STATUS_WAIT);
        chatMessage.setContent("已读");
        chatMessage.setReceiveUserId(requestDTO.getTargetUserId());
        chatMessage.setSendUserId(requestDTO.getCurrentUserId());

        String json = JSON.toJSONString(chatMessage);
        chatEndpoint.sendMessage(json);

        String redisKey = "offline:" + requestDTO.getCurrentUserId() + ":" + requestDTO.getTargetUserId();
        // 清理Redis中的离线消息
        redisTemplate.execute(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.watch(redisKey);
                operations.multi();
                operations.delete(redisKey);
                List<Object> results = operations.exec();
                if(results != null && !results.isEmpty()){
                    chatMessageMapper.updateExpiredStatus(requestDTO.getCurrentUserId(), requestDTO.getTargetUserId());
                }
                return results;
            }
        });
    }

    @Override
    public void sendMessage(ChatMessage chatMessage) {
        chatMessage.setStatus(StatusConstant.STATUS_NO);
        chatMessage.setCreatedTime(LocalDateTime.now());
        chatMessage.setVisibilityBySender(StatusConstant.STATUS_YES);
        chatMessage.setVisibilityByReceiver(StatusConstant.STATUS_YES);
        chatMessage.setIsExpired(StatusConstant.STATUS_NO);

        // 生成唯一ID
        chatMessage.setId(snowFlake.getID());
        String json = JSON.toJSONString(chatMessage);
        chatEndpoint.sendMessage(json);
    }

    @Override
    public void deleteMessage(Long messageId, Integer type) {
        ChatMessage message = chatMessageMapper.getMessageById(messageId);
        // 删除消息
        if(type.equals(DelMsgConstant.DELETE)){
            if(Objects.equals(message.getSendUserId(), CurrentHolder.getCurrent())){
                message.setVisibilityBySender(StatusConstant.STATUS_NO);
            }else{
                message.setVisibilityByReceiver(StatusConstant.STATUS_NO);
            }
            chatMessageMapper.updateMessageById(message);
        }
        // 撤回消息
        else if(type.equals(DelMsgConstant.WITHDRAW)){
            if(Objects.equals(message.getSendUserId(), CurrentHolder.getCurrent())){
                // 如果发送时间小于两分钟
                LocalDateTime validTime = LocalDateTime.now().minusMinutes(2);
                if(message.getCreatedTime().isAfter(validTime)){
                    //删除redis
                    String messageJson = JSON.toJSONString(message);
                    redisTemplate.opsForList().remove("offline:" + message.getSendUserId()
                            + ":" + message.getReceiveUserId(), 0, messageJson);

                    chatMessageMapper.deleteMessage(messageId);

                    // 给对方发送消息表示自己已经撤回
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.setStatus(StatusConstant.STATUS_WAIT);
                    chatMessage.setContent("撤回");
                    chatMessage.setReceiveUserId(message.getReceiveUserId());
                    chatMessage.setSendUserId(message.getSendUserId());

                    String json = JSON.toJSONString(chatMessage);
                    chatEndpoint.sendMessage(json);
                }else{
                    throw new RuntimeException("发送时间大于两分钟，无法撤回");
                }
            }
        }
    }

    @Override
    public List<UserVO> getUserList(Long userId) {
        List<UserVO> userVOS = followMapper.getFollowingUsers(userId);
        if(userVOS.isEmpty()){
            return List.of();
        }

        // 获取未读信息个数
        userVOS.forEach(userVO -> {
            String redisKey = "offline:" + userId + ":" + userVO.getId();
            Long count = redisTemplate.opsForList().size(redisKey);
            Long dbCount = chatMessageMapper.getMessageWhichUnReadAndIsExpired(userId, userVO.getId());
            // 避免空指针
            long totalUnread = (count != null ? count : 0L) + (dbCount != null ? dbCount : 0L);
            userVO.setUnReadMessageCount(totalUnread);
        });
        return userVOS;
    }

    @Override
    public List<MessageShowVO> getAnswerMe(Long userId) {
        List<MessageShowVO> messageShowVOS = chatMessageMapper.getAnswerMe(userId);
        if(!messageShowVOS.isEmpty()) {
            messageShowVOS.forEach(message -> {
                Long sendUserId = message.getSendUserId();
                User user = userMapper.getUserById(sendUserId);
                message.setUserId(sendUserId);
                message.setUsername(user.getUsername());
                message.setImage(user.getImage());
            });
        }
        return messageShowVOS;
    }

    @Override
    public List<MessageShowVO> getAtMe(Long userId) {
        List<MessageShowVO> messageShowVOS = chatMessageMapper.getAtMe(userId);
        if(!messageShowVOS.isEmpty()) {
            messageShowVOS.forEach(message -> {
                Long sendUserId = message.getSendUserId();
                User user = userMapper.getUserById(sendUserId);
                message.setUserId(sendUserId);
                message.setUsername(user.getUsername());
                message.setImage(user.getImage());
            });
        }
        return messageShowVOS;
    }

    @Override
    public List<MessageShowVO> getLikeMe(Long userId) {
        List<MessageShowVO> messageShowVOS = chatMessageMapper.getLikeMe(userId);
        if(!messageShowVOS.isEmpty()) {
            messageShowVOS.forEach(message -> {
                Long sendUserId = message.getSendUserId();
                User user = userMapper.getUserById(sendUserId);
                message.setUserId(sendUserId);
                message.setUsername(user.getUsername());
                message.setImage(user.getImage());
            });
        }
        return messageShowVOS;
    }

    @Override
    public List<MessageShowVO> getSystemMessage(Long userId) {
        List<MessageShowVO> messageShowVOS = chatMessageMapper.getSystemMessage(userId);
        return messageShowVOS;
    }

    @Override
    public List<MessageShowVO> getOtherMessage(Long userId) {
        List<MessageShowVO> messageShowVOS = chatMessageMapper.getOtherMessage(userId);
        if(!messageShowVOS.isEmpty()) {
            messageShowVOS.forEach(message -> {
                Long sendUserId = message.getSendUserId();
                User user = userMapper.getUserById(sendUserId);
                message.setUserId(sendUserId);
                message.setUsername(user.getUsername());
                message.setImage(user.getImage());
            });
        }
        return messageShowVOS;
    }

    public List<ChatMessage> mergeMessages(List<ChatMessage> dbMessages, List<Object> redisMessages){
        // 使用LinkedHashMap保持插入顺序
        Map<Long, ChatMessage> messageMap = new LinkedHashMap<>();

        // 数据库消息去重（按ID）
        dbMessages.forEach(msg -> messageMap.put(msg.getId(), msg));

        // Redis离线消息去重（需确保离线消息生成唯一ID）
        redisMessages.forEach(obj -> {
            ChatMessage msg = JSON.parseObject((String)obj, ChatMessage.class);
            messageMap.putIfAbsent(msg.getId(), msg);
        });

        // 遍历并过滤掉单方面删除掉的消息
        Iterator<Map.Entry<Long, ChatMessage>> iterator = messageMap.entrySet().iterator();
        while (iterator.hasNext()) {
            ChatMessage msg = iterator.next().getValue();
            boolean shouldRemove = false;
            // 判断发送方是否删除
            if (msg.getSendUserId().equals(CurrentHolder.getCurrent())) {
                shouldRemove = msg.getVisibilityBySender().equals(StatusConstant.STATUS_NO);
            }
            else if(msg.getReceiveUserId().equals(CurrentHolder.getCurrent())) {
                shouldRemove = msg.getVisibilityByReceiver().equals(StatusConstant.STATUS_NO);
            }

            if (shouldRemove) {
                iterator.remove();
            }
        }

        return new ArrayList<>(messageMap.values())
                .stream()
                .sorted((o1, o2) -> o2.getCreatedTime().compareTo(o1.getCreatedTime()))
                .collect(Collectors.toList());
    }
}
