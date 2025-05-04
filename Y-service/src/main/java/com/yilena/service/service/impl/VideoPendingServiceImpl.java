package com.yilena.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yilena.service.constant.MessageTypeConstant;
import com.yilena.service.constant.MqConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.*;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.VideoPendingDTO;
import com.yilena.service.entity.dto.VideoPendingStatusDTO;
import com.yilena.service.entity.po.*;
import com.yilena.service.entity.vo.VideoCollectionVO;
import com.yilena.service.entity.vo.VideoVO;
import com.yilena.service.utils.SnowFlake;
import com.yilena.service.utils.VideoDurationUtils;
import com.yilena.service.webSocket.ChatEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.yilena.service.service.VideoPendingService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VideoPendingServiceImpl implements VideoPendingService{

    private final VideoPendingMapper videoPendingMapper;
    private final RabbitTemplate rabbitTemplate;
    private final VideoMapper videoMapper;
    private final ChatEndpoint chatEndpoint;
    private final SnowFlake snowFlake;
    private final VideoCollectionMapper videoCollectionMapper;
    private final UserMapper userMapper;
    private final FollowMapper followMapper;

    @Override
    public PageResult<VideoPending> getVideoPendingByPage(VideoPendingDTO videoPendingDTO) {
        PageHelper.startPage(videoPendingDTO.getPage(),videoPendingDTO.getPageSize());
        List<VideoPending> videoPendings = videoPendingMapper.getVideoPendingByPage(videoPendingDTO);
        videoPendings.forEach(videoPending -> {
            String tgsJson = videoPending.getTagsJson();
            List<String> tags = JSON.parseArray(tgsJson, String.class);
            videoPending.setTags(tags);
        });
        Page<VideoPending> p = (Page<VideoPending>)videoPendings;
        return new PageResult<>(p.getTotal(),p.getResult());
    }

    @Override
    public VideoPending getVideoPendingById(Long id) {
        VideoPending videoPending = videoPendingMapper.getVideoPendingById(id);
        String tgsJson = videoPending.getTagsJson();
        List<String> tags = JSON.parseArray(tgsJson, String.class);
        videoPending.setTags(tags);

        VideoCollection videoCollection = videoCollectionMapper.getVideoCollectionById(videoPending.getCollectionId());
        videoPending.setCollectionName(videoCollection.getTitle());
        return videoPending;
    }

    @Transactional
    @Override
    public void updateVideoPendingStatus(VideoPendingStatusDTO videoPendingStatusDTO) {
        VideoPending videoPending = videoPendingMapper.getVideoPendingById(videoPendingStatusDTO.getId());
        videoPending.setVisibility(videoPendingStatusDTO.getStatus());
        if(videoPendingStatusDTO.getReason() != null){
            videoPending.setReason(videoPendingStatusDTO.getReason());
        }
        videoPendingMapper.updateVideoPending(videoPending);

        if(Objects.equals(videoPending.getVisibility(), StatusConstant.STATUS_YES)) {
            // 添加到视频表
            Video video = new Video();
            BeanUtils.copyProperties(videoPending, video);

            User user = userMapper.getUserById(videoPending.getUserId());
            user.setVideoCount(user.getVideoCount() + 1);
            userMapper.updateUserById(user);

            if(videoPending.getLastId() != null) {
                // 保持新旧id一致
                video.setId(videoPending.getLastId());
            }else{
                video.setId(snowFlake.getID());
            }
            videoMapper.addVideo(video);

            // 删除待审核表
            videoPendingMapper.deleteVideoPending(videoPending.getId());

            // 发送消息添加到es索引库
            VideoVO videoVO = new VideoVO();
            videoVO.setCreatedTime(video.getCreatedTime());
            BeanUtils.copyProperties(video, videoVO);
            videoVO.setType(MqConstant.SEND_ADD);
            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.VIDEO_BINDING_KEY, videoVO);

            // 发送消息添加到投稿合集
            VideoCollectionVO videoCollectionVO = new VideoCollectionVO();
            videoCollectionVO.setId(video.getCollectionId());
            videoCollectionVO.setVideoIdsJson("[" + video.getId().toString() + "]");
            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.COLLECTION_BINDING_KEY, videoCollectionVO);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(MessageTypeConstant.VIDEO);
            String videoJson = JSON.toJSONString(video);
            chatMessage.setContent(videoJson);
            chatMessage.setVisibilityBySender(StatusConstant.STATUS_YES);
            chatMessage.setVisibilityByReceiver(StatusConstant.STATUS_YES);
            chatMessage.setSendUserId(video.getUserId());
            chatMessage.setCreatedTime(LocalDateTime.now());
            chatMessage.setStatus(StatusConstant.STATUS_YES);

            List<Long> receiveUserIds = followMapper.getFollowedIds(video.getUserId());
            chatMessage.setReceiveUserIds(receiveUserIds);

            String json = JSON.toJSONString(chatMessage);
            chatEndpoint.sendMessage(json);
        }else if(Objects.equals(videoPending.getVisibility(), StatusConstant.STATUS_NO)){

            // 发送私信通知
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setCreatedTime(LocalDateTime.now());
            // 官方默认发送人id为0
            chatMessage.setSendUserId(0L);
            chatMessage.setReceiveUserId(videoPending.getUserId());
            chatMessage.setType(MessageTypeConstant.MANAGER);
            chatMessage.setVisibilityBySender(StatusConstant.STATUS_YES);
            chatMessage.setVisibilityByReceiver(StatusConstant.STATUS_YES);
            chatMessage.setContent("您的视频《"+videoPending.getTitle()+"》未通过审核，" +
                    "请修改后再次提交。\n" + "原因：" + videoPendingStatusDTO.getReason());
            chatMessage.setId(snowFlake.getID());
            String json = JSON.toJSONString(chatMessage);
            chatEndpoint.sendMessage(json);
        }
    }

    @Transactional
    @Override
    public void updateVideoPendingFromVideo(VideoPendingStatusDTO videoPendingStatusDTO) {
        Video video = videoMapper.getVideoById(videoPendingStatusDTO.getId());
        video.setVisibility(videoPendingStatusDTO.getStatus());
        videoMapper.updateVideo(video);

        // 发送私信通知
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setCreatedTime(LocalDateTime.now());
        // 官方默认发送人id为0
        chatMessage.setSendUserId(0L);
        chatMessage.setReceiveUserId(video.getUserId());
        chatMessage.setType(MessageTypeConstant.MANAGER);
        chatMessage.setVisibilityBySender(StatusConstant.STATUS_YES);
        chatMessage.setVisibilityByReceiver(StatusConstant.STATUS_YES);
        chatMessage.setContent("您的视频《"+video.getTitle()+"》因违规而被下架，" +
                "请修改后再次提交。\n" + "原因：" + videoPendingStatusDTO.getReason());
        chatMessage.setId(snowFlake.getID());
        String json = JSON.toJSONString(chatMessage);
        chatEndpoint.sendMessage(json);
    }

    @Override
    public List<VideoPending> getVideoPendingByStatus(Long userId, Integer status) {
        return videoPendingMapper.getVideoPendingByStatus(userId, status);
    }

    @Override
    public void updateVideoPending(VideoPending videoPending) {
        if(videoPending.getVideoUrl() != null){
            // 获取视频时长
            long time = VideoDurationUtils.getVideoDuration(videoPending.getVideoUrl());
            videoPending.setTime(time);
        }
        videoPendingMapper.updateVideoPending(videoPending);
    }

    @Override
    public void deleteVideoPending(Long id) {
        videoPendingMapper.deleteVideoPending(id);
    }

    @Override
    public void reuploadVideoPending(Long id) {
        VideoPending videoPending = videoPendingMapper.getVideoPendingById(id);
        videoPending.setStatus(StatusConstant.STATUS_WAIT);
        videoPendingMapper.updateVideoPending(videoPending);
    }
}
