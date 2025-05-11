package com.yilena.service.entity.dto;

import com.yilena.service.entity.vo.VideoVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryDTO implements Serializable {
    private String dateStr;
    private List<Long> entityIds;
    private List<VideoVO> videoVOS;
    private List<LocalDateTime> createTimes;
    private List<Long> ids;
}