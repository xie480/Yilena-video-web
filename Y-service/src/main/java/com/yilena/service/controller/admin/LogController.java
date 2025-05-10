package com.yilena.service.controller.admin;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.LogPageQueryDTO;
import com.yilena.service.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/manager/log")
public class LogController {

    private final LogService logService;

    @GetMapping("/page")
    public Result getLogByPage(LogPageQueryDTO logPageQueryDTO) {
        log.info("分页查询日志：{}", logPageQueryDTO);
        return Result.success(logService.getLogByPage(logPageQueryDTO));
    }
}
