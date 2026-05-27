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
                // 放行所有静态资源、登录注册、认证API
                .antMatchers(
                        "/", "/register", "/login", "/index",
                        "/css/**", "/js/**", "/images/**", "/favicon.ico", "/file/**",
                        "/auth/**"
                ).permitAll()
                // 放行学生管理模块（页面 + API）
                .antMatchers("/student/**").permitAll()
                // 放行考勤管理模块
                .antMatchers("/attendance/**").permitAll()
                // 放行课程管理模块
                .antMatchers("/course/**").permitAll()
                // 放行数据报表模块
                .antMatchers("/report/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
                .and()
                .httpBasic().disable()
                .formLogin().disable();

        return http.build();
    }
}