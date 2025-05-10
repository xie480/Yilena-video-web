package com.yilena.service.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.constant.UserConstant;
import com.yilena.service.dao.ManagerMapper;
import com.yilena.service.entity.LoginInfo;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.ManagerPageQueryDTO;
import com.yilena.service.entity.po.Manager;
import com.yilena.service.exception.LoginException;
import com.yilena.service.service.ManagerService;
import com.yilena.service.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.NameMangler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ManagerServiceImpl implements ManagerService {

    private final ManagerMapper managerMapper;

    @Override
    public LoginInfo login(Manager manager) {
        // 校验用户名密码
        if(manager.getUsername() == null || manager.getPassword() == null){
            throw new LoginException(UserConstant.USER_EMPTY_FAIL);
        }
        Manager dbManager = managerMapper.getByUsername(manager.getUsername());
        // 没找到用户
        if(dbManager == null){
            throw new LoginException(UserConstant.USER_LOGIN_INEXIST_FAIL);
        } else {

            if(dbManager.getStatus().equals(StatusConstant.STATUS_NO)){
                throw new LoginException(UserConstant.USER_CAN_NOT_LOGIN);
            }

            // 密码解密后进行匹配
            if(manager.getPassword().equals(dbManager.getPassword())){
                // 生成jwt令牌
                Map<String, Object> claims = Map.of("id", dbManager.getId(),"username", dbManager.getUsername());
                String jwt = JwtUtils.generateToken(claims);
                return new LoginInfo(dbManager.getUsername(),null, jwt);
            } else {
                throw new LoginException(UserConstant.USER_LOGIN_FAIL);
            }
        }
    }

    @Override
    public void updateStatus(Integer status, Integer id) {
        managerMapper.updateStatus(status, id);
    }

    @Override
    public Manager getManagerById(Integer id) {
        return managerMapper.getManagerById(id);
    }

    @Override
    public void updatePassword(Manager manager) {
        Manager dbManager = managerMapper.getManagerById(manager.getId());
        if(dbManager == null){
            throw new LoginException(UserConstant.USER_LOGIN_INEXIST_FAIL);
        }
        if(manager.getNewPassword().equals(dbManager.getPassword())){
            throw new LoginException(UserConstant.USER_PASSWORD_SAME_FAIL);
        }
        if(manager.getOldPassword().equals(dbManager.getPassword())){
            managerMapper.updatePassword(manager);
        } else {
            throw new LoginException(UserConstant.USER_PASSWORD_WRONG);
        }
    }

    @Override
    public Manager getByUsername(String username) {
        return managerMapper.getByUsername(username);
    }

    @Override
    public PageResult<Manager> getManagerByPage(ManagerPageQueryDTO managerPageQueryDTO) {
        PageHelper.startPage(managerPageQueryDTO.getPage(), managerPageQueryDTO.getPageSize());
        List<Manager> managers = managerMapper.getManagerByPage(managerPageQueryDTO);
        Page<Manager> p = (Page<Manager>)managers;
        return new PageResult<>(p.getTotal(), p.getResult());
    }
}
