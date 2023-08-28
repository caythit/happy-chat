package com.happy.chat.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.happy.chat.dao.RobotDao;
import com.happy.chat.domain.Robot;
import com.happy.chat.service.RobotService;
import com.happy.chat.uitls.CacheKeyProvider;
import com.happy.chat.uitls.RedisUtil;

@Lazy
@Service
public class RobotServiceImpl implements RobotService {

    @Autowired
    private RobotDao robotDao;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Robot getRobotById(String robotId) {
        return robotDao.getRobotById(robotId);
    }

    @Override
    public Map<String, Robot> batchGetRobotById(Set<String> robotIds) {
        return robotDao.batchGetRobotById(robotIds);
    }

    public List<Robot> getAllRobot() {
        return robotDao.getAllRobot();
    }

    @Override
    public int addRobot(Robot robot) {
        return robotDao.insert(robot);
    }

    @Override
    public String getRobotStripePriceId(String robotId) {
        return redisUtil.get(CacheKeyProvider.robotStripePriceIdKey(robotId));
    }
}
