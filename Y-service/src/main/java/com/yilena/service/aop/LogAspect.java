package com.yilena.service.aop;

import com.yilena.service.dao.ManagerLogMapper;
import com.yilena.service.dao.UserLogMapper;
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

    private final UserLogMapper userLogMapper;
    private final ManagerLogMapper managerLogMapper;
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
        newLog.setMethodName(joinPoint.getSignature().getName());
        newLog.setMethodParams(Arrays.toString(joinPoint.getArgs()));
        newLog.setReturnValue(result.toString());
        newLog.setCostTime(costTime / 1000.0);

        String classname = joinPoint.getTarget().getClass().getName().split("\\.")[5];
        if(joinPoint.getTarget().getClass().getName().split("\\.")[4].equals("user")) {
            switch (classname) {
                case "CommentController" -> newLog.setClassName("评论操作");
                case "FavoritesFolderController" -> newLog.setClassName("收藏夹操作");
                case "PostController" -> newLog.setClassName("动态操作");
                case "UserController" -> newLog.setClassName("用户操作");
                case "UserVideoPendingController" -> newLog.setClassName("用户视频后台操作");
                case "VideoCollectionController" -> newLog.setClassName("视频投稿合集操作");
                case "VideoController" -> newLog.setClassName("视频操作");
            }
            userLogMapper.addLog(newLog);
        }else{
            switch (classname) {
                case "ManagerController" -> newLog.setClassName("管理员操作");
                case "PostPendingController" -> newLog.setClassName("动态审核操作");
                case "UserPendingController" -> newLog.setClassName("用户审核操作");
                case "VideoPendingController" -> newLog.setClassName("视频审核操作");
            }
            managerLogMapper.addLog(newLog);
        }
        log.info("操作日志：{}", newLog);
        return result;
    }

    private Long getCurrentUserId() {
        return CurrentHolder.getCurrent();
    }
}