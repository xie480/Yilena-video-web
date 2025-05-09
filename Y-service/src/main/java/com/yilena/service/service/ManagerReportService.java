package com.yilena.service.service;

import java.util.Map;

public interface ManagerReportService {
    Map<String,Long> getVideoReportStatus();

    Long getTotalUser();

    Long getTotalLike();

    Long getTotalPost();

    Long getTotalComment();

    Long getTotalFavorite();
}
