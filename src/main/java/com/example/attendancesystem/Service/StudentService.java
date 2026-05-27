package com.example.attendancesystem.Service;

import com.example.attendancesystem.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface StudentService {
    Student addStudent(Student student);
    Student updateStudent(Student student);
    Optional<Student> getStudentById(Integer id);
    Optional<Student> getStudentByStudentId(String studentId);
    List<Student> getStudentsByClass(String className);
    Page<Student> getAllStudents(Pageable pageable);
    Page<Student> searchStudents(String keyword, Pageable pageable);
    void deleteStudent(Integer id);
    void deleteStudents(List<Integer> ids);
    List<Student> quickSearch(String keyword);
    List<Student> getAllStudents();
}