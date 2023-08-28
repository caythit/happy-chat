package com.happy.chat.enums;

public enum PaymentState {
    INIT("init"),

    FAILED("failed"),
    SUCCESS("success"),
    ;

    private final String state;

    PaymentState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
