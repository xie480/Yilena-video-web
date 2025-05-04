package com.yilena.service.exception;

import com.yilena.service.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LoginException.class)
    public Result handleException(LoginException e) {
        log.error("******异常信息******   ", e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(RegisterException.class)
    public Result handleException(RegisterException e) {
        log.error("******异常信息******   ", e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(EsException.class)
    public Result handleException(EsException e) {
        log.error("******异常信息******   ", e);
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result handleException(SQLIntegrityConstraintViolationException e) {
        log.error("******异常信息******   ", e);
        if (e.getMessage().contains("Duplicate entry")) {
            String[] split = e.getMessage().split(" ");
            String msg = split[2] + "已存在";
            return Result.error(msg);
        }
        return Result.error("操作失败");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleException(DuplicateKeyException e) {
        log.error("******异常信息******   ", e);
        if (e.getMessage().contains("Duplicate entry")) {
            String[] split = e.getMessage().split(" ");
            String msg = split[9] + "已存在";
            return Result.error(msg);
        }
        return Result.error("操作失败");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result handleException(RuntimeException e) {
        log.error("******异常信息******   ", e);
        return Result.error(e.getMessage());
    }
}
