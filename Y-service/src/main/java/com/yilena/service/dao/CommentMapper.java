package com.yilena.service.dao;

import com.yilena.service.entity.po.Comment;
import com.yilena.service.entity.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommentMapper {
    void addComment(Comment comment);

    Comment getCommentById(Long entityId);

    void updateComment(Comment comment1);

    List<CommentVO> getCommentByHot(Comment comment);

    List<CommentVO> getCommentByNew(Comment comment);

    void deleteComment(Long id);

    List<Comment> getCommentByLastId(Long lastId);

    Comment getCommentByLastIdAndUserId(Long lastId, Long userId);

    List<Comment> getCommentsQueryByNewSortType(List<Long> videoIds);

    List<Comment> getCommentsQueryByLikeSortType(List<Long> videoIds);

    List<Comment> getCommentsQueryByMostCommentsSortType(List<Long> videoIds);

    List<Comment> getCommentByIds(List<Long> ids);

    void deleteCommentByIds(List<Long> ids);

    Long getTotalComment();
}
