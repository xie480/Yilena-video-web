package com.yilena.service.dao;

import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.VideoPendingDTO;
import com.yilena.service.entity.po.Video;
import com.yilena.service.entity.po.VideoPending;
import com.yilena.service.entity.vo.VideoVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VideoMapper {

    Video getVideoById(Long id);

    void updateVideo(Video video);

    void deleteVideo(Long id);

    List<VideoVO> getVideoCoins(List<Long> videoIds);

    void addVideo(Video video);

    List<VideoVO> getLeftRecommendVideos();

    Long getUserVideoAllViews(Long userId);

    Long getUserVideoAllCoins(Long userId);

    Long getUserVideoAllComments(Long userId);

    Long getUserVideoAllBarrages(Long userId);

    Long getUserVideoAllLikes(Long userId);

    Long getUserVideoAllShares(Long userId);

    Long getUserVideoAllFavorites(Long userId);

    List<Video> getVideoByUserId(Long userId);

    List<Long> getVideoIdByUserId(Long currentUserId, String videoTitle);

    List<VideoVO> getUserInfoVideosByNew(Long userId);

    List<VideoVO> getUserInfoVideosByViews(Long userId);

    List<VideoVO> getUserInfoVideosByFavorites(Long userId);

    List<VideoVO> getVideoByIds(List<Long> videoIds);

    Integer getFollowingFromReportCount(Long userId, LocalDateTime beginDateTime, LocalDateTime endDateTime);

    Integer getLikeFromReportCount(Long userId, LocalDateTime beginDateTime, LocalDateTime endDateTime);

    Integer getFavoriteFromReportCount(Long userId, LocalDateTime beginDateTime, LocalDateTime endDateTime);

    Integer getCoinFromReportCount(Long userId, LocalDateTime beginDateTime, LocalDateTime endDateTime);

    Integer getCommentFromReportCount(Long userId, LocalDateTime beginDateTime, LocalDateTime endDateTime);

    Long getVideoCount();

    List<VideoPending> getVideoByPage(VideoPendingDTO videoPendingDTO);

    List<VideoPending> getAllVideoByUserId(Long userId);

    Integer getVideoUploadReport(LocalDateTime beginDateTime, LocalDateTime endDateTime);
}
