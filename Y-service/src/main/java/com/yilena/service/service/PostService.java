package com.yilena.service.service;

import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.PostDTO;
import com.yilena.service.entity.po.Post;
import com.yilena.service.entity.vo.PostVO;

import java.util.List;

public interface PostService {
    void addPost(Post post);

    PageResult<PostVO> getPostByPage(PostDTO postDTO);

    Post getPostById(Long id);

    void updatePost(Post post);

    void deletePost(Long id);

    List<PostVO> getPostByFollowing(Long userId);

    List<PostVO> getMyPosts();

    List<PostVO> getPostByUserId(Long userId);
}
