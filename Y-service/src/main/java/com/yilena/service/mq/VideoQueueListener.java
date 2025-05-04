package com.yilena.service.mq;

import com.yilena.service.constant.MqConstant;
import com.yilena.service.entity.vo.VideoVO;
import com.yilena.service.es.VideoES;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoQueueListener {

    private final VideoES videoES;

    @RabbitListener(queues = MqConstant.VIDEO_QUEUE)
    public void receive(VideoVO videoVO) {
        log.info("视频队列监听器收到消息：{}", videoVO);
        if(Objects.equals(videoVO.getType(), MqConstant.SEND_ADD)){
            videoES.addVideo(videoVO);
        }
        else if (Objects.equals(videoVO.getType(),MqConstant.SEND_PUT)) {
            videoES.updateVideo(videoVO);
        }
        else if(Objects.equals(videoVO.getType(), MqConstant.SEND_DELETE)){
            videoES.deleteVideo(videoVO);
        }
    }
}
