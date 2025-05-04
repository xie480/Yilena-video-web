package com.yilena.service.entity.vo;

import com.yilena.service.constant.StatusConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    private Long id;
    @Field(type = FieldType.Keyword)
    private String username;
    private String description;
    private Integer videoCount;
    private String image;
    private Integer followingCount;
    private Integer followersCount;
    private Integer status;
    private Integer type;

    private Long unReadMessageCount;
}
