package com.yilena.service.service;

import com.yilena.service.entity.dto.CommentQueryDTO;
import com.yilena.service.entity.po.Comment;
import com.yilena.service.entity.vo.CommentVO;

import java.util.List;

public interface CommentService {
    void addComment(Comment comment);

    List<CommentVO> getCommentByList(Comment comment);

    void updateComment(Comment comment);

    void deleteComment(List<Long> id);

    List<Comment> getCommentQuery(CommentQueryDTO commentQueryDTO);
}
