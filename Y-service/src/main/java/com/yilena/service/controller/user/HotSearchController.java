package com.yilena.service.controller.user;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.po.HotSearch;
import com.yilena.service.service.HotSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/hotSearch")
public class HotSearchController {
    private final HotSearchService hotSearchService;

    @GetMapping("/search/top")
    @Cacheable(value = "hotSearch", key = "'top10'")
    public Result getTopHotSearch() {
        log.info("获取热搜前十");
        return Result.success(hotSearchService.getTopHotSearch());
    }

    @PostMapping
    public Result addSearchHistory(HotSearch hotSearch) {
        log.info("添加搜索记录");
        hotSearchService.addSearchHistory(hotSearch);
        return Result.success();
    }
}
