package com.yilena.service.controller.user;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.po.VideoPending;
import com.yilena.service.log.LogOperation;
import com.yilena.service.service.VideoPendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/videoPending")
public class UserVideoPendingController {

    private final VideoPendingService videoPendingService;

    @Cacheable(value = "videoPendings", key = "#userId + ':' + #status")
    @GetMapping("/list/{userId}/{status}")
    public Result getVideoPendingByStatus(@PathVariable Long userId,@PathVariable Integer status){
        log.info("用户{}查询视频审核状态为{}的视频", userId, status);
        return Result.success(videoPendingService.getVideoPendingByStatus(userId, status));
    }

    @CacheEvict(value = {"videoPendings","videoPending"}, allEntries = true)
    @LogOperation
    @PutMapping
    public Result updateVideoPending(@RequestBody VideoPending videoPending){
        log.info("用户{}修改视频审核状态为{}的视频", videoPending.getUserId(), videoPending.getStatus());
        videoPendingService.updateVideoPending(videoPending);
        return Result.success();
    }

    @CacheEvict(value = {"videoPendings","videoPending"}, allEntries = true)
    @DeleteMapping("/{id}")
    public Result deleteVideoPending(@PathVariable Long id) {
        log.info("用户{}删除审核视频", id);
        videoPendingService.deleteVideoPending(id);
        return Result.success();
    }

    @CacheEvict(value = {"videoPendings","videoPending"}, allEntries = true)
    @LogOperation
    @PutMapping("/reupload/{id}")
    public Result reuploadVideoPending(@PathVariable Long id) {
        log.info("用户{}重新上传视频", id);
        videoPendingService.reuploadVideoPending(id);
        return Result.success();
    }

    @Cacheable(value = "videoPending", key = "#id")
    @GetMapping("/get/{id}")
    public Result getVideoPendingById(@PathVariable Long id) {
        log.info("用户{}获取审核视频", id);
        return Result.success(videoPendingService.getVideoPendingById(id));
    }
}
