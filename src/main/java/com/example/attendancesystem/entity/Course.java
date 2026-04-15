package com.example.attendancesystem.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "teacher_id", nullable = false)
    private Integer teacherId;  // 教师ID，关联 users 表

    @Column(name = "classroom_layout", columnDefinition = "TEXT")
    private String classroomLayout;  // 存储座位布局JSON

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 无参构造
    public Course() {}

    // 有参构造（可选）
    public Course(String name, Integer teacherId, String classroomLayout) {
        this.name = name;
        this.teacherId = teacherId;
        this.classroomLayout = classroomLayout;
        this.createdAt = LocalDateTime.now();
    }

    // Getter/Setter 省略（可使用 Lombok 或手动生成）
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }
    public String getClassroomLayout() { return classroomLayout; }
    public void setClassroomLayout(String classroomLayout) { this.classroomLayout = classroomLayout; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}