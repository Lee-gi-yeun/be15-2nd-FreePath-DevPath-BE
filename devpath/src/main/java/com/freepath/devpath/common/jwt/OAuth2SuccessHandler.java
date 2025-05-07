package com.freepath.devpath.common.jwt;

import com.freepath.devpath.common.auth.domain.RefreshToken;
import com.freepath.devpath.common.exception.ErrorCode;
import com.freepath.devpath.user.command.repository.UserCommandRepository;
import com.freepath.devpath.user.exception.UserException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, RefreshToken> redisTemplate;
    private final UserCommandRepository userCommandRepository;

    @Value("${app.oauth2.redirect-url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String email = ((org.springframework.security.oauth2.core.user.DefaultOAuth2User) authentication.getPrincipal()).getAttribute("email");

        // 제재된 유저인지 먼저 확인
        boolean isRestrictedUser = userCommandRepository.existsByLoginIdAndLoginMethodAndIsUserRestricted(
                email, "GOOGLE", "Y"
        );

        if (isRestrictedUser) {
            throw new UserException(ErrorCode.RESTRICTED_USER);
        }

        // 로그인 처리
        userCommandRepository.findByLoginIdAndLoginMethodAndUserDeletedAtIsNull(email, "GOOGLE").ifPresentOrElse(user -> {
            // 토큰 발급
            String accessToken = jwtTokenProvider.createToken(String.valueOf(user.getUserId()), user.getUserRole().name());
            String refreshToken = jwtTokenProvider.createRefreshToken(String.valueOf(user.getUserId()), user.getUserRole().name());

            RefreshToken refreshTokenObj = RefreshToken.builder()
                    .token(refreshToken)
                    .build();

            redisTemplate.opsForValue().set(
                    String.valueOf(user.getUserId()),
                    refreshTokenObj,
                    Duration.ofDays(7)
            );

            try {
                response.sendRedirect(redirectUrl);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }, () -> {
            // 유저가 없으면 회원가입 페이지로 리디렉션
            try {
                response.sendRedirect(redirectUrl + "/user/signup/google");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}