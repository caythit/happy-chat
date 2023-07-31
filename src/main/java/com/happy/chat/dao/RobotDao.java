package com.happy.chat.dao;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;
import com.happy.chat.domain.Robot;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Lazy
@Component
public class RobotDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public static final String TABLE_NAME = "robot_info";
    public static final String ALL_FIELD = "id,robot_id,head_url,cover_url,bg_url,"
            + "name,sex,age,city,country,extra_info,create_time,update_time";

    private final RowMapper<Robot> robotRowMapper = new BeanPropertyRowMapper<>(Robot.class);

    public List<Robot> getAllRobot() {
        String sqlBuilder = "select " + ALL_FIELD + " from " + TABLE_NAME;
        return jdbcTemplate.query(sqlBuilder, robotRowMapper);
    }

    public Robot getRobotById(String robotId) {
        return batchGetRobotById(ImmutableSet.of(robotId)).get(robotId);
    }

    public Map<String, Robot> batchGetRobotById(Set<String> robotIds) {
        String sql = format("select * from %s where robot_id in (:robotIds)", TABLE_NAME);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("robotIds", robotIds);

        Map<String, Robot> result = new HashMap<>(robotIds.size());
        jdbcTemplate.query(sql, params, rs->{
            Robot robot = robotRowMapper.mapRow(rs, 0);
            if (robot != null) {
                result.put(robot.getRobotId(), robot);
            }
        });
        return result;
    }

    public int insert(Robot robot) {
        String sql = "insert into " + TABLE_NAME
                + " (robot_id,head_url,cover_url,bg_url, name,sex,age,city,country,extra_info,create_time,update_time) "
                + " values (:robotId, :headUrl, :coverUrl, :bgUrl, :name, :sex, :age, :city, :country, :extraInfo, :createTime, :updateTime) "
                + " on duplicate key update update_time = :updateTime";
        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("robotId", robot.getRobotId());
        params.addValue("headUrl", robot.getHeadUrl());
        params.addValue("coverUrl", robot.getCoverUrl());
        params.addValue("bgUrl", robot.getBgUrl());
        params.addValue("name", robot.getName());
        params.addValue("sex", robot.getSex());
        params.addValue("age", robot.getAge());
        params.addValue("city", robot.getCity());
        params.addValue("country", robot.getCountry());

        if (StringUtils.isNotEmpty(robot.getExtraInfo())) {
            params.addValue("extraInfo", robot.getExtraInfo());
        }

        params.addValue("createTime", System.currentTimeMillis());
        params.addValue("updateTime", System.currentTimeMillis());
        return jdbcTemplate.update(sql, params);
    }
}
