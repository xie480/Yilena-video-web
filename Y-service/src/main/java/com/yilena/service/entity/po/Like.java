package com.yilena.service.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Like {
    private Long id;
    private Long entityId;
    public Integer entityType;
    private Long userId;
    private LocalDateTime createdTime;
}
