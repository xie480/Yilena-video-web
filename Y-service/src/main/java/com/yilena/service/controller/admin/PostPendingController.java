package com.yilena.service.controller.admin;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.PostPendingDTO;
import com.yilena.service.entity.dto.PostPendingPageQueryDTO;
import com.yilena.service.log.LogOperation;
import com.yilena.service.service.PostPendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/manager/post")
public class PostPendingController {
    private final PostPendingService postPendingService;

    @LogOperation
    @PutMapping
    public Result updatePostPendingStatus(@RequestBody PostPendingDTO postPendingDTO){
        log.info("下架动态：{}", postPendingDTO);
        postPendingService.updatePostPendingStatus(postPendingDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result getPostPendingByPage(PostPendingPageQueryDTO postPendingPageQueryDTO){
        log.info("分页查询动态：{}", postPendingPageQueryDTO);
        return Result.success(postPendingService.getPostPendingByPage(postPendingPageQueryDTO));
    }
}
