package com.yilena.service.dao;

import com.yilena.service.entity.po.HotSearch;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HotSearchMapper {
    List<HotSearch> getHotSearchVideo();

    void updateSearchHistory(HotSearch hsv);

    void addSearchHistory(HotSearch hsv);

    HotSearch getHotSearchVideoByContent(String content);

    void deleteHotSearchLtHundredCount();
}
