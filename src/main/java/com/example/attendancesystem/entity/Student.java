package com.example.attendancesystem.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")  // 表名，避免与 user 冲突
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id", unique = true, nullable = false, length = 20)
    private String studentId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 无参构造（必须）
    public Student() {}

    // 有参构造（可选）
    public Student(String studentId, String name, String className) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.createdAt = LocalDateTime.now();
    }

    // Getter / Setter
    // ... 省略，请自行生成或使用 Lombok
}