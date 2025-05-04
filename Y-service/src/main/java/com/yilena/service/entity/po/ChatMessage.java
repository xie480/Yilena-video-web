package com.yilena.service.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private Long id;
    private String content;
    private Long sendUserId;
    private Long receiveUserId;
    private List<Long> receiveUserIds;
    private Integer type;
    private Integer visibilityBySender;
    private Integer visibilityByReceiver;
    private String imageUrl;
    // 0:未读 1:已读
    private Integer status;
    private Integer isExpired;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
