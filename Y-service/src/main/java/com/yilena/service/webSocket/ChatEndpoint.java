package com.yilena.service.webSocket;

import com.alibaba.fastjson.JSON;
import com.yilena.service.constant.MessageTypeConstant;
import com.yilena.service.constant.MqConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.entity.po.ChatMessage;
import com.yilena.service.utils.SnowFlake;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;


@Slf4j
@ServerEndpoint("/YVW/chat/{userId}")
@Component
public class ChatEndpoint {
    private static RedisTemplate<String, Object> redisTemplate;
    private static RabbitTemplate rabbitTemplate;

    @Autowired
    private SnowFlake snowFlake;

    // 必须显式定义无参构造函数
    public ChatEndpoint() {
        log.info("ChatEndpoint无参构造函数被调用");
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        ChatEndpoint.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        ChatEndpoint.rabbitTemplate = rabbitTemplate;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userIdStr) {
        Long userId = Long.valueOf(userIdStr);
        session.getUserProperties().put("userId", userId); // 存储用户ID到Session属性
        WebSocketSessionManager.addSession(Long.valueOf(userId), session);
        log.info("用户 {} 连接建立，当前在线人数：{}", userId, WebSocketSessionManager.getOnlineCount());
    }

    @OnMessage
    public void onMessage(String chatMessageJson, @PathParam("userId") String sid){
        log.info("用户 {} 发送消息：{}", sid, chatMessageJson);
    }


    public void sendMessage(String chatMessageJson) {
        ChatMessage message = JSON.parseObject(chatMessageJson, ChatMessage.class);
        message.setIsExpired(StatusConstant.STATUS_NO);
        log.info("用户{}发送消息：{}，给{}", message.getSendUserId(), message.getContent(), message.getReceiveUserId());

        if(Objects.equals(message.getType(), MessageTypeConstant.VIDEO)){
            message.getReceiveUserIds().forEach(receiveUserId -> {
                message.setId(snowFlake.getID());
                message.setReceiveUserId(receiveUserId);
                Session receiverSession = WebSocketSessionManager.getSession(receiveUserId);
                if (receiverSession != null) {
                    receiverSession.getAsyncRemote().sendText(chatMessageJson);
                }
                // 发送消息到RabbitMQ
                rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.CHAT_BINDING_KEY, message);
            });
        }else {
            // 转发给接收者
            Session receiverSession = WebSocketSessionManager.getSession(message.getReceiveUserId());
            if (receiverSession != null) {
                receiverSession.getAsyncRemote().sendText(chatMessageJson);
            }

                if(!Objects.equals(message.getStatus(),StatusConstant.STATUS_WAIT)){
                // 离线时按发送方维度存储
                String redisKey = "offline:" + message.getReceiveUserId() + ":" + message.getSendUserId();
                redisTemplate.opsForList().rightPush(redisKey, chatMessageJson);
                // 设置过期时间为一天
                redisTemplate.expire(redisKey, Duration.ofDays(1));
                }

        }

        if (!Objects.equals(message.getStatus(), StatusConstant.STATUS_WAIT) && !Objects.equals(message.getType(), MessageTypeConstant.VIDEO) ) {
            // 发送消息到RabbitMQ
            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.CHAT_BINDING_KEY, message);
        }

    }

    @OnClose
    public void onClose(Session session) {
        Long userId = (Long) session.getUserProperties().get("userId");
        WebSocketSessionManager.removeSession(userId);
        log.info("用户 {} 连接关闭，剩余在线人数：{}", userId, WebSocketSessionManager.getOnlineCount());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        Integer userId = (Integer) session.getUserProperties().get("userId");
        log.error("用户 {} 的WebSocket发生错误：{}", userId, error.getMessage());
    }
}
