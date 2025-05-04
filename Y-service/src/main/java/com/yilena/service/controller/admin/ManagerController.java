package com.yilena.service.controller.admin;

import com.yilena.service.constant.UserConstant;
import com.yilena.service.entity.LoginInfo;
import com.yilena.service.entity.Result;
import com.yilena.service.entity.po.Manager;
import com.yilena.service.service.ManagerService;
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
}
