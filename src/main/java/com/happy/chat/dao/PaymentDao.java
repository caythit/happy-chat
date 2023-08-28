package com.happy.chat.dao;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
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
    public static final String USER_SUBSCRIBE_INFO_TABLE = "user_subscribe_info";

    private final RowMapper<PaymentItem> paymentItemRowMapper = new BeanPropertyRowMapper<>(PaymentItem.class);
    private final RowMapper<UserSubscribeInfo> userSubscribeInfoRowMapper = new BeanPropertyRowMapper<>(UserSubscribeInfo.class);

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
        String sql = "select * from " + USER_SUBSCRIBE_INFO_TABLE + " where user_id = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        return jdbcTemplate.query(sql, params, userSubscribeInfoRowMapper);
    }

    public int updateUserSubscribeRobot(String userId, String robotId, long expire) {
        String sql = "insert into " + USER_SUBSCRIBE_INFO_TABLE
                + " (user_id, robot_id, expire_mills, create_time, update_time) "
                + " values (:userId, :robotId, :expireMills, :createTime, :updateTime) "
                + " on duplicate key update expire_mills = expireMills, update_time = :updateTime";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        params.addValue("robotId", robotId);
        params.addValue("createTime", System.currentTimeMillis());
        params.addValue("updateTime", System.currentTimeMillis());
        params.addValue("expireMills", expire);
        return jdbcTemplate.update(sql, params);
    }

    public PaymentItem getPaymentRequest(String sessionId) {
        String sql = "select * from " + PAYMENT_REQUEST_TABLE + " where session_id = :sessionId limit 1";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("sessionId", sessionId);

        List<PaymentItem> items = jdbcTemplate.query(sql, params, paymentItemRowMapper);
        if (CollectionUtils.isNotEmpty(items)) {
            return items.get(0);
        }
        return null;

    }
}
