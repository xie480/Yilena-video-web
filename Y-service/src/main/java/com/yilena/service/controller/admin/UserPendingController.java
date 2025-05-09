package com.yilena.service.controller.admin;


import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.UserPendingPageQueryDTO;
import com.yilena.service.entity.dto.UserPendingStatusDTO;
import com.yilena.service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/manager/user")
public class UserPendingController {

    private final UserService userService;

    @PutMapping("/status")
    public Result updateUserStatus(@RequestBody UserPendingStatusDTO userPendingStatusDTO) {
        log.info("更新用户状态：{}", userPendingStatusDTO);
        userService.updateUserStatus(userPendingStatusDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result getUserByPage(UserPendingPageQueryDTO userPendingPageQueryDTO){
        log.info("用户分页查询");
        return Result.success(userService.getUserPendingByPage(userPendingPageQueryDTO));
    }
}
