package com.yilena.service.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageShowVO {
    private Long id;
    private String content;
    private Long sendUserId;
    private Long receiveUserId;
    private Integer type;

    private Long UserId;
    private String username;
    private String image;

    private LocalDateTime  createdTime;
}
