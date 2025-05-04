package com.yilena.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.yilena.service.constant.*;
import com.yilena.service.dao.CommentMapper;
import com.yilena.service.dao.PostMapper;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.dao.VideoMapper;
import com.yilena.service.entity.dto.CommentQueryDTO;
import com.yilena.service.entity.po.*;
import com.yilena.service.entity.vo.CommentVO;
import com.yilena.service.entity.vo.PostVO;
import com.yilena.service.service.CommentService;
import com.yilena.service.utils.CurrentHolder;
import com.yilena.service.utils.SnowFlake;
import com.yilena.service.webSocket.ChatEndpoint;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final VideoMapper videoMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final RabbitTemplate rabbitTemplate;
    private final SnowFlake snowFlake;
    private final ChatEndpoint chatEndpoint;

    @Transactional
    @Override
    public void addComment(Comment comment) {
        comment.setId(snowFlake.getID());
        comment.setUserId(CurrentHolder.getCurrent());
        comment.setUpdatedTime(LocalDateTime.now());
        comment.setCreatedTime(LocalDateTime.now());
        comment.setComments(0);
        comment.setLikes(0);
        if(comment.getImageUrl().isEmpty()){
            comment.setImageUrl(null);
        }

        commentMapper.addComment(comment);

        String username = userMapper.getUserById(comment.getUserId()).getUsername();

        Long receiveUserId =  null;
        String title = null;
        // 更改实体评论数
        if(Objects.equals(comment.getEntityType(), EntityConstant.VIDEO_TYPE)){
            Video video = videoMapper.getVideoById(comment.getEntityId());
            if(video == null){
                throw new RuntimeException(StatusConstant.VIDEO_NOT_EXIST);
            }
            receiveUserId = video.getUserId();
            title = video.getTitle();
            video.setComments(video.getComments() + 1);
            videoMapper.updateVideo(video);

            // 发送私信通知收到评论
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setCreatedTime(LocalDateTime.now());
            chatMessage.setSendUserId(comment.getUserId());
            chatMessage.setReceiveUserId(receiveUserId);
            chatMessage.setType(MessageTypeConstant.COMMENT);
            chatMessage.setVisibilityBySender(StatusConstant.STATUS_NO);
            chatMessage.setVisibilityByReceiver(StatusConstant.STATUS_YES);
            chatMessage.setContent("用户" + username + "回复了你的视频《" +
                    title + "》\n" + comment.getContent());
            chatMessage.setId(snowFlake.getID());
            String json = JSON.toJSONString(chatMessage);
            chatEndpoint.sendMessage(json);
        }
        else if(Objects.equals(comment.getEntityType(),EntityConstant.POST_TYPE)){
            Post post = postMapper.getPostById(comment.getEntityId());
            if(post == null){
                throw new RuntimeException(StatusConstant.POST_NOT_EXIST);
            }
            receiveUserId = post.getUserId();
            title = post.getTitle();
            post.setComments(post.getComments() + 1);
            postMapper.updatePost(post);

            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(post,postVO);
            postVO.setType(MqConstant.SEND_PUT);
            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE,MqConstant.POST_BINDING_KEY,postVO);

            // 发送私信通知收到评论
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setCreatedTime(LocalDateTime.now());
            chatMessage.setSendUserId(comment.getUserId());
            chatMessage.setReceiveUserId(receiveUserId);
            chatMessage.setType(MessageTypeConstant.COMMENT);
            chatMessage.setVisibilityBySender(StatusConstant.STATUS_NO);
            chatMessage.setVisibilityByReceiver(StatusConstant.STATUS_YES);
            chatMessage.setContent("用户" + username + "回复了你的动态" + "\n" + comment.getContent());
            chatMessage.setId(snowFlake.getID());
            String json = JSON.toJSONString(chatMessage);
            chatEndpoint.sendMessage(json);
        }
        else if(Objects.equals(comment.getEntityType(),EntityConstant.COMMENT_TYPE)){
            Comment comment1 = commentMapper.getCommentById(comment.getEntityId());
            if(comment1 == null){
                throw new RuntimeException(StatusConstant.COMMENT_NOT_EXIST);
            }
            receiveUserId = comment1.getUserId();
            comment1.setComments(comment1.getComments() + 1);
            // 评论直接修改数据库即可
            commentMapper.updateComment(comment1);

            // 发送私信通知收到评论
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setCreatedTime(LocalDateTime.now());
            chatMessage.setSendUserId(comment.getUserId());
            chatMessage.setReceiveUserId(receiveUserId);
            chatMessage.setType(MessageTypeConstant.COMMENT);
            chatMessage.setVisibilityBySender(StatusConstant.STATUS_NO);
            chatMessage.setVisibilityByReceiver(StatusConstant.STATUS_YES);
            chatMessage.setContent("用户" + username + "回复了你的评论\n" + comment.getContent());
            chatMessage.setId(snowFlake.getID());
            String json = JSON.toJSONString(chatMessage);
            chatEndpoint.sendMessage(json);
        }

        //检测评论里面是否有@
        if(comment.getContent().contains("@")){
            String[] split = comment.getContent().split("@");
            String AiUsername = split[1].split(" ")[0];
            User user = userMapper.getByUsername(AiUsername);
            if(user == null){
                throw new RuntimeException(UserConstant.USER_LOGIN_INEXIST_FAIL);
            }
            receiveUserId = user.getId();

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setCreatedTime(LocalDateTime.now());
            chatMessage.setSendUserId(comment.getUserId());
            chatMessage.setReceiveUserId(receiveUserId);
            chatMessage.setType(MessageTypeConstant.At);
            chatMessage.setVisibilityBySender(StatusConstant.STATUS_NO);
            chatMessage.setVisibilityByReceiver(StatusConstant.STATUS_YES);
            chatMessage.setContent("用户" + username + "@了你");
            chatMessage.setId(snowFlake.getID());
            String json = JSON.toJSONString(chatMessage);
            chatEndpoint.sendMessage(json);
        }
    }

    @Override
    public List<CommentVO> getCommentByList(Comment comment) {
        List<CommentVO>commentVOS = null;
        if(comment.getType().equals(EntityConstant.HOT_TYPE)){
            commentVOS = commentMapper.getCommentByHot(comment);
        }else if(comment.getType().equals(EntityConstant.NEW_TYPE)){
            commentVOS = commentMapper.getCommentByNew(comment);
        }
        if(commentVOS == null){
            return null;
        }
        for (CommentVO commentVO : commentVOS){
            Long userId = commentVO.getUserId();
            User user = userMapper.getUserById(userId);
            commentVO.setImage(user.getImage());
            commentVO.setUsername(user.getUsername());
        }
        return commentVOS;
    }

    @Override
    public void updateComment(Comment comment) {
        Comment oldComment = commentMapper.getCommentById(comment.getId());
        if(oldComment == null){
            throw new RuntimeException(StatusConstant.COMMENT_NOT_EXIST);
        }
        oldComment.setContent(comment.getContent());
        oldComment.setImageUrl(comment.getImageUrl());
        oldComment.setUpdatedTime(LocalDateTime.now());
        commentMapper.updateComment(oldComment);
    }

    @Transactional
    @Override
    public void deleteComment(List<Long> ids) {
        List<Comment> comments = commentMapper.getCommentByIds(ids);

        comments.forEach(comment -> {Video video = videoMapper.getVideoById(comment.getEntityId());
            if(video == null){
                throw new RuntimeException(StatusConstant.VIDEO_NOT_EXIST);
            }
            video.setComments(video.getComments() - 1);
            videoMapper.updateVideo(video);}
        );

        commentMapper.deleteCommentByIds(ids);
    }

    @Transactional
    @Override
    public List<Comment> getCommentQuery(CommentQueryDTO commentQueryDTO) {
        Long currentUserId = CurrentHolder.getCurrent();
        List<Long> videoIds = videoMapper.getVideoIdByUserId(currentUserId,commentQueryDTO.getVideoTitle());
        // 如果没有结果，则返回空列表
        if(videoIds == null){
            return List.of();
        }

        List<Comment> comments = null;
        if(commentQueryDTO.getSortType().equals(EntityConstant.COMMENT_NEW_TYPE)){
            comments =  commentMapper.getCommentsQueryByNewSortType(videoIds);
        }else if(commentQueryDTO.getSortType().equals(EntityConstant.COMMENT_LIKE_TYPE)){
            comments =  commentMapper.getCommentsQueryByLikeSortType(videoIds);
        }else if(commentQueryDTO.getSortType().equals(EntityConstant.COMMENT_MOST_COMMENTS_TYPE)){
            comments =  commentMapper.getCommentsQueryByMostCommentsSortType(videoIds);
        }

        // 如果没有结果，则返回空列表
        if(comments == null){
            return List.of();
        }
        comments.forEach(comment -> {
            Video video = videoMapper.getVideoById(comment.getEntityId());
            if(video != null){
                comment.setVideoTitle(video.getTitle());
            }
            User user = userMapper.getUserById(comment.getUserId());
            if(user != null){
                comment.setUsername(user.getUsername());
            }
        });
        return comments;
    }
}
