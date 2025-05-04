package com.yilena.service.service;

import com.yilena.service.entity.dto.VideoCollectionDoOrUndoDTO;
import com.yilena.service.entity.po.VideoCollection;
import com.yilena.service.entity.vo.VideoVO;

import java.util.List;

public interface VideoCollectionService {
    void addVideoCollection(VideoCollection videoCollection);

    List<VideoCollection> getVideoCollectionByList(Long userId,Long currentUserId);

    void updateVideoCollection(VideoCollection videoCollection);

    void deleteVideoCollection(Long id);

    void doVideoCollection(VideoCollectionDoOrUndoDTO videoCollectionDoOrUndo);

    void undoVideoCollection(VideoCollectionDoOrUndoDTO videoCollectionDoOrUndo);

    List<VideoVO> getVideoIdsByCollectionId(Long userId, Long collectionId);
}
