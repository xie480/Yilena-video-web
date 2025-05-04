package com.yilena.service.controller.user;

import com.yilena.service.dao.ShareMapper;
import com.yilena.service.entity.Result;
import com.yilena.service.entity.po.Share;
import com.yilena.service.service.ShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/share")
public class ShareController {

    private final ShareService shareService;

    @GetMapping("/search/{userId}/{entityId}/{entityType}")
    public Result getIsShare(@PathVariable("userId") Long userId,
                             @PathVariable("entityId") Long entityId,
                             @PathVariable("entityType") Integer entityType){
        log.info("查询是否转发");
        Share share = new Share(null, entityId, entityType, userId, null);
        return Result.success(shareService.getIsShare(share));
    }

    @PostMapping
    public Result doShare(@RequestBody Share share){
        log.info("转发");
        shareService.doShare(share);
        return Result.success();
    }
}
