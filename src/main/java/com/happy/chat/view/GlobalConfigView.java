package com.happy.chat.view;

import lombok.Data;

@Data
public class GlobalConfigView {
    private UpdateDialog updateDialog;

    @Data
    public static class UpdateDialog {
        private String title;
        private String content;
        private boolean forceUpdate;
        private String downloadUrl;
    }
}
