package com.happy.chat.view;

import java.util.List;

import lombok.Data;

@Data
public class FeedView {
    // 用户名
    private String userName;
    // 包含了可能的付费卡片
    private List<RobotInfoView> robotInfoViewList;

}
