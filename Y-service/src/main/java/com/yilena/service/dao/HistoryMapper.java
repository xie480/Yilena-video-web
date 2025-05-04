package com.yilena.service.dao;

import com.yilena.service.entity.dto.HistoryDTO;
import com.yilena.service.entity.po.History;
import com.yilena.service.entity.vo.VideoVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface HistoryMapper {
    void addHistory(History history);

    History getHistoryByUserIdAndEntityId(Integer entityType, Long entityId, Long userId);

    void deleteHistory(Long id);

    @MapKey("dateStr")
    Map<String, HistoryDTO> getHistory(Long userId);

    void deleteAllHistory(Long userId);

    void deleteHistoryFifteenDaysAgo();
}
