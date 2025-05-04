package com.yilena.service.dao;

import com.yilena.service.entity.po.UserFollowing;
import com.yilena.service.entity.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FollowMapper {
    void addFollow(UserFollowing userFollowing);

    void updateSpecialFollow(UserFollowing userFollowing);

    UserFollowing getById(Long followerId, Long followedId);

    void deleteFollow(UserFollowing userFollowing);

    List<Long> getFollowingIds(Long id);

    UserFollowing isFollow(Long followerId, Long followedId);

    List<UserVO> getFollowingUsers(Long userId);

    List<Long> getFollowedIds(Long userId);
}
