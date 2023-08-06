package com.happy.chat.helper;

import static com.happy.chat.constants.Constant.CHAT_FROM_ROBOT;
import static com.happy.chat.constants.Constant.CHAT_FROM_USER;
import static com.happy.chat.constants.Constant.DATA;
import static com.happy.chat.constants.Constant.EXTRA_INFO_MESSAGE_PAY_TIPS;
import static com.happy.chat.constants.Constant.EXTRA_INFO_MESSAGE_WARN_TIPS;
import static com.happy.chat.constants.Constant.MESSAGE_ID_PREFIX;
import static com.happy.chat.uitls.PrometheusUtils.perf;
import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.happy.chat.domain.FlirtopiaChat;
import com.happy.chat.domain.IceBreakWord;
import com.happy.chat.domain.Robot;
import com.happy.chat.enums.ErrorEnum;
import com.happy.chat.model.ChatResponse;
import com.happy.chat.service.ChatService;
import com.happy.chat.service.PaymentService;
import com.happy.chat.service.RobotService;
import com.happy.chat.uitls.ApiResult;
import com.happy.chat.uitls.CommonUtils;
import com.happy.chat.uitls.ObjectMapperUtils;
import com.happy.chat.view.FlirtopiaChatView;
import com.happy.chat.view.IceBreakWordView;
import com.happy.chat.view.UserChatListView;

import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;

@Lazy
@Component
@Slf4j
public class ChatApiHelper {
    private final String prometheusName = "chat";
    private final String prometheusHelp = "chat_operation";

    @Autowired
    private CollectorRegistry chatRegistry;

    @Autowired
    private ChatService chatService;

    @Autowired
    private RobotService robotService;

    @Autowired
    private PaymentService paymentService;

    public Map<String, Object> getIceBreakWords(String robotId) {
        Map<String, Object> result = ApiResult.ofSuccess();
        List<IceBreakWord> iceBreakWords = chatService.getIceBreakWordsByRobot(robotId);
        if (CollectionUtils.isEmpty(iceBreakWords)) {
            log.error("getIceBreakWordsByRobot empty {}", robotId);
            perf(chatRegistry, prometheusName, prometheusHelp, "ice_break_get_failed_by_empty", robotId);
            return result;
        }

        List<IceBreakWordView> iceBreakWordViews = iceBreakWords.stream()
                .map(IceBreakWordView::convertIceBreakWord)
                .collect(Collectors.toList());
        result.put(DATA, iceBreakWordViews);
        perf(chatRegistry, prometheusName, prometheusHelp, "ice_break_get_success", robotId);
        return result;
    }

