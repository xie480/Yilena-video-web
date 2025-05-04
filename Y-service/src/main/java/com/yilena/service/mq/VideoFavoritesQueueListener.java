package com.yilena.service.mq;

import com.yilena.service.constant.MqConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.constant.UserConstant;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.dao.VideoFavoritesMapper;
import com.yilena.service.entity.po.User;
import com.yilena.service.entity.po.VideoFavorites;
import com.yilena.service.entity.vo.UserVO;
import com.yilena.service.es.UserES;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class VideoFavoritesQueueListener {

    private final VideoFavoritesMapper videoFavoritesMapper;

    @RabbitListener(queues = MqConstant.VIDEO_FAVORITES_QUEUE)
    public void receive(VideoFavorites videoFavorites) {
        log.info("接收到消息：{}", videoFavorites);
        if(Objects.equals(videoFavorites.getType(), MqConstant.SEND_ADD)){
            videoFavoritesMapper.addVideoFavorites(videoFavorites);
        }
        else if(Objects.equals(videoFavorites.getType(), MqConstant.SEND_DELETE)){
            videoFavoritesMapper.deleteVideoFavorites(videoFavorites);
        }
    }
}
