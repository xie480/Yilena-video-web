package com.yilena.service.dao;

import com.yilena.service.entity.po.VideoCoins;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CoinsMapper {
    void addCoins(VideoCoins videoCoins);

    List<Long> getVideoCoins(Long userId);

    VideoCoins isCoins(Long videoId, Long userId);

    void deleteCoins(VideoCoins videoCoins1);
}
