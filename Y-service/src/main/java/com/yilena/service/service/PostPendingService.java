package com.yilena.service.service;

import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.PostPendingDTO;
import com.yilena.service.entity.dto.PostPendingPageQueryDTO;
import com.yilena.service.entity.vo.PostVO;

public interface PostPendingService {
    void updatePostPendingStatus(PostPendingDTO postPendingDTO);

    PageResult<PostVO> getPostPendingByPage(PostPendingPageQueryDTO postPendingPageQueryDTO);
}
