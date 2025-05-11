package com.yilena.service.service.impl;

import com.alibaba.fastjson2.JSON;
import com.yilena.service.constant.EntityConstant;
import com.yilena.service.constant.MqConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.dao.VideoCollectionMapper;
import com.yilena.service.dao.VideoMapper;
import com.yilena.service.dao.VideoPendingMapper;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.VideoDTO;
import com.yilena.service.entity.po.History;
import com.yilena.service.entity.po.Video;
import com.yilena.service.entity.po.VideoCollection;
import com.yilena.service.entity.po.VideoPending;
import com.yilena.service.entity.vo.VideoVO;
import com.yilena.service.es.VideoES;
import com.yilena.service.service.VideoService;
import com.yilena.service.utils.CurrentHolder;
import com.yilena.service.utils.SensitiveWordFilter;
import com.yilena.service.utils.SnowFlake;
import com.yilena.service.utils.VideoDurationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final VideoMapper videoMapper;
    private final RabbitTemplate rabbitTemplate;
    private final VideoES VideoES;
    private final VideoPendingMapper videoPendingMapper;
    private final VideoCollectionMapper videoCollectionMapper;
    private final SnowFlake snowFlake;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public void addVideoToPending(Video video) {
        // 设置默认值
        video.setUpdatedTime(LocalDateTime.now());
        video.setCreatedTime(LocalDateTime.now());
        video.setViews(0);
        video.setLikes(0);
        video.setFavorites(0);
        video.setShares(0);
        video.setCoins(0);
        video.setComments(0);
        video.setBarrages(0);
        video.setUserId(CurrentHolder.getCurrent());
        video.setId(snowFlake.getID());

        video.setDescription(SensitiveWordFilter.filterSensitiveWords(video.getDescription()));
        video.setTitle(SensitiveWordFilter.filterSensitiveWords(video.getTitle()));

        // 获取视频时长
        long time = VideoDurationUtils.getVideoDuration(video.getVideoUrl());
        video.setTime(time);

        // 将tags转成json
        String tagsJson = JSON.toJSONString(video.getTags());
        video.setTagsJson(tagsJson);

        videoPendingMapper.addVideoPending(video);
    }

    @Override
    public PageResult<VideoVO> getVideoByPage(VideoDTO videoDTO) {
        return VideoES.getVideoByPage(videoDTO);
    }

    @Transactional
    @Override
    public Video getVideoById(Long id) {
        Video video = videoMapper.getVideoById(id);
        // 将tags转成list
        String tagsJson = video.getTagsJson();
        video.setTags(JSON.parseArray(tagsJson, String.class));

        if(Objects.equals(video.getVisibility(), StatusConstant.STATUS_YES)) {
            // 浏览量加1
            if (!Objects.equals(CurrentHolder.getCurrent(), video.getUserId())) {
                video.setViews(video.getViews() + 1);
                videoMapper.updateVideo(video);
            }
        }

        VideoCollection videoCollection = videoCollectionMapper.getVideoCollectionById(video.getCollectionId());
        video.setCollectionName(videoCollection.getTitle());

//        // 如果用户已登录，则添加到历史记录
//        if(CurrentHolder.getCurrent() != null) {
//            // 添加到历史记录
//            History history = new History();
//            history.setId(snowFlake.getID());
//            history.setEntityType(EntityConstant.VIDEO_TYPE);
//            history.setEntityId(video.getId());
//            history.setCreatedTime(LocalDateTime.now());
//            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.HISTORY_BINDING_KEY, history);
//        }
        return video;
    }

    @Transactional
    @Override
    public void updateVideo(Video video) {
        video.setUpdatedTime(LocalDateTime.now());
        String tagsJson = JSON.toJSONString(video.getTags());
        video.setTagsJson(tagsJson);
        Video oldVideo = videoMapper.getVideoById(video.getId());

        // 如果视频未通过审核，则不能修改未可见，而且所有变动均在审核表中进行
        if(oldVideo == null){
            if (Objects.equals(video.getVisibility(), StatusConstant.STATUS_YES)){
                throw new RuntimeException("待审核的视频无法修改可见权限！");
            }
            VideoPending videoPending = new VideoPending();
            BeanUtils.copyProperties(video, videoPending);
            videoPendingMapper.updateVideoPending(videoPending);
        }else {

            // 使用安全的空值比较判断核心字段是否变更（标题、描述、封面、视频、标签）
            boolean isCoreContentUnchanged = Objects.equals(video.getTitle(), oldVideo.getTitle())
                    && Objects.equals(video.getDescription(), oldVideo.getDescription())
                    && Objects.equals(video.getCoverUrl(), oldVideo.getCoverUrl())
                    && Objects.equals(video.getVideoUrl(), oldVideo.getVideoUrl());

            // 如果视频通过了审核，如果只有可见性和投稿合集变动则在全局视频中修改，反之会返回审核表进行二次审核
            if(isCoreContentUnchanged){

                // 核心内容未变更时，检查可见性和合集ID是否修改
                boolean isVisibilityUnchanged = Objects.equals(video.getVisibility(), oldVideo.getVisibility());
                boolean isCollectionUnchanged = Objects.equals(video.getCollectionId(), oldVideo.getCollectionId());

                if(isVisibilityUnchanged && isCollectionUnchanged){
                    throw new RuntimeException("您没有做出修改！");
                }
                // 无需二次审核
                else {
                    videoMapper.updateVideo(video);
                    VideoVO videoVO = new VideoVO();
                    BeanUtils.copyProperties(video, videoVO);
                    videoVO.setType(MqConstant.SEND_PUT);
                    rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.VIDEO_BINDING_KEY, videoVO);
                }
            }else{
                // 判断是否更换了收藏夹
                if(!oldVideo.getCollectionId().equals(video.getCollectionId())){
                    // 更新新的收藏夹ids
                    VideoCollection videoCollection = videoCollectionMapper.getVideoCollectionById(video.getCollectionId());
                    List<Long> videoIds = JSON.parseArray(videoCollection.getVideoIdsJson(), Long.class);
                    videoIds.forEach(videoId -> {
                        if (Objects.equals(videoId, video.getId())) {
                            throw new RuntimeException(StatusConstant.VIDEO_ALREADY_COLLECTION);
                        }
                    });
                    videoIds.add(video.getId());
                    videoCollection.setVideoIdsJson(JSON.toJSONString(videoIds));
                    videoCollectionMapper.updateVideoCollection(videoCollection);

                    // 更新旧的收藏夹ids
                    VideoCollection oldVideoCollection = videoCollectionMapper.getVideoCollectionById(oldVideo.getCollectionId());
                    List<Long> oldVideoIds = JSON.parseArray(oldVideoCollection.getVideoIdsJson(), Long.class);
                    boolean isExist = false;
                    for (int i = 0; i < oldVideoIds.size(); i++) {
                        if(Objects.equals(oldVideoIds.get(i), video.getId())){
                            oldVideoIds.remove(i);
                            isExist = true;
                            break;
                        }
                    }
                    if(isExist){
                        oldVideoCollection.setVideoIdsJson(JSON.toJSONString(oldVideoIds));
                        videoCollectionMapper.updateVideoCollection(oldVideoCollection);
                    }else{
                        throw new RuntimeException(StatusConstant.VIDEO_NOT_EXIST);
                    }
                }

                // 将视频移到待审核表
                videoMapper.updateVideo(video);
                Video oldVideo1 = videoMapper.getVideoById(video.getId());
                oldVideo1.setVisibility(StatusConstant.STATUS_NO);
                oldVideo1.setId(snowFlake.getID());

                //记录本次视频id
                oldVideo1.setLastId(video.getId());

                videoPendingMapper.addVideoPending(oldVideo1);
                videoMapper.deleteVideo(video.getId());

                // 发送消息删除es索引
                VideoVO videoVO = new VideoVO();
                BeanUtils.copyProperties(video, videoVO);
                videoVO.setType(MqConstant.SEND_DELETE);
                rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.VIDEO_BINDING_KEY, videoVO);
            }
        }
    }

    @Override
    public void deleteVideo(Long id) {
        Video video = videoMapper.getVideoById(id);

        // 如果视频通过审核，则删除全局视频，反之删除待审核视频
        if(video != null) {
            videoMapper.deleteVideo(id);

            VideoVO videoVO = new VideoVO();
            videoVO.setId(id);
            videoVO.setType(MqConstant.SEND_DELETE);
            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.VIDEO_BINDING_KEY, videoVO);
        }else{
            videoPendingMapper.deleteVideoPending(id);
        }
    }

    @Override
    public List<VideoVO> getAllVideos() {
        List<VideoVO> videoVOs =  VideoES.getAllVideos();
        Collections.shuffle(videoVOs);
        return videoVOs;
    }

    @Transactional
    @Override
    public void updateVideoDanmuCount(String videoId, Long danmuCount) {
        Video video = videoMapper.getVideoById(Long.parseLong(videoId));
        video.setBarrages(Math.toIntExact(danmuCount));
        videoMapper.updateVideo(video);

        VideoVO videoVO = new VideoVO();
        videoVO.setId(Long.parseLong(videoId));
        videoVO.setBarrages(Math.toIntExact(danmuCount));
        videoVO.setType(MqConstant.SEND_PUT);
        rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.VIDEO_BINDING_KEY, videoVO);
    }

    @Override
    public List<VideoVO> getLeftRecommendVideos() {
        List<VideoVO> videoVOS =  videoMapper.getLeftRecommendVideos();
        for(VideoVO videoVO : videoVOS){
            videoVO.setUsername(userMapper.getUserById(videoVO.getUserId()).getUsername());
        }
        return videoVOS;
    }

    @Override
    public Long getUserVideoAllViews(Long userId) {
        Long views = videoMapper.getUserVideoAllViews(userId);
        return views == null ? 0 : views;
    }

    @Override
    public Long getUserVideoAllCoins(Long userId) {
        Long coins = videoMapper.getUserVideoAllCoins(userId);
        return coins == null ? 0 : coins;
    }

    @Override
    public Long getUserVideoAllComments(Long userId) {
        Long comments = videoMapper.getUserVideoAllComments(userId);
        return comments == null ? 0 : comments;
    }

    @Override
    public Long getUserVideoAllBarrages(Long userId) {
        Long barrages = videoMapper.getUserVideoAllBarrages(userId);
        return barrages == null ? 0 : barrages;
    }

    @Override
    public Long getUserVideoAllLikes(Long userId) {
        Long likes = videoMapper.getUserVideoAllLikes(userId);
        return likes == null ? 0 : likes;
    }

    @Override
    public Long getUserVideoAllShares(Long userId) {
        Long shares = videoMapper.getUserVideoAllShares(userId);
        return shares == null ? 0 : shares;
    }

    @Override
    public Long getUserVideoAllFavorites(Long userId) {
        Long favorites = videoMapper.getUserVideoAllFavorites(userId);
        return favorites == null ? 0 : favorites;
    }

    @Override
    public List<Video> getVideoByUserId(Long userId) {
        return videoMapper.getVideoByUserId(userId);
    }

    @Override
    public List<VideoVO> getUserInfoVideos(Long userId, Integer sortType) {
        if(sortType.equals(EntityConstant.VIDEO_NEW_TYPE)) {
            return videoMapper.getUserInfoVideosByNew(userId);
        }else if(sortType.equals(EntityConstant.VIDEO_VIEWS_TYPE)){
            return videoMapper.getUserInfoVideosByViews(userId);
        }else if(sortType.equals(EntityConstant.VIDEO_FAVORITES_TYPE)){
            return videoMapper.getUserInfoVideosByFavorites(userId);
        }
        return List.of();
    }
}
