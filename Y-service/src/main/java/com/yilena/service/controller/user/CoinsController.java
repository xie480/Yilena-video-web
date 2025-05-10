package com.yilena.service.controller.user;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.po.VideoCoins;
import com.yilena.service.service.CoinsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/coins")
public class CoinsController {

    private final CoinsService coinsService;

    @CacheEvict(value = "videoCoins" ,key="#videoCoins.userId")
    @PutMapping
    public Result addCoins(@RequestBody VideoCoins videoCoins){
        log.info("视频投币:{}",videoCoins);
        coinsService.addCoins(videoCoins);
        return Result.success();
    }

    @Cacheable(value = "videoCoins" ,key="#userId")
    @GetMapping("/search/video/list/{userId}")
    public Result getVideoCoins(@PathVariable Long userId){
        log.info("列表获取投币视频:{}", userId);
        return Result.success(coinsService.getVideoCoins(userId));
    }

    @GetMapping("/search/{videoId}/{userId}")
    public Result isCoins(@PathVariable("videoId") Long videoId,
                          @PathVariable("userId") Long userId){
        log.info("查询是否投币:{}", videoId);
        return Result.success(coinsService.isCoins(videoId, userId));
    }
}
