package com.yilena.service.dao;

import com.yilena.service.entity.dto.FavoritesFolderDTO;
import com.yilena.service.entity.po.FavoritesFolder;
import com.yilena.service.entity.vo.FavoritesFolderVO;
import com.yilena.service.entity.vo.VideoVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FavoritesFolderMapper {
    void addFavoritesFolder(FavoritesFolder favoritesFolder);

    List<FavoritesFolderVO> getFavoritesFolderByList(FavoritesFolder favoritesFolder);

    void updateFavoritesFolder(FavoritesFolder favoritesFolder);

    FavoritesFolder getFavoritesFolderById(Long favoritesId);

    List<FavoritesFolderVO> getOthersFavoritesFolderByList(FavoritesFolder favoritesFolder);

    void deleteFavoritesFolder(Long id);

    List<FavoritesFolderVO> getSubscribeFavoritesFolder(Long userId);
}
