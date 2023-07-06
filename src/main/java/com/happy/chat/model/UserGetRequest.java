package com.happy.chat.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserGetRequest {
    private String userName;
    private String userId;
}
