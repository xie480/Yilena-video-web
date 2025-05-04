package com.yilena.service.webSocket;

import jakarta.websocket.Session;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private static final ConcurrentHashMap<Long, Session> onlineUsers = new ConcurrentHashMap<>();

    public static void addSession(Long userId, Session session) {
        onlineUsers.put(userId, session);
    }

    public static void removeSession(Long userId) {
        onlineUsers.remove(userId);
    }

    public static Session getSession(Long userId) {
        return onlineUsers.get(userId);
    }

    public static Integer getOnlineCount() {
        return onlineUsers.size();
    }
}