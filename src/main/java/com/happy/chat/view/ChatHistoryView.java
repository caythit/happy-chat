package com.happy.chat.view;

import java.util.List;

import lombok.Data;

@Data
public class ChatHistoryView {
    private RobotInfoView robotInfoView;
    private List<FlirtopiaChatView> flirtopiaChatViewList;
}
