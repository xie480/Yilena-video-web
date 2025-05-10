package com.yilena.service.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yilena.service.constant.LogTypeConstant;
import com.yilena.service.dao.ManagerLogMapper;
import com.yilena.service.dao.UserLogMapper;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.LogPageQueryDTO;
import com.yilena.service.entity.po.Log;
import com.yilena.service.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final ManagerLogMapper managerLogMapper;
    private final UserLogMapper userLogMapper;

    @Override
    public PageResult<Log> getLogByPage(LogPageQueryDTO logPageQueryDTO) {
        PageHelper.startPage(logPageQueryDTO.getPage(), logPageQueryDTO.getPageSize());
        List<Log> logs = null;
        if(logPageQueryDTO.getType().equals(LogTypeConstant.USER_LOG)){
            logs = userLogMapper.getLogByPage(logPageQueryDTO);
        }else if(logPageQueryDTO.getType().equals(LogTypeConstant.MANAGER_LOG)){
            logs = managerLogMapper.getLogByPage(logPageQueryDTO);
        }
        Page<Log> p = (Page<Log>) logs;
        return new PageResult<>(p.getTotal(), p.getResult());
    }
}
