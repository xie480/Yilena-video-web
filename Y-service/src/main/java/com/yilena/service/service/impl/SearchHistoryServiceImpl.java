//package com.yilena.service.service.impl;
//
//import com.yilena.service.dao.HotSearchMapper;
//import com.yilena.service.dao.SearchHistoryMapper;
//import com.yilena.service.entity.po.HotSearch;
//import com.yilena.service.entity.po.SearchHistory;
//import com.yilena.service.entity.vo.SearchHistoryVO;
//import com.yilena.service.service.SearchHistoryService;
//import com.yilena.service.utils.CurrentHolder;
//import com.yilena.service.utils.SnowFlake;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.BeanUtils;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class SearchHistoryServiceImpl implements SearchHistoryService {
//
//    private final SearchHistoryMapper searchHistoryMapper;
//    private final HotSearchMapper hotSearchVideoMapper;
//    private final SnowFlake snowFlake;
//
//    @Override
//    public List<SearchHistoryVO> getSearchHistory() {
//        Long id = CurrentHolder.getCurrent();
//        return searchHistoryMapper.getSearchHistory(id);
//    }
//
//    @Transactional
//    @CacheEvict(value="searchHistory", key="'page'")// 删除缓存
//    @Override
//    public void addSearchHistory(SearchHistory searchHistory) {
//        Long id = CurrentHolder.getCurrent();
//        searchHistory.setUserId(id);
//        HotSearch hotSearchVideo = new HotSearch();
//        BeanUtils.copyProperties(searchHistory,hotSearchVideo);
//        SearchHistory searchHistory1 = searchHistoryMapper.getSearchHistoryByContentAndId(searchHistory);
//        HotSearch hsv = hotSearchVideoMapper.getHotSearchVideo(hotSearchVideo);
//        if(searchHistory1 != null){
//            //更新搜索次数
//            searchHistory1.setSearchCount(searchHistory1.getSearchCount()+1);
//            searchHistoryMapper.updateSearchHistory(searchHistory1);
//
//            hsv.setSearchCount(hsv.getSearchCount()+1);
//            hotSearchVideoMapper.updateSearchHistory(hsv);
//        }else{
//            // 添加搜索记录
//            searchHistory.setUserId(id);
//            searchHistory.setSearchCount(1);
//            searchHistory.setCreatedTime(LocalDateTime.now());
//            searchHistory.setId(snowFlake.getID());
//            searchHistoryMapper.addSearchHistory(searchHistory);
//
//            hotSearchVideo.setSearchCount(1);
//            hotSearchVideo.setId(snowFlake.getID());
//            hotSearchVideo.setCreatedTime(LocalDateTime.now());
//            hotSearchVideoMapper.addSearchHistory(hotSearchVideo);
//
//        }
//    }
//}
