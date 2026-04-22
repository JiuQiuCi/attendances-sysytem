package com.example.attendancesystem.service;

import com.example.attendancesystem.entity.Attendance;
import java.util.List;
import java.util.Optional;

public interface AttendanceService {
    Attendance addAttendance(Attendance attendance);
    Optional<Attendance> getAttendanceById(Integer id);
    List<Attendance> getAttendancesByStudent(Integer studentId);
    List<Attendance> getAttendancesByCourse(Integer courseId);
    void deleteAttendance(Integer id);
}