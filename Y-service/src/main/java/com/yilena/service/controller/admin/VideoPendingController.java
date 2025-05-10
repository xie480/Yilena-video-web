package com.yilena.service.controller.admin;

import com.yilena.service.constant.StatusConstant;
import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.VideoPendingDTO;
import com.yilena.service.entity.dto.VideoPendingStatusDTO;
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
@RequestMapping("/manager/pending")
public class VideoPendingController {

    private final VideoPendingService videoPendingService;

    @Cacheable(value = "videoPage",key = "#videoPendingDTO.status + ':' + #videoPendingDTO.pageSize + ':' + #videoPendingDTO.page")
    @GetMapping("/page")
    public Result getVideoPendingByPageWhichWait(VideoPendingDTO videoPendingDTO){
        log.info("分页查询视频");
        return Result.success(videoPendingService.getVideoPendingByPage(videoPendingDTO));
    }

    @GetMapping("/{id}")
    public Result getVideoPendingById(@PathVariable Long id){
        log.info("查询待审核视频");
        return Result.success(videoPendingService.getVideoPendingById(id));
    }

    @LogOperation
    @CacheEvict(value = "videoPage")
    @PutMapping("/status")
    public Result updateVideoPendingStatus(@RequestBody VideoPendingStatusDTO videoPendingStatusDTO){
        log.info("更新待审核视频状态");
        videoPendingService.updateVideoPendingStatus(videoPendingStatusDTO);
        return Result.success();
    }

    @LogOperation
    @CacheEvict(value = "videoPage")
    @PutMapping("/video")
    public Result updateVideoPendingFromVideo(@RequestBody VideoPendingStatusDTO videoPendingStatusDTO){
        log.info("下架视频");
        videoPendingService.updateVideoPendingFromVideo(videoPendingStatusDTO);
        return Result.success();
    }
}
