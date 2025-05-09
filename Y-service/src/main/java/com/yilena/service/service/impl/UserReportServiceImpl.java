package com.yilena.service.service.impl;

import com.yilena.service.dao.*;
import com.yilena.service.service.UserReportService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class UserReportServiceImpl implements UserReportService {

    private final VideoMapper videoMapper;

    private static final Map<Integer, Integer> TYPE_DAYS = Map.of(
            1, 7,
            2, 30,
            3, 90
    );

    public List<Map<String, Object>> getUserReportFollowers(Long userId, Integer type) {
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
                    Integer count = getPeriodFollowersCount(userId, periodStart, periodEnd);
                    return Map.<String, Object>of(
                            "date", periodEnd,
                            "count", Optional.ofNullable(count).orElse(0)
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getUserReportLikes(Long userId, Integer type) {
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
                    Integer count = getPeriodLikesCount(userId, periodStart, periodEnd);
                    return Map.<String, Object>of(
                            "date", periodEnd,
                            "count", Optional.ofNullable(count).orElse(0)
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getUserReportFavorites(Long userId, Integer type) {
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
                    Integer count = getPeriodFavoritesCount(userId, periodStart, periodEnd);
                    return Map.<String, Object>of(
                            "date", periodEnd,
                            "count", Optional.ofNullable(count).orElse(0)
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getUserReportCoins(Long userId, Integer type) {
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
                    Integer count = getPeriodCoinsCount(userId, periodStart, periodEnd);
                    return Map.<String, Object>of(
                            "date", periodEnd,
                            "count", Optional.ofNullable(count).orElse(0)
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getUserReportComments(Long userId, Integer type) {
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
                    Integer count = getPeriodCommentsCount(userId, periodStart, periodEnd);
                    return Map.<String, Object>of(
                            "date", periodEnd,
                            "count", Optional.ofNullable(count).orElse(0)
                    );
                })
                .collect(Collectors.toList());
    }

    private List<LocalDateTime> generateDatePoints(LocalDateTime startDate, int totalDays) {
        int interval = (int) Math.ceil((double) totalDays / 7);
        List<LocalDateTime> points = new ArrayList<>(List.of(startDate));

        IntStream.range(1, 8)
                .forEach(i -> points.add(points.get(i-1).plusDays(interval)));

        // 确保最后一个点不超过今天
        points.replaceAll(date -> date.isAfter(LocalDateTime.now()) ? LocalDateTime.now() : date);
        return points.stream().distinct().collect(Collectors.toList());
    }

    private Integer getPeriodFollowersCount(Long userId, LocalDateTime start, LocalDateTime end) {
        return videoMapper.getFollowingFromReportCount(
                userId,
                start.withHour(0).withMinute(0).withSecond(0).withNano(0) ,
                end.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)   // 包含结束日期的数据
        );
    }

    private Integer getPeriodLikesCount(Long userId, LocalDateTime start, LocalDateTime end) {
        return videoMapper.getLikeFromReportCount(
                userId,
                start.withHour(0).withMinute(0).withSecond(0).withNano(0) ,
                end.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)   // 包含结束日期的数据
        );
    }

    private Integer getPeriodFavoritesCount(Long userId, LocalDateTime start, LocalDateTime end) {
        return videoMapper.getFavoriteFromReportCount(
                userId,
                start.withHour(0).withMinute(0).withSecond(0).withNano(0) ,
                end.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)   // 包含结束日期的数据
        );
    }

    private Integer getPeriodCoinsCount(Long userId, LocalDateTime start, LocalDateTime end) {
        return videoMapper.getCoinFromReportCount(
                userId,
                start.withHour(0).withMinute(0).withSecond(0).withNano(0) ,
                end.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)   // 包含结束日期的数据
        );
    }

    private Integer getPeriodCommentsCount(Long userId, LocalDateTime start, LocalDateTime end) {
        return videoMapper.getCommentFromReportCount(
                userId,
                start.withHour(0).withMinute(0).withSecond(0).withNano(0) ,
                end.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)   // 包含结束日期的数据
        );
    }
}
