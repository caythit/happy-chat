package com.happy.chat.enums;

public enum PaymentState {
    CREATE_SESSION("create_session"),
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
