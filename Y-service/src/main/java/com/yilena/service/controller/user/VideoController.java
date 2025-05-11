package com.yilena.service.controller.user;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.VideoDTO;
import com.yilena.service.entity.po.Video;
import com.yilena.service.log.LogOperation;
import com.yilena.service.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/video")
public class VideoController {

    private final VideoService videoService;

    @CacheEvict(value = "videoPendings", key = "#video.userId + ':' + 0")
    @LogOperation
    @PostMapping
    public Result addVideo(@RequestBody Video video){
        log.info("添加视频{}", video);
        videoService.addVideoToPending(video);
        return Result.success();
    }

    @Transactional
    @GetMapping("/search/page")
    public Result getVideoByPage(VideoDTO videoDTO){
        log.info("用户查询视频分页信息");
        return Result.success(videoService.getVideoByPage(videoDTO));
    }

    @GetMapping("/{id}")
    public Result getVideoById(@PathVariable Long id){
        log.info("用户查询视频信息");
        return Result.success(videoService.getVideoById(id));
    }

    @LogOperation
    @PutMapping
    public Result updateVideo(@RequestBody Video video){
        log.info("更新视频{}", video);
        videoService.updateVideo(video);
        return Result.success();
    }

    @LogOperation
    @DeleteMapping("/{id}")
    public Result deleteVideo(@PathVariable Long id){
        log.info("删除视频{}", id);
        videoService.deleteVideo(id);
        return Result.success();
    }

    @GetMapping("/search/list")
    public Result getAllVideos(){
        log.info("获取所有视频");
        return Result.success(videoService.getAllVideos());
    }

    @GetMapping("/search/leftRecommend")
    public Result getLeftRecommendVideos(){
        log.info("获取左侧推荐视频");
        return Result.success(videoService.getLeftRecommendVideos());
    }

    @Cacheable(value = "allViews",key = "#userId")
    @GetMapping("/allViews/{userId}")
    public Result getUserVideoAllViews(@PathVariable Long userId){
        log.info("获取当前用户视频的所有播放量");
        return Result.success(videoService.getUserVideoAllViews(userId));
    }

    @Cacheable(value = "allCoins",key = "#userId")
    @GetMapping("/allCoins/{userId}")
    public Result getUserVideoAllCoins(@PathVariable Long userId){
        log.info("获取当前用户视频的所有硬币");
        return Result.success(videoService.getUserVideoAllCoins(userId));
    }

    @Cacheable(value = "allComments",key = "#userId")
    @GetMapping("/allComments/{userId}")
    public Result getUserVideoAllComments(@PathVariable Long userId){
        log.info("获取当前用户视频的所有评论");
        return Result.success(videoService.getUserVideoAllComments(userId));
    }

    @Cacheable(value = "allBarrages",key = "#userId")
    @GetMapping("/allBarrages/{userId}")
    public Result getUserVideoAllBarrages(@PathVariable Long userId){
        log.info("获取当前用户视频的所有弹幕");
        return Result.success(videoService.getUserVideoAllBarrages(userId));
    }

    @Cacheable(value = "allLikes",key = "#userId")
    @GetMapping("/allLikes/{userId}")
    public Result getUserVideoAllLikes(@PathVariable Long userId){
        log.info("获取当前用户视频的所有点赞");
        return Result.success(videoService.getUserVideoAllLikes(userId));
    }

    @Cacheable(value = "allShares",key = "#userId")
    @GetMapping("/allShares/{userId}")
    public Result getUserVideoAllShares(@PathVariable Long userId){
        log.info("获取当前用户视频的所有分享");
        return Result.success(videoService.getUserVideoAllShares(userId));
    }

    @Cacheable(value = "allFavorites",key = "#userId")
    @GetMapping("/allFavorites/{userId}")
    public Result getUserVideoAllFavorites(@PathVariable Long userId){
        log.info("获取当前用户视频的所有收藏");
        return Result.success(videoService.getUserVideoAllFavorites(userId));
    }

    @GetMapping("/search/user/{userId}")
    public Result getVideoByUserId(@PathVariable Long userId){
        log.info("获取当前用户视频");
        return Result.success(videoService.getVideoByUserId(userId));
    }

    @Cacheable(value = "userInfoVideo",key = "#userId+ ':' + #sortType")
    @GetMapping("/search/userInfo/{userId}/{sortType}")
    public Result getUserInfoVideos(@PathVariable Long userId, @PathVariable Integer sortType){
        log.info("获取当前用户主页视频");
        return Result.success(videoService.getUserInfoVideos(userId,sortType));
    }
}
