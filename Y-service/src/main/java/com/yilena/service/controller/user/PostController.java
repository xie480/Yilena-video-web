package com.yilena.service.controller.user;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.PostDTO;
import com.yilena.service.entity.po.Post;
import com.yilena.service.log.LogOperation;
import com.yilena.service.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    @CacheEvict(value = "postWhichFollowing", key = "#post.userId")
    @LogOperation
    @PostMapping
    public Result addPost(@RequestBody Post post) {
        log.info("添加动态");
        postService.addPost(post);
        return Result.success();
    }

//    @GetMapping("/search/page")
//    public Result getPostByPage(PostDTO postDTO) {
//        log.info("分页获取动态");
//        return Result.success(postService.getPostByPage(postDTO));
//    }

    @Cacheable(value = "post", key = "#id")
    @GetMapping("/search/{id}")
    public Result getPostById(@PathVariable Long id) {
        log.info("根据id获取动态");
        return Result.success(postService.getPostById(id));
    }

    @CacheEvict(value = "postWhichFollowing", key = "#post.userId")
    @LogOperation
    @PutMapping
    public Result updatePost(@RequestBody Post post) {
        log.info("更新动态");
        postService.updatePost(post);
        return Result.success();
    }

    @Cacheable(value = "postWhichFollowing", key = "#id")
    @LogOperation
    @DeleteMapping
    public Result deletePost(Long id) {
        log.info("删除动态");
        postService.deletePost(id);
        return Result.success();
    }

    @Cacheable(value = "postWhichFollowing", key = "#userId")
    @GetMapping("/following/{userId}")
    public Result getPostByFollowingAndMine(@PathVariable Long userId) {
        log.info("获取关注用户的动态");
        return Result.success(postService.getPostByFollowing(userId));
    }

    @GetMapping("/myPosts")
    public Result getMyPosts() {
        log.info("获取我的动态");
        return Result.success(postService.getMyPosts());
    }

    @GetMapping("/search/byUserId/{userId}")
    public Result getPostByUserId(@PathVariable Long userId) {
        log.info("获取指定用户动态");
        return Result.success(postService.getPostByUserId(userId));
    }
}
