package com.yilena.service.controller.user;

import com.alibaba.fastjson2.JSON;
import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.DanmuDTO;
import com.yilena.service.entity.po.Danmu;
import com.yilena.service.entity.po.Video;
import com.yilena.service.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/danmu")
public class DanmuController {
    private final RedisTemplate redisTemplate;
    private final VideoService videoService;

    @PostMapping("/list")
    public Result listDanmu(@RequestBody DanmuDTO danmuDTO) {
        log.info("查询弹幕列表：{}", danmuDTO);

        // 按时间范围查询（当前时间±5秒）
        Set<String> danmus = redisTemplate.opsForZSet().rangeByScore(
                "videoDanmu:"+danmuDTO.getVideoId(),
                danmuDTO.getCurrentTime(),
                danmuDTO.getCurrentTime() + 5,
                0,
                danmuDTO.getPageSize()
        );
        return Result.success(danmus.stream()
                .map(json -> JSON.parseObject(json, Danmu.class))
                .collect(Collectors.toList()));
    }

    @GetMapping("/count/{videoId}")
    public Result countDanmu(@PathVariable("videoId") String videoId) {
        log.info("查询弹幕数量：{}", videoId);
        Long danmuCount = redisTemplate.opsForZSet().zCard("videoDanmu:" + videoId);
        videoService.updateVideoDanmuCount(videoId, danmuCount);
        return Result.success(danmuCount);
    }
}
