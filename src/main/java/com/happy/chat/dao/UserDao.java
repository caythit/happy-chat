package com.happy.chat.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import com.happy.chat.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Lazy
@Component
public class UserDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public static final String TABLE_NAME = "user_info";
    public static final String ALL_FIELD = "id,user_id,user_name,appver,user_id,apns_token,"
            + "firebase_token,xiaomi_token,huawei_token,oppo_token,vivo_token,klink_token,phone_model,explore,"
            + "locale,slice,device_login_time,user_login_time,timezone,last_active_time,"
            + "last_received_time,last_clicked_time,user_group,ip,device_info,"
            + "last_pull_time,lat,lon,create_time,modify_time,user_name,country,off_channel,region";

    private final RowMapper<User> userRowMapper = new BeanPropertyRowMapper<>(User.class);

    public User getUserByName(String userName) {
        return null;
    }

    public User getUserById(String userId) {
        StringBuilder sqlBuilder = new StringBuilder("select ").append(ALL_FIELD).append(" from ")
                .append(TABLE_NAME)
                .append(" where user_id = :userId");

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);

        try {
            return jdbcTemplate.queryForObject(sqlBuilder.toString(), userRowMapper);
        } catch (EmptyResultDataAccessException e) {
            log.info("no data: userId {}", userId);
        } catch (Exception e) {
            log.error("get data err: userId {}", userId, e);
        }
        return null;
    }
}
