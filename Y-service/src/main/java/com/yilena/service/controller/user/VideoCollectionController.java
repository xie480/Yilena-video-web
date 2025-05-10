package com.yilena.service.controller.user;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.VideoCollectionDoOrUndoDTO;
import com.yilena.service.entity.po.VideoCollection;
import com.yilena.service.entity.vo.VideoVO;
import com.yilena.service.log.LogOperation;
import com.yilena.service.service.VideoCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/collection")
public class VideoCollectionController {

    private final VideoCollectionService videoCollectionService;

    @CacheEvict(value = "videoCollection", key = "#videoCollection.userId + ':' + #videoCollection.userId ")
    @LogOperation
    @PostMapping
    public Result addVideoCollection(@RequestBody VideoCollection videoCollection) {
        log.info("新增投稿合集：{}", videoCollection);
        videoCollectionService.addVideoCollection(videoCollection);
        return Result.success();
    }

    @Cacheable(value = "videoCollection", key = "#userId + ':' + #currentUserId ")
    @GetMapping("/search/list/{userId}/{currentUserId}")
    public Result getVideoCollectionByList(@PathVariable("userId") Long userId, @PathVariable("currentUserId") Long currentUserId) {
        log.info("列表查询投稿合集：{}", userId);
        return Result.success(videoCollectionService.getVideoCollectionByList(userId,currentUserId));
    }

    @CacheEvict(value = "videoCollection", key = "#videoCollection.userId + ':' + #videoCollection.userId ")
    @LogOperation
    @PutMapping
    public Result updateVideoCollection(@RequestBody VideoCollection videoCollection) {
        log.info("更新投稿合集：{}", videoCollection);
        videoCollectionService.updateVideoCollection(videoCollection);
        return Result.success();
    }

    @CacheEvict(value = {"videoCollection","videoCollectionVideos"},allEntries = true)
    @LogOperation
    @DeleteMapping
    public Result deleteVideoCollection(Long id) {
        log.info("删除投稿合集：{}", id);
        videoCollectionService.deleteVideoCollection(id);
        return Result.success();
    }

    @CacheEvict(value = {"videoCollection","videoCollectionVideos"}, allEntries = true)
    @PutMapping("/do")
    public Result doVideoCollection(VideoCollectionDoOrUndoDTO videoCollectionDoOrUndo) {
        log.info("添加投稿合集：{}", videoCollectionDoOrUndo);
        videoCollectionService.doVideoCollection(videoCollectionDoOrUndo);
        return Result.success();
    }

    @CacheEvict(value = {"videoCollection","videoCollectionVideos"}, allEntries = true)
    @PutMapping("/undo")
    public Result undoVideoCollection(VideoCollectionDoOrUndoDTO videoCollectionDoOrUndo) {
        log.info("移出投稿合集：{}", videoCollectionDoOrUndo);
        videoCollectionService.undoVideoCollection(videoCollectionDoOrUndo);
        return Result.success();
    }

    @Cacheable(value = "videoCollectionVideos", key = "#userId + ':' + #collectionId ")
    @GetMapping("/search/videos/{userId}/{collectionId}")
    public Result getVideoIdsByCollectionId(@PathVariable Long userId, @PathVariable Long collectionId) {
        log.info("查询合集视频：{}", collectionId);
        List<VideoVO> videos = videoCollectionService.getVideoIdsByCollectionId(userId, collectionId);
        return Result.success(videos);
    }
}
