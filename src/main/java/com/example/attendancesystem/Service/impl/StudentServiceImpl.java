package com.example.attendancesystem.Service.impl;

import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public Student addStudent(Student student) {
        // 业务校验：学号不能为空
        if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        return studentRepository.save(student);
    }

    @Override
    public Optional<Student> getStudentById(Integer id) {
        return studentRepository.findById(id);
    }

    @Override
    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    @Override
    public List<Student> getStudentsByClass(String className) {
        if (className == null || className.isEmpty()) {
            return studentRepository.findAll();
        }
        return studentRepository.findByClassName(className);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public void deleteStudent(Integer id) {
        studentRepository.deleteById(id);
    }
}