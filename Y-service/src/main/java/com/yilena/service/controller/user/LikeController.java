package com.yilena.service.controller.user;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.po.Like;
import com.yilena.service.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;

    @CacheEvict(value = "postWhichFollowing", key = "#like.userId")
    @PutMapping("/do")
    public Result doLike(@RequestBody Like like){
        log.info("点赞");
        likeService.doLike(like);
        return Result.success();
    }

    @CacheEvict(value = "postWhichFollowing", key = "#like.userId")
    @PutMapping("/undo")
    public Result undoLike(@RequestBody Like like){
        log.info("取消点赞");
        likeService.undoLike(like);
        return Result.success();
    }

    @GetMapping("/search/{userId}/{entityId}/{entityType}")
    public Result isLike(@PathVariable("userId") Long userId,
                         @PathVariable("entityId") Long entityId,
                         @PathVariable("entityType") Integer entityType){
        log.info("查询点赞状态");
        Like like = new Like(null, entityId, entityType, userId, null);
        return Result.success(likeService.isLike(like));
    }
}
