package com.happy.chat.view;

import static com.happy.chat.constants.Constant.EXTRA_INFO_MESSAGE_PAY_TIPS;
import static com.happy.chat.constants.Constant.EXTRA_INFO_MESSAGE_SYSTEM_TIPS;

import java.util.Map;

import org.apache.logging.log4j.util.Strings;

import com.happy.chat.domain.FlirtopiaChat;
import com.happy.chat.uitls.ObjectMapperUtils;

import lombok.Data;

@Data
public class FlirtopiaChatView {
    private String messageId;
    private String messageType;
    private String messageFrom;
    private String content;
    private long sendTime;
    private String payTips;
    private String warnTips;
    private String robotPicture;

    public static FlirtopiaChatView convertChat(FlirtopiaChat flirtopiaChat) {
        FlirtopiaChatView flirtopiaChatView = new FlirtopiaChatView();
        flirtopiaChatView.setMessageId(flirtopiaChat.getMessageId());
        flirtopiaChatView.setMessageType(flirtopiaChat.getMessageType());
        flirtopiaChatView.setMessageFrom(flirtopiaChat.getMessageFrom());
        flirtopiaChatView.setContent(flirtopiaChat.getContent());
        flirtopiaChatView.setSendTime(flirtopiaChat.getCreateTime());

        Map<String, String> extraMap = ObjectMapperUtils.fromJSON(flirtopiaChat.getExtraInfo(), Map.class, String.class,String.class);
        flirtopiaChatView.setPayTips(extraMap.getOrDefault(EXTRA_INFO_MESSAGE_PAY_TIPS, Strings.EMPTY));
        flirtopiaChatView.setWarnTips(extraMap.getOrDefault(EXTRA_INFO_MESSAGE_SYSTEM_TIPS, Strings.EMPTY));

        return flirtopiaChatView;
    }
}
