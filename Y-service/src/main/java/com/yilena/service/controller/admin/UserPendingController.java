package com.yilena.service.controller.admin;


import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.UserPendingStatusDTO;
import com.yilena.service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
