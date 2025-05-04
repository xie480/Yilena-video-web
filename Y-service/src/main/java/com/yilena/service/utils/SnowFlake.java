package com.yilena.service.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class SnowFlake {

    private SnowFlakeProperties properties;

    public SnowFlake() {
    }

    public SnowFlake(SnowFlakeProperties properties) {
        this.properties = properties;
    }
    public synchronized  long getID(){
        //获得当前时间
        long now = System.currentTimeMillis();
        long lastTimeStamp = properties.getLastTimeStamp();
        //当前系统时间小于上一次记录时间
        if (now < lastTimeStamp){
            throw new RuntimeException("时钟回拨异常");
        }
        //相同时间 要序列号进制增量
        if (now == lastTimeStamp){
            //防止溢出
            long sequence = properties.getSequence();
            sequence = (sequence + 1) & properties.getSequenceMask();
            //更新sequence 的值
            properties.setSequence(sequence);
            if (sequence == 0L){
                //溢出处理
                try {
                    Thread.sleep(1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                //获取下一毫秒时间 （有锁）
                now = System.currentTimeMillis();
            }
        }else {
            //置0
            properties.setSequence(0L);
        }
        //记录当前时间
        properties.setLastTimeStamp(now);
        return ((now - properties.getStartTimeStamp()) << properties.getTimeStampIndex()) |
                (properties.getDataCenterID() << properties.getDataCenterIndex())|
                (properties.getWorkID() << properties.getWorkIDIndex()) | properties.getSequence();
    }
}