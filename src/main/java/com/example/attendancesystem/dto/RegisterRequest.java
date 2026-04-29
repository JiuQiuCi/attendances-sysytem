package com.example.attendancesystem.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private String role;   // 可选：默认 student
}