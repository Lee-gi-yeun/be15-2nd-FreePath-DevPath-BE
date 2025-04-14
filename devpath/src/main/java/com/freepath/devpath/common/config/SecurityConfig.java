package com.freepath.devpath.common.config;

import com.freepath.devpath.common.auth.service.GoogleOAuth2Service;
import com.freepath.devpath.common.jwt.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // @PreAuthorize, @PostAuthorize 사용을 위해 설정
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final GoogleOAuth2Service googleOAuth2Service;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(restAuthenticationEntryPoint) // 인증 실패
                                .accessDeniedHandler(restAccessDeniedHandler)) // 인가 실패
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers(HttpMethod.POST,
                                        "/user/signup", "/user/login", "/user/refresh","/user/signup/temp","/user/email/check",
                                        "/user/find-id", "/user/verify-email", "/user/reset-password").permitAll()

                                .requestMatchers(
                                        "/swagger-ui/**",
                                        "/swagger-resources/**",
                                        "/v3/api-docs/**",
                                        "/webjars/**"
                                ).permitAll()
                                .requestMatchers(HttpMethod.GET, "/board/search", "/board/search/content", "/board/category/{category-id:[\\d]+}", "/board/{board-id:[\\d]+}").permitAll()
                                .requestMatchers(HttpMethod.DELETE, "/user").authenticated()
                                .requestMatchers(HttpMethod.GET, "/ws-stomp/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/board/*/comments").permitAll()
                                .requestMatchers(HttpMethod.GET, "/board/*/like/count").permitAll()
                                .requestMatchers(HttpMethod.GET, "/comment/*/like/count").permitAll()
                                .requestMatchers("/user/info", "/mypage/**", "/user/change-password","/user/change-email", "/user/dev-mbti").authenticated()
                                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                                .requestMatchers("/interview-room/**").hasAuthority("USER")
                                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                                .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler) // 로그인 성공 시 토큰 리턴 처리
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(googleOAuth2Service) // 사용자 정보 처리 커스터마이징
                        )
                )
                // 커스텀 인증 필터(JWT 토큰 사용하여 확인)를 인증 필터 앞에 삽입
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }
}
