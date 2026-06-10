package com.example.attendancesystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public CustomAuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public CustomAccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                            CustomAuthenticationEntryPoint entryPoint,
                                            CustomAccessDeniedHandler deniedHandler) throws Exception {
        http
            // ── CSRF: cookie-based so JS can read and send via header ──
            .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringAntMatchers("/auth/login", "/auth/register", "/attendance/checkin")
                .and()

            // ── Route authorization (order matters — more specific first) ──
            .authorizeRequests()
                // Static resources — public
                .antMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // Login & register pages
                .antMatchers("/login", "/register").permitAll()
                // Auth API endpoints
                .antMatchers("/auth/login", "/auth/register").permitAll()
                // Auth session check — any logged-in user
                .antMatchers("/auth/me").authenticated()
                .antMatchers("/auth/logout").authenticated()
                // Dashboard
                .antMatchers("/index").authenticated()
                // Root — public (PageController redirects based on auth state)
                .antMatchers("/").permitAll()
                // Student management — pages & APIs gated by @PreAuthorize or data-required-role
                .antMatchers("/student/**").authenticated()
                // Teacher-only: user/teacher management
                .antMatchers("/user/**").hasRole("TEACHER")
                // Teacher-only: file upload / Excel import
                .antMatchers("/file/**").hasRole("TEACHER")
                // Reports — page & API gated by role-specific logic
                .antMatchers("/report/**").authenticated()
                // Mobile checkin — permitAll so auth-check.js can handle login flow to /mobile-login
                .antMatchers("/mobile-checkin").permitAll()
                .antMatchers("/mobile-login").permitAll()
                // Diagnostic — ping is public for phone connectivity testing
                .antMatchers("/diagnostic/ping").permitAll()
                .antMatchers("/diagnostic/**").authenticated()
                .antMatchers("/diagnostic-page").authenticated()
                .antMatchers("/attendance/verify-student").authenticated()
                .antMatchers("/attendance/checkin").authenticated()
                // Both roles: attendance (pages + API)
                .antMatchers("/attendance/**").authenticated()
                // Both roles: course read; write gated by @PreAuthorize
                .antMatchers("/course/**").authenticated()
                // Both roles: leave; approve gated by @PreAuthorize
                .antMatchers("/api/leave/**").authenticated()
                // Catch-all
                .anyRequest().authenticated()
                .and()

            // ── Exception handling ──
            .exceptionHandling()
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(deniedHandler)
                .and()

            // ── Session management ──
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()

            // ── Disable built-in login/logout/basic (handled by AuthController) ──
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable();

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
