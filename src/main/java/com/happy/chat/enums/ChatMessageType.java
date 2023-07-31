package com.happy.chat.enums;

public enum ChatMessageType {
    NORMAL("普通对话"),
    PAY("支付对话"),
    ;

    private String msg;

    ChatMessageType(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
