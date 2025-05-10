package com.yilena.service.service.impl;

import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.*;
import com.yilena.service.service.ManagerReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class ManagerReportServiceImpl implements ManagerReportService {

    private final VideoPendingMapper videoPendingMapper;
    private final VideoMapper videoMapper;
    private final UserMapper userMapper;
    private final LikeMapper likeMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final VideoFavoritesMapper videoFavoritesMapper;

    @Override
    public Map<String, Long> getVideoReportStatus() {
        Map<String, Long> videoCount = new HashMap<>();
        videoCount.put("no", videoPendingMapper.getVideoCountByStatus(StatusConstant.STATUS_NO));
        videoCount.put("wait",  videoPendingMapper.getVideoCountByStatus(StatusConstant.STATUS_WAIT));
        videoCount.put("yes",videoMapper.getVideoCount());
        Long total = videoCount.get("no") + videoCount.get("wait") + videoCount.get("yes");
        videoCount.put("total", total);
        return videoCount;
    }

    @Override
    public Long getTotalUser() {
        return userMapper.getTotalUser();
    }

    @Override
    public Long getTotalLike() {
        return likeMapper.getTotalLike();
    }

    @Override
    public Long getTotalPost() {
        return postMapper.getTotalPost();
    }

    @Override
    public Long getTotalComment() {
        return commentMapper.getTotalComment();
    }

    @Override
    public Long getTotalFavorite() {
        return videoFavoritesMapper.getTotalFavorite();
    }

    private static final Map<Integer, Integer> TYPE_DAYS = Map.of(
            1, 7,
            2, 30,
            3, 90
    );

    private List<LocalDateTime> generateDatePoints(LocalDateTime startDate, int totalDays) {
        int interval = (int) Math.ceil((double) totalDays / 7);
        List<LocalDateTime> points = new ArrayList<>(List.of(startDate));

        IntStream.range(1, 8)
                .forEach(i -> points.add(points.get(i-1).plusDays(interval)));

        // 确保最后一个点不超过今天
        points.replaceAll(date -> date.isAfter(LocalDateTime.now()) ? LocalDateTime.now() : date);
        return points.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getVideoUploadReport(Integer type) {
        if (!TYPE_DAYS.containsKey(type)) throw new RuntimeException("数据错误！");

        int totalDays = TYPE_DAYS.get(type);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(totalDays - 1);

        // 生成7个时间段的日期分割点
        List<LocalDateTime> datePoints = generateDatePoints(startDate, totalDays);

        return IntStream.range(0, datePoints.size() - 1)
                .mapToObj(i -> {
                    LocalDateTime periodStart = datePoints.get(i);
                    LocalDateTime periodEnd = datePoints.get(i + 1).minusDays(1);
                    Integer count = getVideoUploadReportCount(periodStart, periodEnd);
                    return Map.<String, Object>of(
                            "date", periodEnd,
                            "count", Optional.ofNullable(count).orElse(0)
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getPostUploadReport(Integer type) {
        if (!TYPE_DAYS.containsKey(type)) throw new RuntimeException("数据错误！");

        int totalDays = TYPE_DAYS.get(type);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(totalDays - 1);

        // 生成7个时间段的日期分割点
        List<LocalDateTime> datePoints = generateDatePoints(startDate, totalDays);

        return IntStream.range(0, datePoints.size() - 1)
                .mapToObj(i -> {
                    LocalDateTime periodStart = datePoints.get(i);
                    LocalDateTime periodEnd = datePoints.get(i + 1).minusDays(1);
                    Integer count = getPostUploadReportCount(periodStart, periodEnd);
                    return Map.<String, Object>of(
                            "date", periodEnd,
                            "count", Optional.ofNullable(count).orElse(0)
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getUserReport(Integer type) {
        if (!TYPE_DAYS.containsKey(type)) throw new RuntimeException("数据错误！");

        int totalDays = TYPE_DAYS.get(type);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(totalDays - 1);

        // 生成7个时间段的日期分割点
        List<LocalDateTime> datePoints = generateDatePoints(startDate, totalDays);

        return IntStream.range(0, datePoints.size() - 1)
                .mapToObj(i -> {
                    LocalDateTime periodStart = datePoints.get(i);
                    LocalDateTime periodEnd = datePoints.get(i + 1).minusDays(1);
                    Integer count = getUserReportCount(periodStart, periodEnd);
                    return Map.<String, Object>of(
                            "date", periodEnd,
                            "count", Optional.ofNullable(count).orElse(0)
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getVideoUndoReport(Integer type) {
        if (!TYPE_DAYS.containsKey(type)) throw new RuntimeException("数据错误！");

        int totalDays = TYPE_DAYS.get(type);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(totalDays - 1);

        // 生成7个时间段的日期分割点
        List<LocalDateTime> datePoints = generateDatePoints(startDate, totalDays);

        return IntStream.range(0, datePoints.size() - 1)
                .mapToObj(i -> {
                    LocalDateTime periodStart = datePoints.get(i);
                    LocalDateTime periodEnd = datePoints.get(i + 1).minusDays(1);
                    Integer count = getVideoUndoReportCount(periodStart, periodEnd);
                    return Map.<String, Object>of(
                            "date", periodEnd,
                            "count", Optional.ofNullable(count).orElse(0)
                    );
                })
                .collect(Collectors.toList());
    }

    private Integer getVideoUploadReportCount(LocalDateTime start, LocalDateTime end) {
        return videoMapper.getVideoUploadReport(
                start.withHour(0).withMinute(0).withSecond(0).withNano(0) ,
                end.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)   // 包含结束日期的数据
        );
    }

    private Integer getPostUploadReportCount(LocalDateTime start, LocalDateTime end) {
        return postMapper.getPostUploadReport(
                start.withHour(0).withMinute(0).withSecond(0).withNano(0) ,
                end.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)   // 包含结束日期的数据
        );
    }

    private Integer getUserReportCount(LocalDateTime start, LocalDateTime end) {
        return userMapper.getUserReport(
                start.withHour(0).withMinute(0).withSecond(0).withNano(0) ,
                end.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)   // 包含结束日期的数据
        );
    }

    private Integer getVideoUndoReportCount(LocalDateTime start, LocalDateTime end) {
        return videoPendingMapper.getVideoUndoReport(
                start.withHour(0).withMinute(0).withSecond(0).withNano(0) ,
                end.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)   // 包含结束日期的数据
        );
    }
}
