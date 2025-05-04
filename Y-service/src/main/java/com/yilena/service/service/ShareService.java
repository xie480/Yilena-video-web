package com.yilena.service.service;

import com.yilena.service.entity.po.Share;

public interface ShareService {
    Share getIsShare(Share share);

    void doShare(Share share);
}
