package com.yilena.service.service;

import java.util.List;
import java.util.Map;

public interface ManagerReportService {
    Map<String,Long> getVideoReportStatus();

    Long getTotalUser();

    Long getTotalLike();

    Long getTotalPost();

    Long getTotalComment();

    Long getTotalFavorite();

    List<Map<String,Object>> getVideoUploadReport(Integer type);

    List<Map<String,Object>> getPostUploadReport(Integer type);

    List<Map<String,Object>> getUserReport(Integer type);

    List<Map<String,Object>> getVideoUndoReport(Integer type);
}
