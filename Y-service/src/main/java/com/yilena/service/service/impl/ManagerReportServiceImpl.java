package com.yilena.service.service.impl;

import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.*;
import com.yilena.service.service.ManagerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ManagerReportServiceImpl implements ManagerReportService {

    private final VideoPendingMapper videoPendingMapper;
    private final VideoMapper videoMapper;
    private final UserMapper userMapper;
    private final LikeMapper likeMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final VideoFavoritesMapper videoFavoritesMapper;

    @Override
    public Map<String, Long> getVideoReportStatus() {
        Map<String, Long> videoCount = new HashMap<>();
        videoCount.put("no", videoPendingMapper.getVideoCountByStatus(StatusConstant.STATUS_NO));
        videoCount.put("wait",  videoPendingMapper.getVideoCountByStatus(StatusConstant.STATUS_WAIT));
        videoCount.put("yes",videoMapper.getVideoCount());
        Long total = videoCount.get("no") + videoCount.get("wait") + videoCount.get("yes");
        videoCount.put("total", total);
        return videoCount;
    }

    @Override
    public Long getTotalUser() {
        return userMapper.getTotalUser();
    }

    @Override
    public Long getTotalLike() {
        return likeMapper.getTotalLike();
    }

    @Override
    public Long getTotalPost() {
        return postMapper.getTotalPost();
    }

    @Override
    public Long getTotalComment() {
        return commentMapper.getTotalComment();
    }

    @Override
    public Long getTotalFavorite() {
        return videoFavoritesMapper.getTotalFavorite();
    }
}
