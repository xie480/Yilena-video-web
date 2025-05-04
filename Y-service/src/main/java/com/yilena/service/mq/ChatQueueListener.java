package com.yilena.service.mq;

import com.yilena.service.constant.MqConstant;
import com.yilena.service.dao.ChatMessageMapper;
import com.yilena.service.entity.po.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatQueueListener {

    private final ChatMessageMapper chatMessageMapper;

    @RabbitListener(queues = MqConstant.CHAT_QUEUE)
    public void receive(ChatMessage chatMessage) {
        log.info("接收到消息：{}", chatMessage);
        chatMessageMapper.addMessage(chatMessage);
    }
}
