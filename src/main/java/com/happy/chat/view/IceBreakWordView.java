package com.happy.chat.view;

import static com.happy.chat.uitls.CommonUtils.formatMessageWithArgs;

import com.happy.chat.domain.IceBreakWord;
import com.happy.chat.domain.Robot;

import lombok.Data;

@Data
public class IceBreakWordView {
    private String content;

    public static IceBreakWordView convertIceBreakWord(IceBreakWord iceBreakWord, Robot robot) {
        IceBreakWordView iceBreakWordView = new IceBreakWordView();
        // format下，可能有占位符
        iceBreakWordView.setContent(formatMessageWithArgs(iceBreakWord.getContent(), robot.getName(), robot.getCountry()));
        return iceBreakWordView;
    }
}
