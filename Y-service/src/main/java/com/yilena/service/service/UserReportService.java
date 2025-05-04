package com.yilena.service.service;

import java.util.List;
import java.util.Map;

public interface UserReportService {
    List<Map<String,Object>> getUserReportFollowers(Long userId, Integer type);

    List<Map<String,Object>> getUserReportLikes(Long userId, Integer type);

    List<Map<String,Object>> getUserReportFavorites(Long userId, Integer type);

    List<Map<String,Object>> getUserReportCoins(Long userId, Integer type);

    List<Map<String,Object>> getUserReportComments(Long userId, Integer type);
}
