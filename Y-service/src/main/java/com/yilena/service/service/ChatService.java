package com.yilena.service.service;

import com.yilena.service.entity.dto.ReadRequestDTO;
import com.yilena.service.entity.po.ChatMessage;
import com.yilena.service.entity.vo.MessageShowVO;
import com.yilena.service.entity.vo.UserVO;

import java.util.List;

public interface ChatService {

    List<ChatMessage> getHistoryMessage(Long currentUserId, Long targetUserId);

    void markMessagesAsRead(ReadRequestDTO requestDTO);

    void sendMessage(ChatMessage chatMessage);

    void deleteMessage(Long messageId,Integer type);

    List<UserVO> getUserList(Long userId);

    List<MessageShowVO> getAnswerMe(Long userId);

    List<MessageShowVO> getAtMe(Long userId);

    List<MessageShowVO> getLikeMe(Long userId);

    List<MessageShowVO> getSystemMessage(Long userId);

    List<MessageShowVO> getOtherMessage(Long userId);
}
