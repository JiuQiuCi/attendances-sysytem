package com.example.attendancesystem.Service.impl;

import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.Service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    // ========== 原有方法实现（保留） ==========
    @Override
    public Attendance saveAttendance(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    @Override
    public Optional<Attendance> getAttendanceById(Integer id) {
        return attendanceRepository.findById(id);
    }

    @Override
    public List<Attendance> getAttendancesByStudent(Integer studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    @Override
    public List<Attendance> getAttendancesByCourse(Integer courseId) {
        return attendanceRepository.findByCourseId(courseId);
    }

    @Override
    public List<Attendance> getAttendancesByStudentAndDateRange(Integer studentId, LocalDate start, LocalDate end) {
        return attendanceRepository.findByStudentIdAndAttendanceDateBetween(studentId, start, end);
    }

    @Override
    public List<Attendance> getAttendancesByCourseAndDate(Integer courseId, LocalDate date) {
        return attendanceRepository.findAttendancesByCourseAndDate(courseId, date);
    }

    @Override
    public void deleteAttendance(Integer id) {
        attendanceRepository.deleteById(id);
    }

    // ========== 新增分页方法实现 ==========
    @Override
    public Page<Attendance> getAttendancesPage(Pageable pageable) {
        return attendanceRepository.findAll(pageable);
    }

    @Override
    public Page<Attendance> getAttendancesByStudentPage(Integer studentId, Pageable pageable) {
        return attendanceRepository.findByStudentId(studentId, pageable);
    }

    @Override
    public Page<Attendance> getAttendancesByCoursePage(Integer courseId, Pageable pageable) {
        return attendanceRepository.findByCourseId(courseId, pageable);
    }

    // ========== 新增多条件查询实现 ==========
    @Override
    public Page<Attendance> searchAttendances(Specification<Attendance> spec, Pageable pageable) {
        return attendanceRepository.findAll(spec, pageable);
    }
}