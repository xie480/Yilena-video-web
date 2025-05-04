package com.yilena.service.aop;

import com.yilena.service.dao.LogMapper;
import com.yilena.service.entity.po.Log;
import com.yilena.service.log.LogOperation;
import com.yilena.service.utils.CurrentHolder;
import com.yilena.service.utils.SnowFlake;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final LogMapper logMapper;
    private final SnowFlake snowFlake;

    // 环绕通知
    @Around("@annotation(olog)")
    public Object around(ProceedingJoinPoint joinPoint, LogOperation olog) throws Throwable {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 执行方法
        Object result = joinPoint.proceed();
        // 当前时间
        long endTime = System.currentTimeMillis();
        // 耗时
        long costTime = endTime - startTime;

        // 构建日志对象
        Log newLog = new Log();
        newLog.setId(snowFlake.getID());
        newLog.setOperateUserId(getCurrentUserId());
        newLog.setOperateTime(LocalDateTime.now());
        newLog.setClassName(joinPoint.getTarget().getClass().getName());
        newLog.setMethodName(joinPoint.getSignature().getName());
        newLog.setMethodParams(Arrays.toString(joinPoint.getArgs()));
        newLog.setReturnValue(result.toString());
        newLog.setCostTime(costTime);

        // 插入日志
        logMapper.addLog(newLog);
        log.info("操作日志：{}", newLog);
        return result;
    }

    private Long getCurrentUserId() {
        return CurrentHolder.getCurrent();
    }
}