package com.example.attendancesystem.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String role;   // admin, teacher, student

    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 无参构造
    public User() {}

    // 全参构造（可选）
    public User(Integer id, String username, String password, String role, String name, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.createdAt = createdAt;
    }
}