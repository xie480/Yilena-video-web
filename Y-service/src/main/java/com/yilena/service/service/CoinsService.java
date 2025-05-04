package com.yilena.service.service;

import com.yilena.service.entity.po.VideoCoins;
import com.yilena.service.entity.vo.VideoVO;

import java.util.List;

public interface CoinsService {
    void addCoins(VideoCoins videoCoins);

    List<VideoVO> getVideoCoins(Long userId);

    VideoCoins isCoins(Long videoId, Long userId);
}
