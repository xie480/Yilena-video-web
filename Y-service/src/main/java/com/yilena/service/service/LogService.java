package com.yilena.service.service;

import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.LogPageQueryDTO;
import com.yilena.service.entity.po.Log;

import java.util.List;

public interface LogService {
    PageResult<Log> getLogByPage(LogPageQueryDTO logPageQueryDTO);
}
