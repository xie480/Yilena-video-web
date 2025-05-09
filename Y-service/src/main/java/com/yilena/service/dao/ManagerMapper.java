package com.yilena.service.dao;

import com.yilena.service.entity.po.Manager;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ManagerMapper {
    Manager getByUsername(String username);

    void updateStatus(Integer status, Integer id);

    Manager getManagerById(Integer id);

    void updatePassword(Manager manager);
}
