package com.yilena.service.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable{
    private Long id;
    private String username;
    private String oldPassword;
    private String password;
    private String image;
    private String description;
    private Integer followingCount;
    private Integer followersCount;
    private Integer postCount;
    private Integer videoCount;
    private Integer coins;
    private Integer status;
    private String reason;
    private Integer isRecord;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}