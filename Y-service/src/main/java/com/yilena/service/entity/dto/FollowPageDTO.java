package com.yilena.service.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowPageDTO {
    private Long Id;
    private String username;
    private Integer page;
    private Integer pageSize;
}
