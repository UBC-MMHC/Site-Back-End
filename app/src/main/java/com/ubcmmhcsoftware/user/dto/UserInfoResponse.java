package com.ubcmmhcsoftware.user.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class UserInfoResponse {
    String sub;
    String email;
    String name;
    boolean newsletterSubscription;
    List<String> roles;
}
