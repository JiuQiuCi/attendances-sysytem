package com.example.attendancesystem.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendances")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(nullable = false, length = 20)
    private String status; // present, absent, late

    @Column(name = "seat_position", length = 50)
    private String seatPosition;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 无参构造
    public Attendance() {}

    // Getter/Setter 省略（可使用 Lombok）
}