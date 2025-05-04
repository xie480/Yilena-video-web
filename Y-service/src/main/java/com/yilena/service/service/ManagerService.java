package com.yilena.service.service;

import com.yilena.service.entity.LoginInfo;
import com.yilena.service.entity.po.Manager;

public interface ManagerService {
    LoginInfo login(Manager manager);

    void updateStatus(Integer status, Integer id);
}
