package com.yilena.service.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoritesFolderVO implements Serializable {
    private Long id;
    private String title;
    private Long userId;
    private String coverUrl;
    private Integer visibility;
    private Integer videoCount;
    private Integer isDefault;
    private Integer isCoverAuto;
    private Integer clicks;
}
