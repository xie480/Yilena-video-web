package com.yilena.service.dao;

import com.yilena.service.entity.po.User;
import com.yilena.service.entity.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    User getByUsername(String username);

    void addUser(User user);

    User getUserById(Long id);

    void updateUserById(User user);

    List<UserVO> getUserByIds(List<Long> ids, String username);

    void updateIsRecord(Long userId, Integer isRecord);
}
