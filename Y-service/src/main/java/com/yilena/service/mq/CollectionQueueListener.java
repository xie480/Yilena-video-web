package com.yilena.service.mq;

import com.alibaba.fastjson2.JSON;
import com.yilena.service.constant.MqConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.VideoCollectionMapper;
import com.yilena.service.entity.po.VideoCollection;
import com.yilena.service.entity.vo.VideoCollectionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionQueueListener {

    private final VideoCollectionMapper videoCollectionMapper;

    @RabbitListener(queues = MqConstant.COLLECTION_QUEUE)
    public void receive(VideoCollectionVO videoCollectionVO) {
        log.info("接收到信息：{}",videoCollectionVO);
        // 添加到投稿合集
        VideoCollection videoCollection = videoCollectionMapper.getVideoCollectionById(videoCollectionVO.getId());
        String videoIdsJson = videoCollection.getVideoIdsJson();
        List<Long> videoIds = JSON.parseArray(videoIdsJson, Long.class);
        List<Long> newVideoIds = JSON.parseArray(videoCollectionVO.getVideoIdsJson(),Long.class);
        Long newVideoId = newVideoIds.get(0);
        videoIds.add(newVideoId);
        String newVideoIdsJson = JSON.toJSONString(videoIds);
        videoCollection.setVideoIdsJson(newVideoIdsJson);
        videoCollectionMapper.updateVideoCollection(videoCollection);
    }
}
