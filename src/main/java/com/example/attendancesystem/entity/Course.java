package com.example.attendancesystem.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(name = "teacher_id")
    private Integer teacherId;

    @Column(name = "teacher_name", length = 50)
    private String teacherName;

    @Column(length = 50)
    private String classroom;

    @Column(name = "classroom_layout")
    private String classroomLayout;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "week_day")
    @Enumerated(EnumType.STRING)
    private DayOfWeek weekDay;

    private Integer semester;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
