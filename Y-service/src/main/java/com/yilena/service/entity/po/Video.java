package com.yilena.service.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Video implements Serializable {
    private Long id;
    private Long lastId;
    private String title;
    private String description;
    private String videoUrl;
    private String coverUrl;
    private List<String> tags; // JSON转换
    private String tagsJson;
    private Integer sort;
    private Integer barrages;
    private Integer views;
    private Integer likes;
    private Integer favorites;
    private Integer shares;
    private Integer coins;
    private Integer comments;
    private Long userId;
    private Long collectionId;
    private String collectionName;
    private Integer visibility;
    private Long time;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}