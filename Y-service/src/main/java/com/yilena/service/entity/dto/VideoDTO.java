package com.yilena.service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoDTO {
    private Long id;
    private String description;
    private String title;
    private List<String> tags;
    private String username;
    private LocalDateTime beginDate;
    private LocalDateTime endDate;
    private Long beginTime;
    private Long endTime;
    private Integer sortType;
    private Integer page;
    private Integer pageSize;
}
