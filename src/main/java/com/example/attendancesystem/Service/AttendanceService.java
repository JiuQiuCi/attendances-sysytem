package com.example.attendancesystem.Service;

import com.example.attendancesystem.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceService {

    // ========== 原有方法（保留） ==========
    Attendance saveAttendance(Attendance attendance);
    Optional<Attendance> getAttendanceById(Integer id);
    List<Attendance> getAttendancesByStudent(Integer studentId);
    List<Attendance> getAttendancesByCourse(Integer courseId);
    List<Attendance> getAttendancesByStudentAndDateRange(Integer studentId, LocalDate start, LocalDate end);
    List<Attendance> getAttendancesByCourseAndDate(Integer courseId, LocalDate date);
    void deleteAttendance(Integer id);

    // ========== 新增分页方法 ==========
    Page<Attendance> getAttendancesPage(Pageable pageable);
    Page<Attendance> getAttendancesByStudentPage(Integer studentId, Pageable pageable);
    Page<Attendance> getAttendancesByCoursePage(Integer courseId, Pageable pageable);

    // ========== 新增多条件动态查询 ==========
    Page<Attendance> searchAttendances(Specification<Attendance> spec, Pageable pageable);
}