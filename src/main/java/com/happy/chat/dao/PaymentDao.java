package com.happy.chat.dao;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.happy.chat.domain.PaymentItem;
import com.happy.chat.domain.UserSubscribeInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Lazy
@Component
public class PaymentDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public static final String PAYMENT_REQUEST_TABLE = "payment_request";

    private final RowMapper<PaymentItem> rowMapper = new BeanPropertyRowMapper<>(PaymentItem.class);

    public int insertRequest(PaymentItem item) {
        String sql = "insert into " + PAYMENT_REQUEST_TABLE
                + " (user_id, robot_id,session_id, `state`, extra_info,create_time,update_time) "
                + " values (:userId, :robotId, :sessionId, :state, :extraInfo, :createTime, :updateTime) "
                + " on duplicate key update update_time = :updateTime";
        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("userId", item.getUserId());
        params.addValue("robotId", item.getRobotId());
        params.addValue("sessionId", item.getSessionId());
        params.addValue("state", item.getState());

        params.addValue("extraInfo", StringUtils.isNotEmpty(item.getExtraInfo()) ? item.getExtraInfo() : "");

        params.addValue("createTime", item.getCreateTime() == 0 ? System.currentTimeMillis() : item.getCreateTime());
        params.addValue("updateTime", item.getUpdateTime() == 0 ? System.currentTimeMillis() : item.getUpdateTime());
        return jdbcTemplate.update(sql, params);
    }

    public int updateRequestState(String sessionId, String state) {
        String sql = "update " + PAYMENT_REQUEST_TABLE + " set `state` = :state where session_id = :sessionId";
        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("sessionId", sessionId);
        params.addValue("state", state);
        return jdbcTemplate.update(sql, params);
    }

    public List<UserSubscribeInfo> getUserSubscribeRobotIds(String userId) {
        return null;
    }
}
