//package com.yilena.service.controller.user;
//
//import com.yilena.service.entity.Result;
//import com.yilena.service.entity.po.SearchHistory;
//import com.yilena.service.service.SearchHistoryService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.CachePut;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.*;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/searchHistory")
//public class SearchHistoryController {
//
//    private final SearchHistoryService searchHistoryService;
//
//    @Cacheable(value = "searchHistory", key = "'page'")
//    @GetMapping
//    public Result getSearchHistory() {
//        log.info("获取搜索历史");
//        return Result.success(searchHistoryService.getSearchHistory());
//    }
//
//    @PostMapping
//    public Result addSearchHistory(SearchHistory searchHistory) {
//        log.info("添加搜索历史");
//        searchHistoryService.addSearchHistory(searchHistory);
//        return Result.success();
//    }
//}
