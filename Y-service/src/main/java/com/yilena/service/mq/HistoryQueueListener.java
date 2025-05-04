package com.yilena.service.mq;

import com.yilena.service.constant.MqConstant;
import com.yilena.service.dao.ChatMessageMapper;
import com.yilena.service.dao.HistoryMapper;
import com.yilena.service.entity.po.ChatMessage;
import com.yilena.service.entity.po.History;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HistoryQueueListener {

    private final HistoryMapper historyMapper;

    @RabbitListener(queues = MqConstant.HISTORY_QUEUE)
    public void receive(History history) {
        log.info("接收到消息：{}", history);

        History oldHistory = historyMapper.getHistoryByUserIdAndEntityId(history.getEntityType(), history.getEntityId(), history.getUserId());
        if(oldHistory != null){
            historyMapper.deleteHistory(oldHistory.getId());
        }
        historyMapper.addHistory(history);
    }
}
