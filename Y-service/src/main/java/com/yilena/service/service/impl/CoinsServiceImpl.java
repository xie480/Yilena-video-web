package com.yilena.service.service.impl;

import com.yilena.service.constant.EntityConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.CoinsMapper;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.dao.VideoMapper;
import com.yilena.service.entity.po.Video;
import com.yilena.service.entity.po.VideoCoins;
import com.yilena.service.entity.vo.VideoVO;
import com.yilena.service.service.CoinsService;
import com.yilena.service.utils.CurrentHolder;
import com.yilena.service.utils.SnowFlake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoinsServiceImpl implements CoinsService {

    private final CoinsMapper coinsMapper;
    private final VideoMapper videoMapper;
    private final UserMapper userMapper;
    private final SnowFlake snowFlake;

    @Transactional
    @Override
    public void addCoins(VideoCoins videoCoins) {
        videoCoins.setUserId(CurrentHolder.getCurrent());
        Long videoId = videoCoins.getVideoId();
        Video video = videoMapper.getVideoById(videoId);
        video.setCoins(video.getCoins()+videoCoins.getCoinsCount());
        videoMapper.updateVideo(video);

        videoCoins.setCreatedTime(LocalDateTime.now());
        videoCoins.setId(snowFlake.getID());
        coinsMapper.addCoins(videoCoins);
    }

    @Override
    public List<VideoVO> getVideoCoins(Long userId) {
        List<Long> videosIds = coinsMapper.getVideoCoins(userId);
        if (videosIds.isEmpty()){
            throw new RuntimeException("没有投币的视频");
        }
        List<VideoVO> videoVOS = videoMapper.getVideoCoins(videosIds);
        if(videoVOS.isEmpty()){
            throw new RuntimeException(StatusConstant.VIDEO_NOT_EXIST);
        }
        videoVOS.forEach(videoVO -> {
            Long videoUserId = videoVO.getUserId();
            videoVO.setUsername(userMapper.getUserById(videoUserId).getUsername());
        });
        return videoVOS;
    }

    @Override
    public VideoCoins isCoins(Long videoId, Long userId) {
        VideoCoins videoCoins =  coinsMapper.isCoins(videoId, userId);
//        // 检测是否是之前视频的投币
//        if(videoCoins == null){
//                Video video = videoMapper.getVideoById(videoId);
//                if(video != null){
//                    Long oldId = video.getLastId();
//                    Integer oldCoins = video.getCoins();
//                    videoCoins = coinsMapper.isCoins(oldId, userId);
//                    // 如果存在投币记录
//                    if(videoCoins != null){
//                        // 删除旧纪录
//                        VideoCoins videoCoins1 = new VideoCoins();
//                        videoCoins1.setVideoId(oldId);
//                        videoCoins1.setUserId(userId);
//                        coinsMapper.deleteCoins(videoCoins1);
//
//                        // 添加新记录
//                        videoCoins1.setId(snowFlake.getID());
//                        videoCoins1.setVideoId(videoId);
//                        videoCoins1.setCreatedTime(videoCoins.getCreatedTime());
//                        coinsMapper.addCoins(videoCoins1);
//                    }
//                }
//        }
        return videoCoins;
    }
}
