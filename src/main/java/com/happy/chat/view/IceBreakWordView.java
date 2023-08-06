package com.happy.chat.view;

import com.happy.chat.domain.IceBreakWord;

import lombok.Data;

@Data
public class IceBreakWordView {
    private String content;

    public static IceBreakWordView convertIceBreakWord(IceBreakWord iceBreakWord) {
        IceBreakWordView iceBreakWordView = new IceBreakWordView();
        iceBreakWordView.setContent(iceBreakWordView.getContent());
        // format下，可能有占位符
//        iceBreakWordView.setContent(formatMessageWithArgs(iceBreakWord.getContent(), robot.getName(), robot.getCountry()));
        return iceBreakWordView;
    }
}
