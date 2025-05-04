package com.yilena.service.service.impl;

import com.google.protobuf.MapEntry;
import com.yilena.service.constant.EntityConstant;
import com.yilena.service.constant.MqConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.HistoryMapper;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.dao.VideoMapper;
import com.yilena.service.entity.dto.HistoryDTO;
import com.yilena.service.entity.po.History;
import com.yilena.service.entity.po.User;
import com.yilena.service.entity.po.Video;
import com.yilena.service.entity.vo.VideoVO;
import com.yilena.service.service.HistoryService;
import com.yilena.service.utils.SnowFlake;
import lombok.RequiredArgsConstructor;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final RabbitTemplate rabbitTemplate;
    private final SnowFlake snowFlake;
    private final VideoMapper videoMapper;
    private final HistoryMapper historyMapper;
    private final UserMapper userMapper;

    @Override
    public void addHistory(Long userId, Long videoId) {
        User user = userMapper.getUserById(userId);
        if(user.getIsRecord().equals(StatusConstant.STATUS_YES)) {
            Video video = videoMapper.getVideoById(videoId);
            History history = new History();
            history.setId(snowFlake.getID());
            history.setEntityType(EntityConstant.VIDEO_TYPE);
            history.setEntityId(video.getId());
            history.setUserId(userId);
            history.setCreatedTime(LocalDateTime.now());
            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.HISTORY_BINDING_KEY, history);
        }
    }

    @Override
    public Map<String, HistoryDTO> getHistory(Long userId) {
        Map<String, HistoryDTO> history =  historyMapper.getHistory(userId);

        for (Map.Entry<String, HistoryDTO> entry : history.entrySet()) {
            HistoryDTO historyDTO = entry.getValue();
            List<Long> entityIds = historyDTO.getEntityIds();
            List<VideoVO> videoVOS = videoMapper.getVideoByIds(entityIds);
            videoVOS.forEach(videoVO -> {
                Long videoUserId = videoVO.getUserId();
                videoVO.setUsername(userMapper.getUserById(videoUserId).getUsername());
            });

            // 进行重新排序
            Map<Long, VideoVO> videoMap = videoVOS.stream()
                    .collect(Collectors.toMap(VideoVO::getId, Function.identity()));
            List<VideoVO> sortedVideos = entityIds.stream()
                    .map(videoMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            historyDTO.setVideoVOS(sortedVideos);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        history =  history.entrySet().stream()
                .sorted((e1, e2) -> {
                    LocalDate d1 = LocalDate.parse(e1.getKey(), formatter);
                    LocalDate d2 = LocalDate.parse(e2.getKey(), formatter);
                    return d2.compareTo(d1); // 降序
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, // 碰撞策略（可选）
                        LinkedHashMap::new // 有序的 Map
                ));
        return history;
    }

    @Override
    public void updateIsRecord(Long userId, Integer isRecord) {
        userMapper.updateIsRecord(userId, isRecord);
    }

    @Override
    public void deleteAllHistory(Long userId) {
        historyMapper.deleteAllHistory(userId);
    }

    @Override
    public void deleteHistory(Long id) {
        historyMapper.deleteHistory(id);
    }
}
