package com.happy.chat.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.happy.chat.domain.Robot;

public interface RobotService {
    Robot getRobotById(String robotId);

    Map<String, Robot> batchGetRobotById(Set<String> robotId);

    List<Robot> getAllRobot();

    int addRobot(Robot robot);
}
