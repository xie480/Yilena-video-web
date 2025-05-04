package com.yilena.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.yilena.service.constant.*;
import com.yilena.service.dao.*;
import com.yilena.service.entity.po.*;
import com.yilena.service.entity.vo.PostVO;
import com.yilena.service.service.LikeService;
import com.yilena.service.utils.CurrentHolder;
import com.yilena.service.utils.SnowFlake;
import com.yilena.service.webSocket.ChatEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService{

    private final VideoMapper videoMapper;
    private final RabbitTemplate rabbitTemplate;
    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final SnowFlake snowFlake;
    private final ChatEndpoint chatEndpoint;
    private final LikeMapper likeMapper;

    @Transactional
    @Override
    public void doLike(Like like) {
        like.setUserId(CurrentHolder.getCurrent());

        ChatMessage chatMessage = new ChatMessage();

        Long receiveUserId = null;
        String username = userMapper.getUserById(like.getUserId()).getUsername();

        if(Objects.equals(like.getEntityType(), EntityConstant.VIDEO_TYPE)){
            Video video = videoMapper.getVideoById(like.getEntityId());
            if(video == null){
                throw new RuntimeException(StatusConstant.VIDEO_NOT_EXIST);
            }
            receiveUserId = video.getUserId();
            video.setLikes(video.getLikes()+1);
            videoMapper.updateVideo(video);

            chatMessage.setContent("用户" + username + "点赞了你的视频《" + video.getTitle() + "》");
        }
        else if(Objects.equals(like.getEntityType(), EntityConstant.POST_TYPE)){
            Post post = postMapper.getPostById(like.getEntityId());
            if(post == null){
                throw new RuntimeException(StatusConstant.POST_NOT_EXIST);
            }
            receiveUserId = post.getUserId();
            post.setLikes(post.getLikes()+1);
            postMapper.updatePost(post);

            chatMessage.setContent("用户" + username + "点赞了你的动态\n" + post.getContent());

            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(post, postVO);
            postVO.setType(MqConstant.SEND_PUT);
            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.POST_BINDING_KEY, postVO);
        }
        else if(Objects.equals(like.getEntityType(), EntityConstant.COMMENT_TYPE)){
            Comment comment = commentMapper.getCommentById(like.getEntityId());
            if(comment == null){
                throw new RuntimeException(StatusConstant.COMMENT_NOT_EXIST);
            }
            receiveUserId = comment.getUserId();
            comment.setLikes(comment.getLikes()+1);
            commentMapper.updateComment(comment);

            chatMessage.setContent("用户" + username + "点赞了你的评论\n" + comment.getContent());
        }

        like.setId(snowFlake.getID());
        like.setCreatedTime(LocalDateTime.now());
        likeMapper.doLike(like);

        // 发送私信通知收到点赞
        chatMessage.setCreatedTime(LocalDateTime.now());
        chatMessage.setSendUserId(like.getUserId());
        chatMessage.setReceiveUserId(receiveUserId);
        chatMessage.setType(MessageTypeConstant.LIKE);
        chatMessage.setVisibilityBySender(StatusConstant.STATUS_NO);
        chatMessage.setVisibilityByReceiver(StatusConstant.STATUS_YES);
        chatMessage.setId(snowFlake.getID());
        String json = JSON.toJSONString(chatMessage);
        chatEndpoint.sendMessage(json);
    }

    @Transactional
    @Override
    public void undoLike(Like like) {
        like.setUserId(CurrentHolder.getCurrent());
        if(Objects.equals(like.getEntityType(), EntityConstant.VIDEO_TYPE)){
            Video video = videoMapper.getVideoById(like.getEntityId());
            if(video == null){
                throw new RuntimeException(StatusConstant.VIDEO_NOT_EXIST);
            }
            if(video.getLikes() == 0){
                throw new RuntimeException("点赞数不能为负数");
            }
            video.setLikes(video.getLikes()-1);
            videoMapper.updateVideo(video);
        }
        else if(Objects.equals(like.getEntityType(), EntityConstant.POST_TYPE)){
            Post post = postMapper.getPostById(like.getEntityId());
            if(post == null){
                throw new RuntimeException(StatusConstant.POST_NOT_EXIST);
            }
            if(post.getLikes() == 0){
                throw new RuntimeException("点赞数不能为负数");
            }
            post.setLikes(post.getLikes()-1);
            postMapper.updatePost(post);

            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(post, postVO);
            postVO.setType(MqConstant.SEND_PUT);
            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.POST_BINDING_KEY, postVO);
        }
        else if(Objects.equals(like.getEntityType(), EntityConstant.COMMENT_TYPE)){
            Comment comment = commentMapper.getCommentById(like.getEntityId());
            if(comment == null){
                throw new RuntimeException(StatusConstant.COMMENT_NOT_EXIST);
            }
            comment.setLikes(comment.getLikes()-1);
            commentMapper.updateComment(comment);
        }

        likeMapper.undoLike(like);
    }

    @Override
    public Like isLike(Like like) {
        Like oldLike =  likeMapper.isLike(like);
        // 检测是否是之前视频的点赞
//        if(oldLike == null){
//            if(like.getEntityType().equals(EntityConstant.VIDEO_TYPE)){
//                Video video = videoMapper.getVideoById(like.getEntityId());
//                if(video != null){
//                    Long oldId = video.getLastId();
//                    oldLike =  likeMapper.isLike(like);
//                    // 如果存在点赞记录
//                    if(oldLike != null){
//                        // 删掉原来的记录
//                        Like dealLike = new Like();
//                        dealLike.setEntityId(oldId);
//                        dealLike.setEntityType(like.getEntityType());
//                        dealLike.setUserId(like.getUserId());
//                        likeMapper.undoLike(dealLike);
//
//                        // 增加新的记录
//                        dealLike.setId(snowFlake.getID());
//                        dealLike.setEntityId(video.getId());
//                        dealLike.setCreatedTime(oldLike.getCreatedTime());
//                        likeMapper.doLike(dealLike);
//                    }
//                }
//            }
//        }
        return oldLike;
    }
}
