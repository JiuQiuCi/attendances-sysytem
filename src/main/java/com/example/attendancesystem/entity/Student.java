package com.example.attendancesystem.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "students")
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

    @Column(length = 10)
    private String gender;   // 男/女 (保留字段，学生端不显示)

    @Column(length = 100)
    private String college;  // 学院

    @Column(length = 50)
    private String major;    // 专业

    @Column(length = 20)
    private String grade;    // 年级，如 2024级

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 20)
    private String phone;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}