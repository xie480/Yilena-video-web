package com.yilena.service.mq;

import com.yilena.service.constant.MqConstant;
import com.yilena.service.entity.vo.PostVO;
import com.yilena.service.es.PostES;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostQueueListener {

    private final PostES postES;

    @RabbitListener(queues = MqConstant.POST_QUEUE)
    public void receive(PostVO postVO) {
        log.info("动态队列监听到消息:{}", postVO);
        if(Objects.equals(postVO.getType(), MqConstant.SEND_ADD)) {
            postES.addPost(postVO);
        }
        else if (Objects.equals(postVO.getType(), MqConstant.SEND_PUT)) {
            postES.updatePost(postVO);
        }
        else if (Objects.equals(postVO.getType(), MqConstant.SEND_DELETE)) {
            postES.deletePost(postVO.getId());
        }
    }
}
