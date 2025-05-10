package com.yilena.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.yilena.service.constant.*;
import com.yilena.service.dao.FollowMapper;
import com.yilena.service.dao.PostMapper;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.dao.VideoMapper;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.PostDTO;
import com.yilena.service.entity.po.History;
import com.yilena.service.entity.po.Post;
import com.yilena.service.entity.po.User;
import com.yilena.service.entity.po.Video;
import com.yilena.service.entity.vo.PostVO;
import com.yilena.service.es.PostES;
import com.yilena.service.service.PostService;
import com.yilena.service.utils.CurrentHolder;
import com.yilena.service.utils.SensitiveWordFilter;
import com.yilena.service.utils.SnowFlake;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final RabbitTemplate rabbitTemplate;
    private final PostES postES;
    private final VideoMapper videoMapper;
    private final SnowFlake snowFlake;
    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public void addPost(Post post) {
        post.setClicks(0);
        post.setLikes(0);
        post.setFavorites(0);
        post.setShares(0);
        post.setComments(0);
        post.setCreatedTime(LocalDateTime.now());
        post.setUpdatedTime(LocalDateTime.now());
        post.setId(snowFlake.getID());

        post.setTitle(SensitiveWordFilter.filterSensitiveWords(post.getTitle()));
        post.setContent(SensitiveWordFilter.filterSensitiveWords(post.getContent()));

        // 不需要后台审核
        post.setVisibility(StatusConstant.STATUS_YES);

        // list转json
        String imageUrlJson = JSON.toJSONString(post.getImageUrl());
        post.setImageUrlJson(imageUrlJson);
        String tagsJson = JSON.toJSONString(post.getTags());
        post.setTagsJson(tagsJson);

        // 判断是文字还是转发视频动态
        if(post.getVideoId() == null){
            post.setPostType(PostConstant.Text_Post);

            // 更新视频转发数
            Video video = videoMapper.getVideoById(post.getVideoId());
            if(video != null){
                video.setShares(video.getShares() + 1);
                videoMapper.updateVideo(video);
            }
        }
        else {
            post.setPostType(PostConstant.Video_Post);
        }

        // 更新用户动态数
        User user = userMapper.getUserById(post.getUserId());
        if(user != null){
            Integer postCount = user.getPostCount();
            user.setPostCount(postCount + 1);
            userMapper.updateUserById(user);
        }

        postMapper.addPost(post);

        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        postVO.setType(MqConstant.SEND_ADD);
        rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.POST_BINDING_KEY, postVO);
    }

    @Override
    public PageResult<PostVO> getPostByPage(PostDTO postDTO) {
        return postES.getPostByPage(postDTO);
    }

    @Transactional
    @Override
    public Post getPostById(Long id) {

        Post p = postMapper.getPostById(id);
        String imageUrlJson = p.getImageUrlJson();
        String tagsJson = p.getTagsJson();
        if(imageUrlJson != null) {
            p.setImageUrl(JSON.parseArray(imageUrlJson, String.class));
        }
        p.setTags(JSON.parseArray(tagsJson, String.class));

        //原本以为是动态和专栏是一类东西，后来才发现不是，动态是不需要以下业务逻辑的
//        if(p.getVisibility().equals(StatusConstant.STATUS_YES)){
//            if(!p.getUserId().equals(CurrentHolder.getCurrent())){
//                p.setClicks(p.getClicks() + 1);
//                postMapper.updatePost(p);
//            }
//        }
//
//        if(CurrentHolder.getCurrent() != null) {
//            // 添加到历史记录
//            History history = new History();
//            history.setId(snowFlake.getID());
//            history.setEntityType(EntityConstant.POST_TYPE);
//            history.setEntityId(p.getId());
//            history.setCreatedTime(LocalDateTime.now());
//            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.HISTORY_BINDING_KEY, history);
//        }

        return p;
    }

    @Override
    public void updatePost(Post post) {
        post.setUpdatedTime(LocalDateTime.now());
        String imageUrlJson = JSON.toJSONString(post.getImageUrl());
        post.setImageUrlJson(imageUrlJson);
        String tagsJson = JSON.toJSONString(post.getTags());
        post.setTagsJson(tagsJson);
        postMapper.updatePost(post);

        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        postVO.setType(MqConstant.SEND_PUT);
        rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.POST_BINDING_KEY, postVO);
    }

    @Override
    public void deletePost(Long id) {
        postMapper.deletePost(id);

        // 更新用户动态数
        User user = userMapper.getUserById(CurrentHolder.getCurrent());
        if(user != null){
            Integer postCount = user.getPostCount();
            user.setPostCount(postCount - 1);
            userMapper.updateUserById(user);
        }

        PostVO postVO = new PostVO();
        postVO.setId(id);
        postVO.setType(MqConstant.SEND_DELETE);
        rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.POST_BINDING_KEY, postVO);
    }

    @Override
    public List<PostVO> getPostByFollowing(Long userId) {
        List<Long> followingIds = followMapper.getFollowingIds(userId);
        // 添加自己
        followingIds.add(userId);
        List<PostVO>postVOS =  postMapper.getPostByUserIds(followingIds);
        if(postVOS.isEmpty()){
            return List.of();
        }
        postVOS.forEach(postVO -> {
            String imageUrlJson = postVO.getImageUrlJson();
            postVO.setImageUrl(JSON.parseArray(imageUrlJson, String.class));
            String tagsJson = postVO.getTagsJson();
            postVO.setTags(JSON.parseArray(tagsJson, String.class));

            if(postVO.getVideoId() != null){
                Video video = videoMapper.getVideoById(postVO.getVideoId());
                if(video != null){
                    User u = userMapper.getUserById(video.getUserId());
                    postVO.setVideoUsername(u.getUsername());
                    postVO.setVideoUserId(u.getId());
                    postVO.setVideoUserImage(u.getImage());
                    postVO.setVideoCoverUrl(video.getCoverUrl());
                    postVO.setVideoTitle(video.getTitle());
                    postVO.setVideoDescription(video.getDescription());
                    postVO.setVideoViews(video.getViews());
                    postVO.setVideoBarrages(video.getBarrages());
                    postVO.setVideoTime(video.getTime());
                }
            }
        });
        return postVOS;
    }

    @Override
    public List<PostVO> getMyPosts() {
        Long userId = CurrentHolder.getCurrent();
        List<PostVO> postVOS = postMapper.getMyPost(userId);
        if(postVOS.isEmpty()){
            return List.of();
        }
        postVOS.forEach(postVO -> {
            String imageUrlJson = postVO.getImageUrlJson();
            postVO.setImageUrl(JSON.parseArray(imageUrlJson, String.class));
            String tagsJson = postVO.getTagsJson();
            postVO.setTags(JSON.parseArray(tagsJson, String.class));

            if(postVO.getVideoId() != null){
                Video video = videoMapper.getVideoById(postVO.getVideoId());
                if(video != null){
                    User u = userMapper.getUserById(video.getUserId());
                    postVO.setVideoUsername(u.getUsername());
                    postVO.setVideoUserId(u.getId());
                    postVO.setVideoUserImage(u.getImage());
                    postVO.setVideoCoverUrl(video.getCoverUrl());
                    postVO.setVideoTitle(video.getTitle());
                    postVO.setVideoDescription(video.getDescription());
                    postVO.setVideoViews(video.getViews());
                    postVO.setVideoBarrages(video.getBarrages());
                    postVO.setVideoTime(video.getTime());
                }
            }
        });
        return postVOS;
    }

    @Override
    public List<PostVO> getPostByUserId(Long userId) {
        List<PostVO> postVOS  = postMapper.getPostByUserId(userId);
        if(postVOS.isEmpty()){
            return List.of();
        }
        postVOS.forEach(postVO -> {
            String imageUrlJson = postVO.getImageUrlJson();
            postVO.setImageUrl(JSON.parseArray(imageUrlJson, String.class));
            String tagsJson = postVO.getTagsJson();
            postVO.setTags(JSON.parseArray(tagsJson, String.class));

            if(postVO.getVideoId() != null){
                Video video = videoMapper.getVideoById(postVO.getVideoId());
                if(video != null){
                    User u = userMapper.getUserById(video.getUserId());
                    postVO.setVideoUsername(u.getUsername());
                    postVO.setVideoUserId(u.getId());
                    postVO.setVideoUserImage(u.getImage());
                    postVO.setVideoCoverUrl(video.getCoverUrl());
                    postVO.setVideoTitle(video.getTitle());
                    postVO.setVideoDescription(video.getDescription());
                    postVO.setVideoViews(video.getViews());
                    postVO.setVideoBarrages(video.getBarrages());
                    postVO.setVideoTime(video.getTime());
                }
            }
        });
        return postVOS;
    }
}
