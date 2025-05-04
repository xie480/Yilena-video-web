package com.yilena.service.dao;

import com.yilena.service.entity.po.Like;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface LikeMapper {
    void doLike(Like like);

    void undoLike(Like like);

    Like isLike(Like like);
}
