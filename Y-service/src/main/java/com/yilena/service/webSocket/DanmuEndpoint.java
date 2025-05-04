package com.yilena.service.webSocket;

import com.alibaba.fastjson.JSON;
import com.yilena.service.constant.MqConstant;
import com.yilena.service.entity.po.Danmu;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@ServerEndpoint("/YVW/danmu/{videoId}/{userId}")
@Component
public class DanmuEndpoint {
    private static RedisTemplate<String, Object> redisTemplate;
    private static RabbitTemplate rabbitTemplate;
    
    // 会话管理 {videoId: sessionSet}
    private static final ConcurrentHashMap<String, CopyOnWriteArraySet<Session>> videoSessions = new ConcurrentHashMap<>();
    
    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        DanmuEndpoint.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        DanmuEndpoint.rabbitTemplate = rabbitTemplate;
    }

    @OnOpen
    public void onOpen(Session session, 
                      @PathParam("videoId") String videoId,
                      @PathParam("userId") Long userId) throws IOException {
        session.getUserProperties().put("videoId", videoId);
        session.getUserProperties().put("userId", userId);
        
        videoSessions.computeIfAbsent(videoId, k -> new CopyOnWriteArraySet<>())
                    .add(session);

        // 获取当前在线人数
        int currentUsers = videoSessions.get(videoId).size();

        // 构造 JSON 消息
        String message = String.format("{\"type\": \"userCount\", \"count\": %d}", currentUsers);

        // 发送给当前连接的客户端
        session.getBasicRemote().sendText(message);

        log.info("视频 {} 新增观众 {}, 当前在线: {}", videoId, userId, currentUsers);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到消息: {}", message);
        Danmu danmu = JSON.parseObject(message, Danmu.class);
        // 广播给同视频观众
        broadcastMessage(danmu.getVideoId(), JSON.toJSONString(danmu));

        // 存储到Redis有序集合（按视频时间排序）
        String danmuKey = "videoDanmu:" + danmu.getVideoId();
        redisTemplate.opsForZSet().add(danmuKey, message, danmu.getVideoTime());
        // 设置过期时间，默认为7天
        redisTemplate.expire(danmuKey, Duration.ofDays(7));
    }

    private void broadcastMessage(String videoId, String message) {
        videoSessions.getOrDefault(videoId, new CopyOnWriteArraySet<>())
                    .parallelStream()
                    .filter(Session::isOpen)
                    .forEach(session -> {
                        session.getAsyncRemote().sendText(message);
                    });
    }

    @OnClose
    public void onClose(Session session) {
        String videoId = (String) session.getUserProperties().get("videoId");
        videoSessions.get(videoId).remove(session);
        log.info("视频 {} 观众退出，剩余在线: {}", videoId, 
                videoSessions.get(videoId).size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        String videoId = (String) session.getUserProperties().get("videoId");
        log.error("视频 {} 的WebSocket发生异常: {}", videoId, error.getMessage());
        videoSessions.get(videoId).remove(session);
    }
}