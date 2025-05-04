package com.yilena.service.service;

import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.FavoritesFolderDoOrUndoDTO;
import com.yilena.service.entity.dto.FavoritesFolderVideosDTO;
import com.yilena.service.entity.po.FavoritesFolder;
import com.yilena.service.entity.po.VideoFavorites;
import com.yilena.service.entity.vo.FavoritesFolderVO;
import com.yilena.service.entity.vo.VideoVO;

import java.util.List;

public interface FavoritesFolderService {
    void addFavoritesFolder(FavoritesFolder favoritesFolder);

    List<FavoritesFolderVO> getFavoritesFolderByList();

    void updateFavoritesFolder(FavoritesFolder favoritesFolder);

    void doFavoritesFolder(FavoritesFolderDoOrUndoDTO favoritesFolderDoOrUndoDTO);

    void unDoFavoritesFolder(FavoritesFolderDoOrUndoDTO favoritesFolderDoOrUndoDTO);

    List<FavoritesFolderVO> getFavoritesFolderById(Long userId);

    PageResult<VideoVO> getVideosFromFavoritesFolderByPage(FavoritesFolderVideosDTO favoritesFolderVideosDTO,Long userId);

    void deleteFavoritesFolder(Long id);

    void subscribeOthersFavoritesFolder(Long id);

    VideoFavorites isFavoritesFolder(Long videoId, Long userId);

    List<FavoritesFolderVO> getSubscribeFavoritesFolder(Long userId);

    FavoritesFolder isSubscribe(Long userId, Long favoritesFolderId);

    void unsubscribeOthersFavoritesFolder(Long id);
}
