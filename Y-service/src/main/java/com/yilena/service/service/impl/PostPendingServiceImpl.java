package com.yilena.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.yilena.service.constant.MessageTypeConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.PostMapper;
import com.yilena.service.entity.dto.PostPendingDTO;
import com.yilena.service.entity.po.ChatMessage;
import com.yilena.service.entity.po.Post;
import com.yilena.service.service.PostPendingService;
import com.yilena.service.utils.SnowFlake;
import com.yilena.service.webSocket.ChatEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostPendingServiceImpl implements PostPendingService {
    private final PostMapper postMapper;
    private final ChatEndpoint chatEndpoint;
    private final SnowFlake snowFlake;

    @Transactional
    @Override
    public void updatePostPendingStatus(PostPendingDTO postPendingDTO) {
        Post post = postMapper.getPostById(postPendingDTO.getId());
        if (post == null) {
            throw new RuntimeException(StatusConstant.POST_NOT_EXIST);
        }
        post.setVisibility(StatusConstant.STATUS_NO);
        postMapper.updatePost(post);

        // 发送私信通知
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setCreatedTime(LocalDateTime.now());
        // 官方默认发送人id为0
        chatMessage.setSendUserId(0L);
        chatMessage.setReceiveUserId(post.getUserId());
        chatMessage.setType(MessageTypeConstant.MANAGER);
        chatMessage.setVisibilityBySender(StatusConstant.STATUS_YES);
        chatMessage.setVisibilityByReceiver(StatusConstant.STATUS_YES);
        chatMessage.setContent("您的动态《"+post.getTitle()+"》因违规而被下架，" +
                "请修改后再发布。\n" + "原因：" + postPendingDTO.getReason());
        chatMessage.setId(snowFlake.getID());
        String json = JSON.toJSONString(chatMessage);
        chatEndpoint.sendMessage(json);
    }
}
