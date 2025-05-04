package com.yilena.service.service;

import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.VideoPendingDTO;
import com.yilena.service.entity.dto.VideoPendingStatusDTO;
import com.yilena.service.entity.po.VideoPending;

import java.util.List;

public interface VideoPendingService {
    PageResult<VideoPending> getVideoPendingByPage(VideoPendingDTO videoPendingDTO);

    VideoPending getVideoPendingById(Long id);

    void updateVideoPendingStatus( VideoPendingStatusDTO videoPendingStatusDTO);

    void updateVideoPendingFromVideo(VideoPendingStatusDTO videoPendingStatusDTO);

    List<VideoPending> getVideoPendingByStatus(Long userId, Integer status);

    void updateVideoPending(VideoPending videoPending);

    void deleteVideoPending(Long id);

    void reuploadVideoPending(Long id);
}
