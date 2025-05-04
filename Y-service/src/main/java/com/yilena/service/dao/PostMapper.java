package com.yilena.service.dao;

import com.yilena.service.entity.po.Post;
import com.yilena.service.entity.vo.PostVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostMapper {
    void addPost(Post post);

    Post getPostById(Long id);

    void updatePost(Post post);

    void deletePost(Long id);

    List<PostVO> getPostByUserIds(List<Long> followingIds);

    List<PostVO> getMyPost(Long userId);

    List<PostVO> getPostByUserId(Long userId);
}
