package com.yilena.service.mq;

import com.yilena.service.constant.MqConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.constant.UserConstant;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.entity.po.User;
import com.yilena.service.entity.vo.UserVO;
import com.yilena.service.es.UserES;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserQueueListener {

    private final UserES userES;
    private final UserMapper userMapper;

    @RabbitListener(queues = MqConstant.USER_QUEUE)
    public void receive(UserVO userVO) {
        log.info("接收到消息：{}", userVO);
        if(Objects.equals(userVO.getType(), MqConstant.SEND_ADD)){
            userES.addUser(userVO);
        }
        else if(Objects.equals(userVO.getType(), MqConstant.SEND_PUT)){
            userES.updateUser(userVO);
        }else if(Objects.equals(userVO.getType(), MqConstant.SEND_QUERY)){
            User user = userMapper.getUserById(userVO.getId());
            if(Objects.equals(user.getStatus(), StatusConstant.STATUS_NO)){
                throw new RuntimeException(UserConstant.USER_CAN_NOT_DO_ANYTHING);
            }
        }
    }
}
