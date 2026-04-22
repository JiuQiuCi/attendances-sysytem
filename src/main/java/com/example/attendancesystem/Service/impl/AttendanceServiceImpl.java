package com.example.attendancesystem.Service.impl;

import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public Attendance addAttendance(Attendance attendance) {
        // 可增加业务校验（如日期不能为空）
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
    public void deleteAttendance(Integer id) {
        attendanceRepository.deleteById(id);
    }
}