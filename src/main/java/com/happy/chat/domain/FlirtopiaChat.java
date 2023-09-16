package com.happy.chat.domain;

import lombok.Data;

@Data
public class FlirtopiaChat {
    private String userId;
    private String robotId;
    private String messageId;
    private String messageType;
    private String messageFrom;
    private String content;
    private String extraInfo;
    private String aiRespReasonAndModel;
    private long createTime;
    private long updateTime;
}
