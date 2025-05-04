package com.yilena.service.service;

import com.yilena.service.entity.dto.HistoryDTO;
import com.yilena.service.entity.vo.VideoVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface HistoryService {
    void addHistory(Long userId, Long videoId);

    Map<String, HistoryDTO> getHistory(Long userId);

    void updateIsRecord(Long userId, Integer isRecord);

    void deleteAllHistory(Long userId);

    void deleteHistory(Long id);
}
