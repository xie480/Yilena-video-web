package com.yilena.service.mq;

import com.yilena.service.constant.MqConstant;
import com.yilena.service.constant.UserConstant;
import com.yilena.service.entity.po.UserFollowing;
import com.yilena.service.entity.vo.UserVO;
import com.yilena.service.es.FollowES;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class FollowQueueListener {

    private final FollowES followES;

    @RabbitListener(queues = MqConstant.FOLLOW_QUEUE)
    public void receive(UserFollowing userFollowing) {
        log.info("接收到消息：{}", userFollowing);
        if(Objects.equals(userFollowing.getType(), MqConstant.SEND_ADD)){
            followES.add(userFollowing);
        }
        else if(Objects.equals(userFollowing.getType(), MqConstant.SEND_DELETE)){
            followES.delete(userFollowing);
        }
    }
}
