package com.yilena.service.service;

import com.yilena.service.entity.LoginInfo;
import com.yilena.service.entity.PageResult;
import com.yilena.service.entity.dto.UserDTO;
import com.yilena.service.entity.dto.UserPendingPageQueryDTO;
import com.yilena.service.entity.dto.UserPendingStatusDTO;
import com.yilena.service.entity.po.User;

public interface UserService {
    LoginInfo login(User user);

    void register(User user);

    PageResult<User> getUserByPage(UserDTO userDTO);

    User getUserById(Long id);

    void updateUserById(User user);

    User getUserByUsername(String username);

    void updateUserStatus(UserPendingStatusDTO userPendingStatusDTO);

    LoginInfo getToken(Long id);

    PageResult<User> getUserPendingByPage(UserPendingPageQueryDTO userPendingPageQueryDTO);
}