    public Map<String, Object> listUserChat(String userId) {
        List<FlirtopiaChat> flirtopiaChats = chatService.getUserHistoryChats(userId);
        if (CollectionUtils.isEmpty(flirtopiaChats)) {
            log.error("listUserChat empty {}", userId);
            perf(chatRegistry, prometheusName, prometheusHelp, "user_history_chat_empty", userId);
            return ApiResult.ofSuccess();
        }

        Set<String> robotIdSet = flirtopiaChats.stream()
                .map(FlirtopiaChat::getRobotId)
                .collect(Collectors.toSet());
        Map<String, Robot> robotMap = robotService.batchGetRobotById(robotIdSet);
        List<String> userSubscribeRobots = paymentService.getUserSubscribeRobotIds(userId);

        // key=robotId， value为最新的一次聊天信息
        Map<String, FlirtopiaChat> robotChatMsgMap = flirtopiaChats.stream()
                .collect(groupingBy(FlirtopiaChat::getRobotId))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .sorted(Comparator.comparing(FlirtopiaChat::getCreateTime).reversed())
                        .collect(Collectors.toList())
                        .get(0)));


        List<UserChatListView> userChatListViews = new ArrayList<>();
        for (Map.Entry<String, FlirtopiaChat> entry : robotChatMsgMap.entrySet()) {
            String robotId = entry.getKey();
            if (robotMap.containsKey(robotId)) {
                Robot robot = robotMap.get(robotId);
                UserChatListView userHistoryChatListView = new UserChatListView();
                // 是否订阅
                userHistoryChatListView.setSubscribeRobot(userSubscribeRobots.contains(robotId));
                userHistoryChatListView.setRobotId(robotId);
                userHistoryChatListView.setRobotName(robot.getName());
                userHistoryChatListView.setRobotHeadUrl(robot.getHeadUrl());

                FlirtopiaChat latestChat = robotChatMsgMap.get(robotId);
                userHistoryChatListView.setLastMessageContent(latestChat.getContent());
                userHistoryChatListView.setLastMessageSendTime(latestChat.getCreateTime());

                userChatListViews.add(userHistoryChatListView);
            } else {
                log.error("robotMap not contains {}", robotId);
            }
        }

        Map<String, Object> result = ApiResult.ofSuccess();
        // 再做一次排序。按最后发送时间降序
        userChatListViews = userChatListViews.stream()
                .sorted(Comparator.comparing(UserChatListView::getLastMessageSendTime).reversed())
                .collect(Collectors.toList());
        result.put(DATA, userChatListViews);
        perf(chatRegistry, prometheusName, prometheusHelp, "user_history_chat_success", userId);
        return result;

    }

    public Map<String, Object> getUserRobotHistoryChats(String userId, String robotId) {
        List<FlirtopiaChat> flirtopiaChats = chatService.getUserRobotHistoryChats(userId, robotId);
        if (CollectionUtils.isEmpty(flirtopiaChats)) {
            log.error("getUserRobotHistoryChats empty {} {} ", userId, robotId);
            perf(chatRegistry, prometheusName, prometheusHelp, "user_robot_chat_empty", userId);
            return ApiResult.ofSuccess();
        }
        Map<String, Object> result = ApiResult.ofSuccess();
        result.put(DATA, flirtopiaChats.stream().map(FlirtopiaChatView::convertChat).collect(Collectors.toList()));
        return result;
    }

    /**
     * 聊天
     * （1）写DB，记录用户发送的内容
     * （2）判定是否需要请求模型，若否，直接用特定内容，若是，则请求模型返回内容
     * （3）基于一定规则判定是否需要展示付费卡片，
     * 若是通过extra info记录下来，同时将返回内容写入DB，记录robot发送的内容
     *
     * @param content
     * @return
     */
    public Map<String, Object> request(String userId, String robotId, String content) {
        FlirtopiaChat userRequestMessage = new FlirtopiaChat();
        userRequestMessage.setUserId(userId);
        userRequestMessage.setRobotId(robotId);
        userRequestMessage.setMessageId(CommonUtils.uuid(MESSAGE_ID_PREFIX));
        userRequestMessage.setMessageFrom(CHAT_FROM_USER);
        userRequestMessage.setMessageType("normal");
        userRequestMessage.setContent(content);
        userRequestMessage.setCreateTime(System.currentTimeMillis());
        userRequestMessage.setUpdateTime(System.currentTimeMillis());
        int effectRow = chatService.insert(userRequestMessage);
        if (effectRow <= 0) {
            log.error("request insert db failed {} {} {}", userId, robotId, content);
            perf(chatRegistry, prometheusName, prometheusHelp, "request_chat_failed_by_req_db_error", userId, robotId);
            return ApiResult.ofFail(ErrorEnum.SERVER_ERROR);
        }

        // 请求聊天回复，逻辑复杂
        ChatResponse chatResponse = chatService.requestChat(userId, robotId, content);

        // 没拿到
        if (chatResponse == null) {
            log.error("response return null {} {} {}", userId, robotId, content);
            perf(chatRegistry, prometheusName, prometheusHelp, "request_chat_failed_by_no_resp", userId, robotId);
            return ApiResult.ofFail(ErrorEnum.CHAT_NO_RESP);
        }

        FlirtopiaChat robotRespMessage = new FlirtopiaChat();
        robotRespMessage.setUserId(userId);
        robotRespMessage.setRobotId(robotId);
        robotRespMessage.setMessageId(CommonUtils.uuid(MESSAGE_ID_PREFIX));
        robotRespMessage.setMessageFrom(CHAT_FROM_ROBOT);
        robotRespMessage.setMessageType("normal");
        robotRespMessage.setContent(chatResponse.getContent());
        robotRespMessage.setCreateTime(System.currentTimeMillis());
        robotRespMessage.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isNotEmpty(chatResponse.getPayTips())) {
            // 付费后要变更成付费的文案
            robotRespMessage.setExtraInfo(
                    ObjectMapperUtils.toJSON(ImmutableMap.of(EXTRA_INFO_MESSAGE_PAY_TIPS, chatResponse.getPayTips())));
        }

        if (StringUtils.isNotEmpty(chatResponse.getWarnTips())) {
            // 提示文案
            robotRespMessage.setExtraInfo(
                    ObjectMapperUtils.toJSON(ImmutableMap.of(EXTRA_INFO_MESSAGE_WARN_TIPS, chatResponse.getWarnTips())));
        }

        Map<String, Object> result = ApiResult.ofSuccess();
        effectRow = chatService.insert(robotRespMessage);

        if (effectRow <= 0) {
            log.error("response insert db failed {} {} {}", userId, robotId, content);
            perf(chatRegistry, prometheusName, prometheusHelp, "request_chat_resp_failed_by_db_error", userId, robotId);
            return ApiResult.ofFail(ErrorEnum.SERVER_ERROR);
        }
        result.put(DATA, FlirtopiaChatView.convertChat(robotRespMessage));
        return result;

    }
}
