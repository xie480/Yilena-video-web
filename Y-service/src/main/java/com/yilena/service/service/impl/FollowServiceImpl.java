package com.yilena.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yilena.service.constant.*;
import com.yilena.service.dao.FollowMapper;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.FollowDTO;
import com.yilena.service.entity.dto.FollowPageDTO;
import com.yilena.service.entity.po.ChatMessage;
import com.yilena.service.entity.po.UserFollowing;
import com.yilena.service.entity.vo.UserVO;
import com.yilena.service.service.FollowService;
import com.yilena.service.utils.SnowFlake;
import com.yilena.service.webSocket.ChatEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final RabbitTemplate rabbitTemplate;
    private final UserMapper userMapper;
    private final SnowFlake snowFlake;
    private final ChatEndpoint chatEndpoint;

    @Transactional
    @Override
    public void follow(FollowDTO followDTO) {
        if(Objects.equals(followDTO.getFollowedId(), followDTO.getFollowerId())){
            throw new RuntimeException(FollowConstant.FOLLOW_SAME_USER_FAIL);
        }
        UserFollowing userFollowing = new UserFollowing();
        BeanUtils.copyProperties(followDTO, userFollowing);
        // 判断是否为特别关注
        if(Objects.equals(followDTO.getIsSpecial(), FollowConstant.NotSpecial)){
            log.info("用户:{}关注用户:{}", followDTO.getFollowerId(), followDTO.getFollowedId());
            if(followMapper.getById(followDTO.getFollowerId(), followDTO.getFollowedId()) != null){
                throw new RuntimeException(FollowConstant.FOLLOW_ADD_FAIL);
            }
            userFollowing.setUpdatedTime(LocalDateTime.now());
            userFollowing.setCreatedTime(LocalDateTime.now());
            userFollowing.setId(snowFlake.getID());
            followMapper.addFollow(userFollowing);

            userFollowing.setType(MqConstant.SEND_ADD);
            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.FOLLOW_BINDING_KEY, userFollowing);
        }
        else if(Objects.equals(followDTO.getIsSpecial(), FollowConstant.Special)){
            log.info("用户:{}特别关注用户:{}", followDTO.getFollowerId(), followDTO.getFollowedId());
            if(followMapper.getById(followDTO.getFollowerId(), followDTO.getFollowedId()) == null){
                throw new RuntimeException(FollowConstant.FOLLOW_DELETE_FAIL);
            }
            userFollowing.setUpdatedTime(LocalDateTime.now());
            followMapper.updateSpecialFollow(userFollowing);
        }

        String username = userMapper.getUserById(followDTO.getFollowerId()).getUsername();

        // 发送私信通知受到关注
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setCreatedTime(LocalDateTime.now());
        chatMessage.setSendUserId(followDTO.getFollowerId());
        chatMessage.setReceiveUserId(followDTO.getFollowedId());
        chatMessage.setType(MessageTypeConstant.FOLLOW);
        chatMessage.setVisibilityBySender(StatusConstant.STATUS_NO);
        chatMessage.setVisibilityByReceiver(StatusConstant.STATUS_YES);
        chatMessage.setContent("用户" + username + "关注了你");
        chatMessage.setId(snowFlake.getID());
        String json = JSON.toJSONString(chatMessage);
        chatEndpoint.sendMessage(json);
    }

    @Transactional
    @Override
    public void unfollow(FollowDTO followDTO) {
        if(Objects.equals(followDTO.getFollowedId(), followDTO.getFollowerId())){
            throw new RuntimeException(FollowConstant.FOLLOW_SAME_USER_FAIL);
        }
        UserFollowing userFollowing = new UserFollowing();
        BeanUtils.copyProperties(followDTO, userFollowing);
        // 判断是否为特别关注
        if(Objects.equals(followDTO.getIsSpecial(), FollowConstant.NotSpecial)){
            log.info("用户:{}取消关注用户:{}", followDTO.getFollowerId(), followDTO.getFollowedId());
            if(followMapper.getById(followDTO.getFollowerId(), followDTO.getFollowedId()) == null){
                throw new RuntimeException(FollowConstant.FOLLOW_DELETE_FAIL);
            }

            userFollowing.setUpdatedTime(LocalDateTime.now());
            followMapper.deleteFollow(userFollowing);

            userFollowing.setType(MqConstant.SEND_DELETE);
            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.FOLLOW_BINDING_KEY, userFollowing);
        }
        else if(Objects.equals(followDTO.getIsSpecial(), FollowConstant.Special)){
            log.info("用户:{}取消特别关注用户:{}", followDTO.getFollowerId(), followDTO.getFollowedId());
            userFollowing.setUpdatedTime(LocalDateTime.now());
            userFollowing.setIsSpecial(FollowConstant.NotSpecial);
            followMapper.updateSpecialFollow(userFollowing);
        }
    }

    @Override
    public PageResult<UserVO> getFollowByPage(FollowPageDTO followPageDTO) {
        List<Long> followingIds = followMapper.getFollowingIds(followPageDTO.getId());
        if(followingIds.isEmpty()){
            return new PageResult<>(0L, null);
        }
        PageHelper.startPage(followPageDTO.getPage(), followPageDTO.getPageSize());
        List<UserVO> users = userMapper.getUserByIds(followingIds,followPageDTO.getUsername());
        Page<UserVO> p = (Page<UserVO>) users;
        return new PageResult<>(p.getTotal(), p.getResult());
    }

    @Override
    public UserFollowing isFollow(Long userId, Long authorId) {
        return followMapper.isFollow(userId, authorId);
    }
}
