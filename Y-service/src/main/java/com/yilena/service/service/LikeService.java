package com.yilena.service.service;

import com.yilena.service.entity.po.Like;

public interface LikeService {
    void doLike(Like like);

    void undoLike(Like like);

    Like isLike(Like like);
}
