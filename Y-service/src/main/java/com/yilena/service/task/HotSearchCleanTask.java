package com.yilena.service.task;
import com.yilena.service.dao.ChatMessageMapper;
import com.yilena.service.dao.HistoryMapper;
import com.yilena.service.dao.HotSearchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class HotSearchCleanTask {

    private final HotSearchMapper hotSearchMapper;

    // 自动清除单日搜索量小于一百的数据
    @Scheduled(cron = "0 0 2 * * ?")
    public void executeEveryMinute() {
        try {
            log.info("清除历史记录定时任务开始执行，当前时间: {}", System.currentTimeMillis());

            hotSearchMapper.deleteHotSearchLtHundredCount();

            log.info("清除历史记录定时任务执行完成");
        } catch (Exception e) {
            log.error("清除历史记录定时任务执行出错: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
