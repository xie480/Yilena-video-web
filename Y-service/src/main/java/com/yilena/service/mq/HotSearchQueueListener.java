package com.yilena.service.mq;

import com.yilena.service.constant.MqConstant;
import com.yilena.service.dao.HotSearchMapper;
import com.yilena.service.entity.po.HotSearch;
import com.yilena.service.utils.SnowFlake;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotSearchQueueListener {

    private final HotSearchMapper hotSearchMapper;
    private final SnowFlake snowFlake;

    @RabbitListener(queues = MqConstant.HOT_SEARCH_QUEUE)
    public void receive(HotSearch hotSearch) {
        log.info("收到热搜消息：{}", hotSearch);
        HotSearch hs = hotSearchMapper.getHotSearchVideoByContent(hotSearch.getContent());
        if(hs == null){
            hotSearch.setId(snowFlake.getID());
            hotSearch.setSearchCount(1);
            hotSearch.setCreatedTime(LocalDateTime.now());
            hotSearchMapper.addSearchHistory(hotSearch);
        }
        else{
            hs.setSearchCount(hs.getSearchCount()+1);
            hotSearchMapper.updateSearchHistory(hs);
        }
    }
}
