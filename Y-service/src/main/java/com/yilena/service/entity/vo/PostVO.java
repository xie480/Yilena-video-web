package com.yilena.service.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostVO implements Serializable {
    private Long id;
    private String title;
    private String content;
    private List<String> imageUrl;
    private String imageUrlJson;
    private List<String> tags;
    private Long videoId;
    private String tagsJson;
    private Integer clicks;
    private Integer likes;
    private Integer favorites;
    private Integer shares;
    private Integer comments;
    private Long userId;
    private String username;
    private String image;
    private Integer visibility;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
    // 唯一标识
    private String symbol;
    private Integer PostType;
    private Integer type;

    private String videoUsername;
    private Long videoUserId;
    private String videoUserImage;
    private String videoCoverUrl;
    private String videoTitle;
    private String videoDescription;
    private Integer videoViews;
    private Integer videoBarrages;
    private double videoTime;
}