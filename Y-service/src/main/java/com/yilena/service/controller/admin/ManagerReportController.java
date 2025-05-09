package com.yilena.service.controller.admin;

import com.yilena.service.entity.Result;
import com.yilena.service.service.ManagerReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/manager/report")
public class ManagerReportController {

    private final ManagerReportService managerReportService;

    @GetMapping("/video/status")
    public Result getVideoReportStatus() {
        log.info("管理员获取未通过、未审核、已通过的视频数量");
        return Result.success(managerReportService.getVideoReportStatus());
    }

    @GetMapping("/total/user")
    public Result getTotalUser() {
        log.info("管理员获取用户总数");
        return Result.success(managerReportService.getTotalUser());
    }

    @GetMapping("/total/like")
    public Result getTotalLike() {
        log.info("管理员获取点赞总数");
        return Result.success(managerReportService.getTotalLike());
    }

    @GetMapping("/total/post")
    public Result getTotalPost() {
        log.info("管理员获取动态总数");
        return Result.success(managerReportService.getTotalPost());
    }

    @GetMapping("/total/comment")
    public Result getTotalComment() {
        log.info("管理员获取评论总数");
        return Result.success(managerReportService.getTotalComment());
    }

    @GetMapping("/total/favorite")
    public Result getTotalFavorite() {
        log.info("管理员获取收藏总数");
        return Result.success(managerReportService.getTotalFavorite());
    }
}
