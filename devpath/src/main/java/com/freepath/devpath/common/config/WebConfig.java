package com.freepath.devpath.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 첫 번째 레벨 경로 (예: /user, /about)
        registry.addViewController("/{spring:[a-zA-Z0-9-_]+}")
                .setViewName("forward:/index.html");

        // 두 번째 레벨 경로 (예: /user/signup)
        registry.addViewController("/{spring:[a-zA-Z0-9-_]+}/{spring2:[a-zA-Z0-9-_]+}")
                .setViewName("forward:/index.html");

        // 세 번째 레벨 경로 (예: /user/signup/google)
        registry.addViewController("/{spring:[a-zA-Z0-9-_]+}/{spring2:[a-zA-Z0-9-_]+}/{spring3:[a-zA-Z0-9-_]+}")
                .setViewName("forward:/index.html");
    }
}
