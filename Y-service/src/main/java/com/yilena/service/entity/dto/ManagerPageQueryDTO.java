package com.yilena.service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManagerPageQueryDTO {
    private String username;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Integer status;
    private Integer page;
    private Integer pageSize;
}
