package com.yilena.service.entity.dto;

import com.github.pagehelper.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoritesFolderVideosDTO {
    private Long id;
    private String title;
    private List<Long> videoIds;
    private Integer page;
    private Integer pageSize;
}
