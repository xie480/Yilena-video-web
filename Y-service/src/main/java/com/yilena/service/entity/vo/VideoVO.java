package com.yilena.service.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoVO implements Serializable {
    private Long id;

    private Long lastId;

    private String title;

    private String description;

    private String coverUrl;

    private List<String> tags;
    private Integer sort;
    private Integer barrages;
    private Integer views;
    private Integer visibility;
    private Integer favorites;

    private Long time;

    private Long userId;

    private String username;

    private LocalDateTime createdTime;

    private Integer type;
}