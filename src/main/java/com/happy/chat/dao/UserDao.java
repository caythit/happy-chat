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

import com.happy.chat.domain.User;

@Lazy
@Component
public class UserDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    public static final String TABLE_NAME = "user_info";
    public static final String ALL_FIELD = "id,dummy_user_id,user_id,user_prefer_info,user_name,user_pwd,email,"
            + "phone,pwd_salt,extra_info,create_time,update_time";

    private final RowMapper<User> userRowMapper = new BeanPropertyRowMapper<>(User.class);

    public User getUserByEmail(String email) {
        StringBuilder sqlBuilder = new StringBuilder("select ").append(ALL_FIELD).append(" from ")
                .append(TABLE_NAME)
                .append(" where email = :email");

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", email);
        List<User> uses = jdbcTemplate.query(sqlBuilder.toString(), params, userRowMapper);
        if (CollectionUtils.isNotEmpty(uses)) {
            return uses.get(0);
        }
        return null;
    }

    public User getUserById(String userId) {
        StringBuilder sqlBuilder = new StringBuilder("select ").append(ALL_FIELD).append(" from ")
                .append(TABLE_NAME)
                .append(" where user_id = :userId");

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);

        List<User> uses = jdbcTemplate.query(sqlBuilder.toString(), params, userRowMapper);
        if (CollectionUtils.isNotEmpty(uses)) {
            return uses.get(0);
        }
        return null;
    }

    public User getDummyUserById(String userId) {
        StringBuilder sqlBuilder = new StringBuilder("select ").append(ALL_FIELD).append(" from ")
                .append(TABLE_NAME)
                .append(" where dummy_user_id = :userId");

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);

        List<User> uses = jdbcTemplate.query(sqlBuilder.toString(), params, userRowMapper);
        if (CollectionUtils.isNotEmpty(uses)) {
            return uses.get(0);
        }
        return null;
    }

    public int insert(User user) {
        String sql = "insert into " + TABLE_NAME
                + " (user_id, user_name, user_pwd,email, phone, pwd_salt, extra_info, create_time, update_time) "
                + " values (:userId, :userName, :userPwd, :email, :phone, :pwdSalt, :extraInfo, :createTime, :updateTime) "
                + " on duplicate key update update_time = :updateTime";
        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("userId", user.getUserId());
        params.addValue("userName", user.getUserName());
        params.addValue("userPwd", user.getUserPwd());
        if (StringUtils.isNotEmpty(user.getEmail())) {
            params.addValue("email", user.getEmail());
        }
        if (StringUtils.isNotEmpty(user.getPhone())) {
            params.addValue("phone", user.getPhone());
        }
        params.addValue("pwdSalt", user.getPwdSalt());
        if (StringUtils.isNotEmpty(user.getExtraInfo())) {
            params.addValue("extraInfo", user.getExtraInfo());
        }

        params.addValue("createTime", System.currentTimeMillis());
        params.addValue("updateTime", System.currentTimeMillis());
        return jdbcTemplate.update(sql, params);
    }

    public int insertForDummy(String userId) {
        String sql = "insert into " + TABLE_NAME
                + " (dummy_user_id, create_time, update_time) "
                + " values (:userId, :createTime, :updateTime) on duplicate key update update_time = :updateTime";
        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("userId", userId);
        params.addValue("createTime", System.currentTimeMillis());
        params.addValue("updateTime", System.currentTimeMillis());
        return jdbcTemplate.update(sql, params);
    }

    public int updateUserPreferInfo(String userId, String preferInfo) {
        String sql = "update " + TABLE_NAME
                + " set user_prefer_info = :preferInfo where dummy_user_id = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("preferInfo", preferInfo);
        params.addValue("userId", userId);
        return jdbcTemplate.update(sql, params);
    }

    public int updateUserPassword(String userId, String encryptPwd) {
        String sql = "update " + TABLE_NAME
                + " set user_pwd = :pwd where user_id = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("pwd", encryptPwd);
        params.addValue("userId", userId);
        return jdbcTemplate.update(sql, params);
    }

    public int updateUserEmail(String userId, String email) {
        String sql = "update " + TABLE_NAME
                + " set email = :email where user_id = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("email", email);
        params.addValue("userId", userId);
        return jdbcTemplate.update(sql, params);
    }

    public int updateUserName(String userId, String userName) {
        String sql = "update " + TABLE_NAME
                + " set user_name = :userName where user_id = :userId";
        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("userName", userName);
        params.addValue("userId", userId);
        return jdbcTemplate.update(sql, params);
    }
}
