package com.happy.chat.view;

import static com.happy.chat.constants.Constant.EXTRA_INFO_MESSAGE_PAY_TIPS;

import java.util.Map;

import org.apache.logging.log4j.util.Strings;

import com.happy.chat.domain.ChatMessage;
import com.happy.chat.uitls.ObjectMapperUtils;

import lombok.Data;

@Data
public class ChatMessageView {
    private String messageId;
    private String messageType;
    private String messageFrom;
    private String content;
    private long sendTime;
    private String payTips;

    public static ChatMessageView convertChatMessage(ChatMessage chatMessage) {
        ChatMessageView chatMessageView = new ChatMessageView();
        chatMessageView.setMessageId(chatMessage.getMessageId());
        chatMessageView.setMessageType(chatMessage.getMessageType());
        chatMessageView.setMessageFrom(chatMessage.getMessageFrom());
        chatMessageView.setContent(chatMessage.getContent());
        chatMessageView.setSendTime(chatMessage.getCreateTime());

        Map<String, String> extraMap = ObjectMapperUtils.fromJSON(chatMessage.getExtraInfo(), Map.class, String.class,String.class);
        chatMessageView.setPayTips(extraMap.getOrDefault(EXTRA_INFO_MESSAGE_PAY_TIPS, Strings.EMPTY));

        return chatMessageView;
    }
}
