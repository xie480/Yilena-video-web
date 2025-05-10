package com.yilena.service.service.impl;

import com.yilena.service.dao.ManagerLogMapper;
import com.yilena.service.dao.UserLogMapper;
import com.yilena.service.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final ManagerLogMapper managerLogMapper;
    private final UserLogMapper userLogMapper;
}
