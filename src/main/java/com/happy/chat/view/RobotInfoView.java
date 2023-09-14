package com.happy.chat.view;

import static com.happy.chat.constants.Constant.EXTRA_INFO_ROBOT_ABOUT_ME;
import static com.happy.chat.constants.Constant.EXTRA_INFO_ROBOT_INTEREST;
import static com.happy.chat.constants.Constant.EXTRA_INFO_ROBOT_WORK;

import java.util.Map;

import org.apache.logging.log4j.util.Strings;

import com.happy.chat.domain.Robot;
import com.happy.chat.uitls.ObjectMapperUtils;

import lombok.Data;

@Data
public class RobotInfoView {
    private String robotId;
    private String name;
    private int sex;
    private int age;
    private String city;
    private String country;
    private String headUrl;
    private String coverUrl;
    private String bgUrl;
    private String work; //职业
    private String aboutMe; //介绍话
    private String interest; //兴趣爱好
    private boolean userHasSubscribe;

    //private String paymentHint; // 支付提示语，首页用到

    public static RobotInfoView convertRobot(Robot robot) {
        if (robot == null) {
            return null;
        }
        RobotInfoView robotInfoView = new RobotInfoView();
        robotInfoView.setRobotId(robot.getRobotId());
        robotInfoView.setName(robot.getName());
        robotInfoView.setAge(robot.getAge());
        robotInfoView.setSex(robot.getSex());
        robotInfoView.setCity(robot.getCity());
        robotInfoView.setCountry(robot.getCountry());
        robotInfoView.setHeadUrl(robot.getHeadUrl());
        robotInfoView.setCoverUrl(robot.getCoverUrl());
        robotInfoView.setBgUrl(robot.getBgUrl());

        Map<String, String> extraMap = ObjectMapperUtils.fromJSON(robot.getExtraInfo(), Map.class, String.class,String.class);
        robotInfoView.setWork(extraMap.getOrDefault(EXTRA_INFO_ROBOT_WORK, Strings.EMPTY));
        robotInfoView.setAboutMe(extraMap.getOrDefault(EXTRA_INFO_ROBOT_ABOUT_ME, Strings.EMPTY));
        robotInfoView.setInterest(extraMap.getOrDefault(EXTRA_INFO_ROBOT_INTEREST, Strings.EMPTY));
        return robotInfoView;
    }
}
