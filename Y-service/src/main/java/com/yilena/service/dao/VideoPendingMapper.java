package com.yilena.service.dao;

import com.yilena.service.entity.dto.VideoPendingDTO;
import com.yilena.service.entity.po.Video;
import com.yilena.service.entity.po.VideoPending;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VideoPendingMapper {
    void updateVideoPending(VideoPending video);

    void addVideoPending(Video video);

    void deleteVideoPending(Long id);

    List<VideoPending> getVideoPendingByPage(VideoPendingDTO videoPendingDTO);

    VideoPending getVideoPendingById(Long id);

    List<VideoPending> getVideoPendingByStatus(Long userId, Integer status);
}
