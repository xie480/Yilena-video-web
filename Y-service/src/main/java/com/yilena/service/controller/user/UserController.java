package com.yilena.service.controller.user;

import com.yilena.service.constant.UserConstant;
import com.yilena.service.entity.Result;
import com.yilena.service.entity.LoginInfo;
import com.yilena.service.entity.dto.UserDTO;
import com.yilena.service.entity.po.User;
import com.yilena.service.log.LogOperation;
import com.yilena.service.service.UserService;
import com.yilena.service.utils.CurrentHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody User user){
        log.info("用户{}登录", user.getUsername());
        LoginInfo loginInfo =userService.login(user);
        // 登录失败
        if (loginInfo == null){
            return Result.error(UserConstant.USER_LOGIN_FAIL);
        }
        return Result.success(loginInfo);
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user){
        log.info("用户{}注册", user.getUsername());
        userService.register(user);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result logout(){
        log.info("用户{}退出登录", CurrentHolder.getCurrent());
        CurrentHolder.removeCurrent();
        return Result.success();
    }

    @GetMapping("/search/page")
    @CacheEvict(value = "searchHistory", key = "'page'")
    public Result getUserByPage(UserDTO userDTO){
        log.info("用户查询用户分页信息");
        return Result.success(userService.getUserByPage(userDTO));
    }

    @Cacheable(value = "user", key = "#id")
    @GetMapping("/search/{id}")
    public Result getUserById(@PathVariable Long id){
        log.info("用户查询用户id:{}信息", id);
        return Result.success(userService.getUserById(id));
    }

    @Cacheable(value = "user", key = "#username")
    @GetMapping("/search/username/{username}")
    public Result getUserByUsername(@PathVariable String username){
        log.info("用户查询用户名:{}信息", username);
        return Result.success(userService.getUserByUsername(username));
    }

    @Caching(evict = {
            @CacheEvict(value = "user", key = "#user.id"),
            @CacheEvict(value = "user", key = "#user.username")
    })
    @LogOperation
    @PutMapping
    public Result updateUserById(@RequestBody User user){
        log.info("用户修改用户id:{}信息",user.getId());
        userService.updateUserById(user);
        return Result.success();
    }

    @GetMapping("/getToken/{id}")
    public Result getToken(@PathVariable Long id){
        log.info("用户获取token");
        return Result.success(userService.getToken(id));
    }
}
