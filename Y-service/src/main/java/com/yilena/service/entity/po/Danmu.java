package com.yilena.service.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Danmu implements Serializable {
    private String content;    // 弹幕内容
    private double videoTime;  // 视频时间戳（秒）
    private String username;     // 用户名
    private String videoId;    // 视频id
    private String color;      // 弹幕颜色
    private Long createTime; // 创建时间
}
