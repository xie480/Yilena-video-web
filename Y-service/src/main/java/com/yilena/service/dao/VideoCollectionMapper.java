package com.yilena.service.dao;

import com.yilena.service.entity.po.VideoCollection;
import com.yilena.service.entity.vo.VideoVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VideoCollectionMapper {
    void addVideoCollection(VideoCollection videoCollection);

    List<VideoCollection> getVideoCollectionByList(Long userId);

    VideoCollection getVideoCollectionById(Long id);

    void updateVideoCollection(VideoCollection videoCollection1);

    void deleteVideoCollection(Long id);
}
