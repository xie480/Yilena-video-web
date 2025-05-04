package com.yilena.service.service.impl;

import com.yilena.service.constant.MqConstant;
import com.yilena.service.dao.HotSearchMapper;
import com.yilena.service.entity.po.HotSearch;
import com.yilena.service.mq.HotSearchQueueListener;
import com.yilena.service.service.HotSearchService;
import com.yilena.service.utils.SnowFlake;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotSearchServiceImpl implements HotSearchService {

    private final HotSearchMapper hotSearchMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public List<HotSearch> getTopHotSearch() {
        return hotSearchMapper.getHotSearchVideo();
    }

    @Override
    public void addSearchHistory(HotSearch hotSearch) {
        //mq发送消息
        rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE, MqConstant.HOT_SEARCH_BINDING_KEY, hotSearch);
    }
}
