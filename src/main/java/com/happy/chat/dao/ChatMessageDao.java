package com.happy.chat.dao;

import static java.lang.String.format;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.happy.chat.domain.ChatMessage;
import com.happy.chat.domain.IceBreakWord;

@Lazy
@Component
public class ChatMessageDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public static final String ICE_BREAK_WORD_TABLE_NAME = "robot_ice_break_word";
    public static final String CHAT_TABLE_NAME = "chat_message";

    private final RowMapper<IceBreakWord> iceBreakWordRowMapper = new BeanPropertyRowMapper<>(IceBreakWord.class);
    private final RowMapper<ChatMessage> chatMessageRowMapper = new BeanPropertyRowMapper<>(ChatMessage.class);


    public List<IceBreakWord> getRobotIceBreakWords(String robotId) {
        String sql = format("select * from %s where robot_id = :robotId)", ICE_BREAK_WORD_TABLE_NAME);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("robotId", robotId);

        return jdbcTemplate.query(sql, params, iceBreakWordRowMapper);
    }

    public List<ChatMessage> getUserChatList(String userId) {
        String sql = format("select * from %s where user_id = :userId)", CHAT_TABLE_NAME);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        return jdbcTemplate.query(sql, params, chatMessageRowMapper);
    }

    public int insertChat(ChatMessage chatMessage) {
        String sql = "insert into " + CHAT_TABLE_NAME
                + " (user_id, robot_id, message_id,message_type, message_from, content, extra_info, create_time, update_time) "
                + " values (:userId, :robotId, :messageId, :messageType, :messageFrom, :content, :extraInfo, :createTime, :updateTime) "
                + " on duplicate key update modify_time = :modifyTime";
        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("userId", chatMessage.getUserId());
        params.addValue("robotId", chatMessage.getRobotId());
        params.addValue("messageId", chatMessage.getMessageId());
        params.addValue("messageType", chatMessage.getMessageType());
        params.addValue("messageFrom", chatMessage.getMessageFrom());
        params.addValue("content", chatMessage.getContent());

        if (StringUtils.isNotEmpty(chatMessage.getExtraInfo())) {
            params.addValue("extraInfo", chatMessage.getExtraInfo());
        }

        params.addValue("createTime", System.currentTimeMillis());
        params.addValue("updateTime", System.currentTimeMillis());
        return jdbcTemplate.update(sql, params);
    }

    public int batchInsertChat(List<ChatMessage> chatMessages) {
        String sql = "INSERT ignore INTO " + CHAT_TABLE_NAME
                + " (user_id, robot_id, message_id,message_type, message_from, content, extra_info, create_time, update_time) "
                + " values (:userId, :robotId, :messageId, :messageType, :messageFrom, :content, :extraInfo, :createTime, :updateTime) "
                + " on duplicate key update modify_time = :modifyTime";

        return jdbcTemplate.batchUpdate(sql,
                chatMessages.stream()
                        .map(chatMessage -> {
                            MapSqlParameterSource params = new MapSqlParameterSource();

                            params.addValue("userId", chatMessage.getUserId());
                            params.addValue("robotId", chatMessage.getRobotId());
                            params.addValue("messageId", chatMessage.getMessageId());
                            params.addValue("messageType", chatMessage.getMessageType());
                            params.addValue("messageFrom", chatMessage.getMessageFrom());
                            params.addValue("content", chatMessage.getContent());

                            if (StringUtils.isNotEmpty(chatMessage.getExtraInfo())) {
                                params.addValue("extraInfo", chatMessage.getExtraInfo());
                            }

                            params.addValue("createTime", System.currentTimeMillis());
                            params.addValue("updateTime", System.currentTimeMillis());
                            return params;
                        })
                        .toArray(MapSqlParameterSource[]::new)).length;
    }

    public List<ChatMessage> getUserRobotChats(String userId, String robotId) {
        String sql = format("select * from %s where user_id = :userId and robot_id = :robotId)", CHAT_TABLE_NAME);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        params.addValue("robotId", robotId);
        return jdbcTemplate.query(sql, params, chatMessageRowMapper);

    }
}
