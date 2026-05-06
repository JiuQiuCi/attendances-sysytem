package com.example.attendancesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                // 放行所有静态资源、登录注册页面及认证API
                .antMatchers(
                        "/", "/register", "/login", "/index",
                        "/css/**", "/js/**", "/favicon.ico",
                        "/auth/**"
                ).permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
                .and()
                .formLogin().disable()  // 完全禁用默认登录页
                .httpBasic();
        return http.build();
    }
}