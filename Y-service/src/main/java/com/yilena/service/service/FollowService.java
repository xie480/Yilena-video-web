package com.yilena.service.service;

import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.FollowDTO;
import com.yilena.service.entity.dto.FollowPageDTO;
import com.yilena.service.entity.po.UserFollowing;
import com.yilena.service.entity.vo.UserVO;

public interface FollowService {
    void follow(FollowDTO followDTO);

    void unfollow(FollowDTO followDTO);

    PageResult<UserVO> getFollowByPage(FollowPageDTO followPageDTO);

    UserFollowing isFollow(Long userId, Long authorId);
}
