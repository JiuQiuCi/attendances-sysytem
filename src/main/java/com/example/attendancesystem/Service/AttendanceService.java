package com.example.attendancesystem.Service;

import com.example.attendancesystem.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceService {

    // 新增签到和签退
    Attendance checkIn(Integer studentId, Integer courseId, LocalDate date);
    Attendance checkOut(Integer studentId, Integer courseId, LocalDate date);

    // 手动标记考勤状态（present/absent/late/early_leave）
    Attendance markAttendance(Integer studentId, Integer courseId, LocalDate date, String status);

    // 原有方法
    Attendance saveAttendance(Attendance attendance);
    Optional<Attendance> getAttendanceById(Integer id);
    List<Attendance> getAttendancesByStudent(Integer studentId);
    List<Attendance> getAttendancesByCourse(Integer courseId);
    List<Attendance> getAttendancesByStudentAndDateRange(Integer studentId, LocalDate start, LocalDate end);
    List<Attendance> getAttendancesByCourseAndDate(Integer courseId, LocalDate date);
    void deleteAttendance(Integer id);

    Page<Attendance> getAttendancesPage(Pageable pageable);
    Page<Attendance> searchAttendances(Integer courseId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // 导出所有符合条件的考勤记录（不分页）
    List<Attendance> exportAttendances(Integer courseId, LocalDate startDate, LocalDate endDate);
}