package com.yilena.service.controller.user;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.IsRecordDTO;
import com.yilena.service.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping("/video/{userId}/{videoId}")
    public Result addHistory(@PathVariable Long userId, @PathVariable Long videoId) {
        log.info("用户{}观看了视频{}", userId, videoId);
        historyService.addHistory(userId, videoId);
        return Result.success();
    }

    @GetMapping("/{userId}")
    public Result getHistory(@PathVariable Long userId) {
        log.info("获取用户{}的观看历史", userId);
        return Result.success(historyService.getHistory(userId));
    }

    @CacheEvict(value = "user", key = "#isRecord.getUsername()")
    @PutMapping("/isRecord")
    public Result updateIsRecord(@RequestBody IsRecordDTO isRecord){
        log.info("用户{}的观看记录是否更新{}", isRecord.getUserId(),  isRecord.getIsRecord());
        historyService.updateIsRecord(isRecord.getUserId(), isRecord.getIsRecord());
        return Result.success();
    }

    @DeleteMapping("/delAll/{userId}")
    public Result deleteAllHistory(@PathVariable Long userId) {
        log.info("删除用户{}的全部观看历史", userId);
        historyService.deleteAllHistory(userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result deleteHistory(@PathVariable Long id) {
        log.info("删除观看历史{}", id);
        historyService.deleteHistory(id);
        return Result.success();
    }
}
