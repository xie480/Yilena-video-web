package com.yilena.service.dao;

import com.yilena.service.entity.po.VideoFavorites;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface VideoFavoritesMapper {
    void addVideoFavorites(VideoFavorites videoFavorites);

    void deleteVideoFavorites(VideoFavorites videoFavorites);

    VideoFavorites isFavoritesFolder(Long videoId, Long userId);

    Long getTotalFavorite();
}
