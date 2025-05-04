package com.yilena.service.service;

import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.VideoDTO;
import com.yilena.service.entity.po.Video;
import com.yilena.service.entity.vo.VideoVO;

import java.util.List;

public interface VideoService {
    void addVideoToPending(Video video);

    PageResult<VideoVO> getVideoByPage(VideoDTO videoDTO);

    Video getVideoById(Long id);

    void updateVideo(Video video);

    void deleteVideo(Long id);

    List<VideoVO> getAllVideos();

    void updateVideoDanmuCount(String videoId, Long danmuCount);

    List<VideoVO> getLeftRecommendVideos();

    Long getUserVideoAllViews(Long userId);

    Long getUserVideoAllCoins(Long userId);

    Long getUserVideoAllComments(Long userId);

    Long getUserVideoAllBarrages(Long userId);

    Long getUserVideoAllLikes(Long userId);

    Long getUserVideoAllShares(Long userId);

    Long getUserVideoAllFavorites(Long userId);

    List<Video> getVideoByUserId(Long userId);

    List<VideoVO> getUserInfoVideos(Long userId,Integer sortType);
}
