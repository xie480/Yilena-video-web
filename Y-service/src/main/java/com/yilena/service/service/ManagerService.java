package com.yilena.service.service;

import com.yilena.service.entity.LoginInfo;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.ManagerPageQueryDTO;
import com.yilena.service.entity.po.Manager;

public interface ManagerService {
    LoginInfo login(Manager manager);

    void updateStatus(Integer status, Integer id);

    Manager getManagerById(Integer id);

    void updatePassword(Manager manager);

    Manager getByUsername(String username);

    PageResult<Manager> getManagerByPage(ManagerPageQueryDTO managerPageQueryDTO);
}
