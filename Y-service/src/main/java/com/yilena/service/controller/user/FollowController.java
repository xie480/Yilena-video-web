package com.yilena.service.controller.user;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.FollowDTO;
import com.yilena.service.entity.dto.FollowPageDTO;
import com.yilena.service.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/follow")
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public Result follow(@RequestBody FollowDTO followDTO){
        log.info("用户{}关注了用户{}", followDTO.getFollowerId(), followDTO.getFollowedId());
        followService.follow(followDTO);
        return Result.success();
    }

    @DeleteMapping("/{followedId}/{followerId}/{isSpecial}")
    public Result unfollow(@PathVariable Long followedId, @PathVariable Long followerId, @PathVariable Integer isSpecial){
        log.info("用户{}取消关注了用户{}", followerId, followedId);
        FollowDTO followDTO = FollowDTO.builder().followedId(followedId).followerId(followerId).isSpecial(isSpecial).build();
        followService.unfollow(followDTO);
        return Result.success();
    }

    @GetMapping("/search/page")
    public Result getFollowByPage(FollowPageDTO followPageDTO){
        log.info("用户{}查询了关注列表", followPageDTO.getId());
        return Result.success(followService.getFollowByPage(followPageDTO));
    }

    @GetMapping("/{userId}/{authorId}")
    public Result isFollow(@PathVariable Long userId, @PathVariable Long authorId){
        log.info("用户{}查询了用户{}的关注状态", userId, authorId);
        return Result.success(followService.isFollow(userId, authorId));
    }
}
