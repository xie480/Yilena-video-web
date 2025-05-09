package com.yilena.service.dao;

import com.yilena.service.entity.po.Log;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserLogMapper {

    void addLog(Log operateLog);
}
