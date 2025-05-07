package com.freepath.devpath.common.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String role;
}
