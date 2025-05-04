package com.yilena.service.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoritesFolder {
    private Long id;
    private String title;
    private List<Long> videoIds; // JSON转换
    private String videoIdsJson;
    private Long userId;
    private String coverUrl;
    private Integer isCoverAuto;
    private Integer visibility;
    private Integer videoCount;
    private Integer clicks;
    private List<Long> subscribers; // JSON转换
    private String subscribersJson;
    private Integer isDefault;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}