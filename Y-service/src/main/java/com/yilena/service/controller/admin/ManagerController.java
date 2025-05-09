package com.yilena.service.controller.admin;

import com.yilena.service.constant.UserConstant;
import com.yilena.service.entity.LoginInfo;
import com.yilena.service.entity.Result;
import com.yilena.service.entity.po.Manager;
import com.yilena.service.service.ManagerService;
import com.yilena.service.utils.CurrentHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerController {

    private final ManagerService managerService;

    @PostMapping("/login")
    public Result login(@RequestBody Manager manager) {
        log.info("管理员登录");
        LoginInfo loginInfo = managerService.login(manager);
        if(loginInfo == null){
            return Result.error(UserConstant.USER_LOGIN_FAIL);
        }
        return Result.success(loginInfo);
    }

    @PutMapping("/status/{status}")
    public Result updateStatus(@PathVariable Integer status, Integer id) {
        log.info("管理员修改状态");
        managerService.updateStatus(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result getManagerById(@PathVariable Integer id) {
        log.info("管理员查询");
        Manager manager = managerService.getManagerById(id);
        return Result.success(manager);
    }

    @PutMapping("/password")
    public Result updatePassword(@RequestBody Manager manager) {
        log.info("管理员修改密码");
        managerService.updatePassword(manager);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result logout(){
        log.info("管理员{}退出登录", CurrentHolder.getCurrent());
        CurrentHolder.removeCurrent();
        return Result.success();
    }

    @GetMapping("/getByUsername/{username}")
    public Result getByUsername(@PathVariable String username) {
        log.info("管理员根据username:{}查询",username);
        Manager manager = managerService.getByUsername(username);
        return Result.success(manager);
    }
}
