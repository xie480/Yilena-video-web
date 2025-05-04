package com.yilena.service.entity.dto;

import com.yilena.service.entity.vo.PostVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private List<String> tags;
    private String username;
    private Integer page;
    private Integer pageSize;
}
