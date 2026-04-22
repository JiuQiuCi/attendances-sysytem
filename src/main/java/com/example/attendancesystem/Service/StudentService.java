package com.example.attendancesystem.Service;

import com.example.attendancesystem.entity.Student;
import java.util.List;
import java.util.Optional;

public interface StudentService {
    Student addStudent(Student student);
    Optional<Student> getStudentById(Integer id);
    Optional<Student> getStudentByStudentId(String studentId);
    List<Student> getStudentsByClass(String className);
    List<Student> getAllStudents();
    void deleteStudent(Integer id);
}