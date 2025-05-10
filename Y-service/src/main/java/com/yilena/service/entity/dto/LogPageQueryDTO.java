package com.yilena.service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogPageQueryDTO {
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Integer page;
    private Integer pageSize;
    private Integer type;
}
