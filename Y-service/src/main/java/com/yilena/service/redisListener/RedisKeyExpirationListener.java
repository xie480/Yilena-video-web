package com.yilena.service.redisListener;

import com.yilena.service.dao.ChatMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    @Autowired
    private ChatMessageMapper messageMapper;
    
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());
        if(expiredKey.startsWith("offline:")) {
            log.info("检测到离线消息过期: {}", expiredKey);
            // 解析key获取用户ID和消息ID
            String[] parts = expiredKey.split(":");
            if(parts.length >= 3) {
                Long receiveUserId = Long.parseLong(parts[1]);
                Long sendUserId = Long.parseLong(parts[2]);
                // 更新MySQL中这些消息的状态为"已过期"
                messageMapper.updateExpiredStatus(receiveUserId, sendUserId);
            }
        }
    }
}