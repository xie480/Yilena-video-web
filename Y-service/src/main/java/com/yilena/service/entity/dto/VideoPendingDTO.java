package com.yilena.service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPendingDTO implements Serializable {
    private Long id;
    private String title;
    private Long userId;
    private LocalDateTime beginTime;
    private LocalDateTime endTime;
    private Integer status;
    private Integer page;
    private Integer pageSize;
}
