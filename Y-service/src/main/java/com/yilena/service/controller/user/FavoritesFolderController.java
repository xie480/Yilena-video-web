package com.yilena.service.controller.user;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.FavoritesFolderDoOrUndoDTO;
import com.yilena.service.entity.dto.FavoritesFolderVideosDTO;
import com.yilena.service.entity.po.FavoritesFolder;
import com.yilena.service.log.LogOperation;
import com.yilena.service.service.FavoritesFolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoritesFolderController {

    private final FavoritesFolderService favoritesFolderService;

    @LogOperation
    @PostMapping
    public Result addFavoritesFolder(@RequestBody FavoritesFolder favoritesFolder){
        log.info("添加收藏夹{}", favoritesFolder);
        favoritesFolderService.addFavoritesFolder(favoritesFolder);
        return Result.success();
    }

    @GetMapping("/list")
    public Result getFavoritesFolderByList(){
        log.info("列表获取收藏夹");
        return Result.success(favoritesFolderService.getFavoritesFolderByList());
    }

    @CacheEvict(value = "favoritesFolder", key = "#favoritesFolder.id")
    @LogOperation
    @PutMapping
    public Result updateFavoritesFolder(@RequestBody FavoritesFolder favoritesFolder){
        log.info("修改收藏夹{}", favoritesFolder);
        favoritesFolderService.updateFavoritesFolder(favoritesFolder);
        return Result.success();
    }

    @CacheEvict(value = {"favoritesFolder", "userFavorites","folderVideos"}, allEntries = true)
    @PutMapping("/video/do")
    public Result doFavoritesFolder(@RequestBody FavoritesFolderDoOrUndoDTO favoritesFolderDoOrUndoDTO){
        log.info("收藏视频{}", favoritesFolderDoOrUndoDTO);
        favoritesFolderService.doFavoritesFolder(favoritesFolderDoOrUndoDTO);
        return Result.success();
    }

    @CacheEvict(value = {"favoritesFolder", "userFavorites","folderVideos"}, allEntries = true)
    @PutMapping("/video/undo")
    public Result undoFavoritesFolder(@RequestBody FavoritesFolderDoOrUndoDTO favoritesFolderDoOrUndoDTO){
        log.info("取消收藏视频{}", favoritesFolderDoOrUndoDTO);
        favoritesFolderService.unDoFavoritesFolder(favoritesFolderDoOrUndoDTO);
        return Result.success();
    }

    @Cacheable(value = "favoritesFolder", key = "#id")
    @GetMapping("/search/{id}")
    public Result getFavoritesFolderById(@PathVariable Long id){
        log.info("获取收藏夹{}", id);
        return Result.success(favoritesFolderService.getFavoritesFolderById(id));
    }
    @Cacheable(value = "folderVideos", key = "#userId + ':' + " +
            "#favoritesFolderVideosDTO.id + ':' + #favoritesFolderVideosDTO.page + ':' + #favoritesFolderVideosDTO.pageSize")
    @GetMapping("/search/video/{userId}")
    public Result getVideosFromFavoritesFolderById(FavoritesFolderVideosDTO favoritesFolderVideosDTO,@PathVariable Long userId){
        log.info("获取收藏夹{}的视频", favoritesFolderVideosDTO);
        return Result.success(favoritesFolderService.getVideosFromFavoritesFolderByPage(favoritesFolderVideosDTO,userId));
    }

    @CacheEvict(value = {"favoritesFolder", "userFavorites"}, allEntries = true)
    @LogOperation
    @DeleteMapping
    public Result deleteFavoritesFolder(Long id){
        log.info("删除收藏夹{}", id);
        favoritesFolderService.deleteFavoritesFolder(id);
        return Result.success();
    }

    @CacheEvict(value = "userFavorites", allEntries = true)
    @PutMapping("/others/do")
    public Result subscribeOthersFavoritesFolder(Long id){
        log.info("订阅收藏夹{}", id);
        favoritesFolderService.subscribeOthersFavoritesFolder(id);
        return Result.success();
    }

    @CacheEvict(value = "userFavorites", allEntries = true)
    @PutMapping("/others/undo")
    public Result unsubscribeOthersFavoritesFolder(Long id){
        log.info("取消订阅收藏夹{}", id);
        favoritesFolderService.unsubscribeOthersFavoritesFolder(id);
        return Result.success();
    }

    @GetMapping("/search/{videoId}/{userId}")
    public Result isFavoritesFolder(@PathVariable Long videoId, @PathVariable Long userId){
        log.info("判断用户{}是否收藏了视频{}", userId, videoId);
        return Result.success(favoritesFolderService.isFavoritesFolder(videoId, userId));
    }

    @Cacheable(value = "userFavorites", key = "#userId")
    @GetMapping("/search/subscribe/{userId}")
    public Result getSubscribeFavoritesFolder(@PathVariable Long userId){
        log.info("获取用户的订阅收藏夹");
        return Result.success(favoritesFolderService.getSubscribeFavoritesFolder(userId));
    }

    @GetMapping("/others/isSubscribe/{userId}/{favoritesFolderId}")
    public Result isSubscribe(@PathVariable Long userId, @PathVariable Long favoritesFolderId){
        log.info("判断用户{}是否订阅了收藏夹{}", userId, favoritesFolderId);
        return Result.success(favoritesFolderService.isSubscribe(userId, favoritesFolderId));
    }
}
