package com.example.attendancesystem.controller;

import com.example.attendancesystem.common.Result;
import com.example.attendancesystem.dto.LoginRequest;
import com.example.attendancesystem.dto.RegisterRequest;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        boolean authenticated = userService.authenticate(request.getUsername(), request.getPassword());
        if (authenticated) {
            return Result.success("登录成功");
        } else {
            return Result.error(401, "用户名或密码错误");
        }
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        System.out.println("注册请求: " + request);   // 检查 password 字段
        try {
            User newUser = userService.register(
                    request.getUsername(),
                    request.getPassword(),   // 注意这里的 getPassword()
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
}