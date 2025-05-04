package com.yilena.service.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Manager {
    private Integer id;
    private String username;
    private String password;
    private Integer status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
