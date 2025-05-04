package com.yilena.service.dao;

import com.yilena.service.entity.po.ChatMessage;
import com.yilena.service.entity.vo.MessageShowVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    void addMessage(ChatMessage message);

    List<ChatMessage> getMessageByPair(Long sendUserId, Long receiveUserId);

    void markMessagesAsRead(Long receiveUserId, Long sendUserId);

    void updateStatusBatch(List<Long> messageIds);

    ChatMessage getMessageById(Long messageId);

    void deleteMessage(Long messageId);

    void updateMessageById(ChatMessage message);

    void updateExpiredStatus(Long receiveUserId, Long sendUserId);

    Long getMessageWhichUnReadAndIsExpired(Long receiveUserId, Long sendUserId);

    List<MessageShowVO> getAnswerMe(Long userId);

    List<MessageShowVO> getAtMe(Long userId);

    List<MessageShowVO> getLikeMe(Long userId);

    List<MessageShowVO> getSystemMessage(Long userId);

    List<MessageShowVO> getOtherMessage(Long userId);

    void deleteMessageHalfYearsAgo();
}
