package com.example.attendancesystem.Service;

import com.example.attendancesystem.entity.AttendanceRecord;
import com.example.attendancesystem.entity.Student;
import java.util.List;
import java.util.Optional;

public interface StudentService {
    Optional<Student> getStudentById(String studentId);
    List<Student> getStudentsByClass(String className, int page, int pageSize);
    String updateAttendance(AttendanceRecord record);
    int addStudent(Student student);
}