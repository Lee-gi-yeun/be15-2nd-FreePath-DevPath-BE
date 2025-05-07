package com.freepath.devpath.user.query.dto.response;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
    private String loginId;
    @Email
    private String email;
    private String userName;
    private String nickname;
    private String developerPersonality;
    private String itNewsSubscription;
}
