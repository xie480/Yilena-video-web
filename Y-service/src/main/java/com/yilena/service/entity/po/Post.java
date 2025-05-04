package com.yilena.service.entity.po;

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
public class Post implements Serializable {
    private Long id;
    private String title;
    private String content;
    private List<String> imageUrl;
    private String imageUrlJson;
    private List<String> tags;
    private String tagsJson;
    private Long videoId;
    private Integer clicks;
    private Integer likes;
    private Integer favorites;
    private Integer shares;
    private Integer comments;
    private Long userId;
    private Integer visibility;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
    private Integer PostType;
}