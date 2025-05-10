package com.yilena.service.service.impl;

import com.alibaba.fastjson.JSON;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.VideoCollectionMapper;
import com.yilena.service.dao.VideoMapper;
import com.yilena.service.entity.dto.VideoCollectionDoOrUndoDTO;
import com.yilena.service.entity.po.VideoCollection;
import com.yilena.service.entity.vo.VideoVO;
import com.yilena.service.service.VideoCollectionService;
import com.yilena.service.utils.CurrentHolder;
import com.yilena.service.utils.SensitiveWordFilter;
import com.yilena.service.utils.SnowFlake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VideoCollectionServiceImpl implements VideoCollectionService {

    private final VideoCollectionMapper videoCollectionMapper;
    private final VideoMapper videoMapper;
    private final SnowFlake snowFlake;

    @Override
    public void addVideoCollection(VideoCollection videoCollection) {
        videoCollection.setId(snowFlake.getID());
        videoCollection.setUserId(CurrentHolder.getCurrent());
        videoCollection.setIsDefault(StatusConstant.STATUS_NO);
        videoCollection.setVideoIdsJson("[]");
        videoCollection.setVisibility(videoCollection.getVisibility());
        videoCollection.setCreatedTime(LocalDateTime.now());
        videoCollection.setUpdatedTime(LocalDateTime.now());

        videoCollection.setTitle(SensitiveWordFilter.filterSensitiveWords(videoCollection.getTitle()));

        videoCollectionMapper.addVideoCollection(videoCollection);
    }

    @Override
    public List<VideoCollection> getVideoCollectionByList(Long userId,Long currentUserId) {
        List<VideoCollection> videoCollections = videoCollectionMapper.getVideoCollectionByList(userId);
        if(!userId.equals(currentUserId)){
            videoCollections.removeIf(videoCollection -> Objects.equals(videoCollection.getVisibility(), StatusConstant.STATUS_NO));
        }
        // 便于前端计算视频数量
        videoCollections.forEach(videoCollection -> {
            videoCollection.setVideoIds(JSON.parseArray(videoCollection.getVideoIdsJson(), Long.class));
        });
        return videoCollections;
    }

    @Override
    public void updateVideoCollection(VideoCollection videoCollection) {
        VideoCollection videoCollection1 = videoCollectionMapper.getVideoCollectionById(videoCollection.getId());
        if (videoCollection1 == null){
            throw new RuntimeException(StatusConstant.VIDEO_COLLECTION_NOT_EXIST);
        }
        videoCollection1.setTitle(videoCollection.getTitle());
        videoCollection1.setVisibility(videoCollection.getVisibility());
        videoCollection1.setUpdatedTime(LocalDateTime.now());
        videoCollectionMapper.updateVideoCollection(videoCollection1);
    }

    @Override
    public void deleteVideoCollection(Long id) {
        videoCollectionMapper.deleteVideoCollection(id);
    }

    @Override
    public void doVideoCollection(VideoCollectionDoOrUndoDTO videoCollectionDoOrUndoDTO) {
        VideoCollection videoCollection = videoCollectionMapper.getVideoCollectionById(videoCollectionDoOrUndoDTO.getCollectionId());
        if(videoCollection == null){
            throw new RuntimeException(StatusConstant.VIDEO_COLLECTION_NOT_EXIST);
        }
        String videoIdsJson = videoCollection.getVideoIdsJson();
        List<Long> videoIds = JSON.parseArray(videoIdsJson, Long.class);
        videoIds.forEach(videoId -> {
            if (Objects.equals(videoId, videoCollectionDoOrUndoDTO.getVideoId())) {
                throw new RuntimeException(StatusConstant.VIDEO_ALREADY_COLLECTION);
            }
        });
        videoIds.add(videoCollectionDoOrUndoDTO.getVideoId());
        videoCollection.setVideoIdsJson(JSON.toJSONString(videoIds));

        videoCollectionMapper.updateVideoCollection(videoCollection);
    }

    @Override
    public void undoVideoCollection(VideoCollectionDoOrUndoDTO videoCollectionDoOrUndoDTO) {
        VideoCollection videoCollection = videoCollectionMapper.getVideoCollectionById(videoCollectionDoOrUndoDTO.getCollectionId());
        if(videoCollection == null){
            throw new RuntimeException(StatusConstant.VIDEO_COLLECTION_NOT_EXIST);
        }
        String videoIdsJson = videoCollection.getVideoIdsJson();
        List<Long> videoIds = JSON.parseArray(videoIdsJson, Long.class);

        boolean isFavorites = false;
        Iterator<Long> iterator = videoIds.iterator(); // 使用 Iterator 遍历
        while (iterator.hasNext()) {
            Long id = iterator.next();
            if (Objects.equals(id, videoCollectionDoOrUndoDTO.getVideoId())) {
                iterator.remove(); // 通过 Iterator 的 remove 方法安全移除
                isFavorites = true;
            }
        }
        if(!isFavorites){
            throw new RuntimeException(StatusConstant.VIDEO_NOT_EXIST);
        }

        videoCollection.setVideoIdsJson(JSON.toJSONString(videoIds));
        videoCollectionMapper.updateVideoCollection(videoCollection);
    }

    @Transactional
    @Override
    public List<VideoVO> getVideoIdsByCollectionId(Long userId, Long collectionId) {
        VideoCollection videoCollection = videoCollectionMapper.getVideoCollectionById(collectionId);
        if(videoCollection == null){
            throw new RuntimeException(StatusConstant.VIDEO_COLLECTION_NOT_EXIST);
        }
        String videoIdsJson = videoCollection.getVideoIdsJson();
        List<Long> videoIds = JSON.parseArray(videoIdsJson, Long.class);
        if(videoIds.isEmpty()){
            return List.of();
        }
        List<VideoVO> videoVOS = videoMapper.getVideoByIds(videoIds);
        if(!userId.equals(videoCollection.getUserId())){
            videoVOS.removeIf(videoVO -> Objects.equals(videoVO.getVisibility(), StatusConstant.STATUS_NO));
        }
        return videoVOS;
    }
}
