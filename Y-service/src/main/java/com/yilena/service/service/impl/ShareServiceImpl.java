package com.yilena.service.service.impl;

import com.yilena.service.constant.EntityConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.PostMapper;
import com.yilena.service.dao.ShareMapper;
import com.yilena.service.dao.VideoMapper;
import com.yilena.service.entity.po.Post;
import com.yilena.service.entity.po.Share;
import com.yilena.service.entity.po.Video;
import com.yilena.service.service.ShareService;
import com.yilena.service.utils.SnowFlake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final ShareMapper shareMapper;
    private final VideoMapper videoMapper;
    private final PostMapper postMapper;
    private final SnowFlake snowFlake;

    @Transactional
    @Override
    public Share getIsShare(Share share) {
        return shareMapper.getIsShare(share);
    }

    public void doShare(Share share) {
        if(share.getEntityType().equals(EntityConstant.VIDEO_TYPE)){
            Video video = videoMapper.getVideoById(share.getEntityId());
            if(video == null){
                throw new RuntimeException(StatusConstant.VIDEO_NOT_EXIST);
            }
            share.setId(snowFlake.getID());
            share.setUserId(video.getUserId());
            share.setEntityType(EntityConstant.VIDEO_TYPE);
            share.setEntityId(video.getId());
            share.setCreatedTime(LocalDateTime.now());

            shareMapper.addShare(share);

            video.setShares(video.getShares()+1);
            videoMapper.updateVideo(video);
        }else if(share.getEntityType().equals(EntityConstant.POST_TYPE)){
            Post post = postMapper.getPostById(share.getEntityId());
            if(post == null){
                throw new RuntimeException(StatusConstant.POST_NOT_EXIST);
            }
            share.setId(snowFlake.getID());
            share.setUserId(post.getUserId());
            share.setEntityType(EntityConstant.POST_TYPE);
            share.setEntityId(post.getId());
            share.setCreatedTime(LocalDateTime.now());

            shareMapper.addShare(share);

            post.setShares(post.getShares()+1);
            postMapper.updatePost(post);
        }
    }
}
