package com.yilena.service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DanmuDTO {
    private Long videoId;
    private double currentTime;
    private Integer pageSize;
}
