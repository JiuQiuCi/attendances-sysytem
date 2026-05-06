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
                // 关键：授权请求配置
                .authorizeRequests(authorize -> authorize
                        // 1. 明确放行所有公共页面（Thymeleaf模板路由）
                        .antMatchers("/", "/register", "/login", "/index").permitAll()
                        // 2. 放行所有静态资源（CSS, JS, 图片等）
                        .antMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        // 3. 放行所有认证相关的API接口
                        .antMatchers("/auth/**").permitAll()
                        // 4. 其他任何请求都需要认证
                        .anyRequest().authenticated()
                )
                // 5. 禁用HTTP Basic认证（避免弹出浏览器登录框）
                .httpBasic().disable()
                // 6. 禁用默认的表单登录页
                .formLogin().disable();

        return http.build();
    }
}