package com.example.attendancesystem.controller;

import com.example.attendancesystem.common.Result;
import com.example.attendancesystem.dto.LoginRequest;
import com.example.attendancesystem.dto.RegisterRequest;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request,
                                              HttpServletRequest httpRequest) {
        try {
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
            Authentication auth = authenticationManager.authenticate(token);

            // Set security context and create session
            SecurityContextHolder.getContext().setAuthentication(auth);
            httpRequest.getSession(true);

            // Return user info
            User user = (User) auth.getPrincipal();
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("username", user.getUsername());
            userInfo.put("name", user.getName());
            userInfo.put("role", user.getRole());

            return Result.success("登录成功", userInfo);
        } catch (AuthenticationException e) {
            return Result.error(401, "用户名或密码错误");
        }
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        System.out.println("注册请求: " + request);
        try {
            User newUser = userService.register(
                    request.getUsername(),
                    request.getPassword(),
                    request.getName(),
                    request.getRole()
            );
            newUser.setPassword(null);
            return Result.success("注册成功", newUser);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "注册失败：" + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Result.success("已退出登录");
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return Result.error(401, "未登录");
        }
        User user = (User) auth.getPrincipal();
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("name", user.getName());
        data.put("role", user.getRole());
        return Result.success(data);
    }
}
