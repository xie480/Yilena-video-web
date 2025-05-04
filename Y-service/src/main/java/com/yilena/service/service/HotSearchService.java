package com.yilena.service.service;

import com.yilena.service.entity.po.HotSearch;

import java.util.List;

public interface HotSearchService {
    List<HotSearch> getTopHotSearch();

    void addSearchHistory(HotSearch hotSearch);
}
