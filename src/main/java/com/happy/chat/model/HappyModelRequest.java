package com.happy.chat.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HappyModelRequest {
    private Current current;
    private List<History> history;
    private double temperature;
    @JsonProperty("max_new_tokens")
    private int maxNewToken;
    @JsonProperty("history_max_len")
    private int historyMaxLen;
    @JsonProperty("top_p")
    private double topP;
    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("presence_penalty")
    private Double presencePenalty;
    @JsonProperty("frequency_penalty")
    private Double frequencyPenalty;

    @Data
    @Builder
    public static class Current {
        private String u;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class History {
        private String u;
        private String b;
    }
}
