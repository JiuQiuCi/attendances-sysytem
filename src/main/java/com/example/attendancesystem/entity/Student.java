package com.example.attendancesystem.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
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

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}