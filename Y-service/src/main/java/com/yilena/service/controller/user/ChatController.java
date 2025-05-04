package com.yilena.service.controller.user;

import com.yilena.service.entity.Result;
import com.yilena.service.entity.dto.ReadRequestDTO;
import com.yilena.service.entity.po.ChatMessage;
import com.yilena.service.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/message")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/history")
    public Result getChatHistory(ReadRequestDTO requestDTO) {
        log.info("获取聊天记录");
        return Result.success(chatService.getHistoryMessage(requestDTO.getCurrentUserId(), requestDTO.getTargetUserId()));
    }

    @PostMapping("/mark-read")
    public Result markMessagesAsRead(@RequestBody ReadRequestDTO requestDTO) {
        log.info("标记消息为已读");
        chatService.markMessagesAsRead(requestDTO);
        return Result.success();
    }

    @PostMapping("/send")
    public Result sendMessage(@RequestBody ChatMessage chatMessage) {
        log.info("发送消息");
        chatService.sendMessage(chatMessage);
        return Result.success();
    }

    @GetMapping("/userList/{userId}")
    public Result getUserList(@PathVariable Long userId) {
        log.info("获取用户列表");
        return Result.success(chatService.getUserList(userId));
    }

    @DeleteMapping
    public Result deleteMessage(@RequestParam Long messageId,Integer type) {//1删除消息，2撤回消息
        log.info("删除或撤回消息");
        chatService.deleteMessage(messageId,type);
        return Result.success();
    }

    @GetMapping("/answerMe/{userId}")
    public Result getAnswerMe(@PathVariable Long userId) {
        log.info("获取回复我的页面的数据列表");
        return Result.success(chatService.getAnswerMe(userId));
    }

    @GetMapping("/atMe/{userId}")
    public Result getAtMe(@PathVariable Long userId) {
        log.info("获取@我的页面的数据列表");
        return Result.success(chatService.getAtMe(userId));
    }

    @GetMapping("/likeMe/{userId}")
    public Result getLikeMe(@PathVariable Long userId) {
        log.info("获取点赞页面的数据列表");
        return Result.success(chatService.getLikeMe(userId));
    }

    @GetMapping("/systemMessage/{userId}")
    public Result getSystemMessage(@PathVariable Long userId) {
        log.info("获取系统消息");
        return Result.success(chatService.getSystemMessage(userId));
    }

    @GetMapping("/otherMessage/{userId}")
    public Result getOtherMessage(@PathVariable Long userId) {
        log.info("获取其他消息");
        return Result.success(chatService.getOtherMessage(userId));
    }
}