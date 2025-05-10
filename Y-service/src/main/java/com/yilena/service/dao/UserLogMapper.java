package com.yilena.service.dao;

import com.yilena.service.entity.dto.LogPageQueryDTO;
import com.yilena.service.entity.po.Log;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserLogMapper {

    void addLog(Log operateLog);

    List<Log> getLogByPage(LogPageQueryDTO logPageQueryDTO);

    void deleteLogsBeforeHalfYear();
}
