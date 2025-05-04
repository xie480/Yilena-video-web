package com.yilena.service.controller.admin;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.PostPendingDTO;
import com.yilena.service.service.PostPendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/manager")
public class PostPendingController {
    private final PostPendingService postPendingService;

    @PutMapping("/post")
    public Result updatePostPendingStatus(@RequestBody PostPendingDTO postPendingDTO){
        log.info("下架动态：{}", postPendingDTO);
        postPendingService.updatePostPendingStatus(postPendingDTO);
        return Result.success();
    }
}
