package com.happy.chat.view;

import lombok.Data;

@Data
public class UserChatListView {
    private boolean subscribeRobot;
    private String robotId;
    private String robotName;
    private String robotHeadUrl;
    private String lastMessageContent;
    private long lastMessageSendTime;
}
