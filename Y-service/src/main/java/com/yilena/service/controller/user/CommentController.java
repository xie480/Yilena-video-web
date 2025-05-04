package com.yilena.service.controller.user;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.CommentQueryDTO;
import com.yilena.service.entity.po.Comment;
import com.yilena.service.log.LogOperation;
import com.yilena.service.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @LogOperation
    @PostMapping
    public Result addComment(@RequestBody Comment comment){
        log.info("添加评论：{}",comment);
        commentService.addComment(comment);
        return Result.success();
    }

    @GetMapping("/search/hotOrNew")
    public Result getCommentByList(Comment comment){
        log.info("查询评论：{}",comment);
        return Result.success(commentService.getCommentByList(comment));
    }

//    @PutMapping
//    public Result updateComment(@RequestBody Comment comment){
//        log.info("修改评论：{}",comment);
//        commentService.updateComment(comment);
//        return Result.success();
//    }

    @LogOperation
    @DeleteMapping
    public Result deleteComment(@RequestParam List<Long> ids){
        log.info("删除评论：{}", ids);
        commentService.deleteComment(ids);
        return Result.success();
    }

    @GetMapping("/query")
    public Result getCommentQuery(CommentQueryDTO commentQueryDTO){
        log.info("条件查询评论：{}",commentQueryDTO);
        return Result.success(commentService.getCommentQuery(commentQueryDTO));
    }
}
