package com.example.attendancesystem.Service.impl;

import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.repository.CourseStudentRepository;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseStudentRepository courseStudentRepository;

    @Override
    public Student addStudent(Student student) {
        if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        return studentRepository.save(student);
    }

    @Override
    public Student updateStudent(Student student) {
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
    public Optional<Student> getStudentByUserId(Integer userId) {
        return studentRepository.findByUserId(userId);
    }

    @Override
    public List<Student> getStudentsByClass(String className) {
        if (className == null || className.isEmpty()) {
            return studentRepository.findAll();
        }
        return studentRepository.findByClassName(className);
    }

    @Override
    public Page<Student> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    @Override
    public Page<Student> searchStudents(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return studentRepository.findAll(pageable);
        }
        return studentRepository.searchByKeyword(keyword.trim(), pageable);
    }

    @Override
    public void deleteStudent(Integer id) {
        // 先删除选课关联
        courseStudentRepository.deleteByStudentId(id);
        studentRepository.deleteById(id);
    }

    @Override
    public void deleteStudents(List<Integer> ids) {
        for (Integer id : ids) {
            courseStudentRepository.deleteByStudentId(id);
        }
        studentRepository.deleteAllById(ids);
    }

    @Override
    public List<Student> quickSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return studentRepository.findAll(PageRequest.of(0, 10, Sort.by("name"))).getContent();
        }
        return studentRepository.searchByKeyword(keyword.trim(), PageRequest.of(0, 10, Sort.by("name"))).getContent();
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll(Sort.by("id"));
    }

    // ===== 数据隔离方法 =====

    @Override
    public Page<Student> searchStudentsByTeacher(Integer teacherId, String keyword, Pageable pageable) {
        Page<Student> result;
        if (keyword == null || keyword.trim().isEmpty()) {
            result = courseStudentRepository.findStudentsByTeacherId(teacherId, pageable);
        } else {
            result = courseStudentRepository.searchStudentsByTeacherId(teacherId, keyword.trim(), pageable);
        }
        // 回退：如果 CourseStudent 表为空，使用 Attendance 表查询（兼容旧数据）
        if (result.getTotalElements() == 0) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return studentRepository.findStudentsByTeacherId(teacherId, pageable);
            }
            return studentRepository.searchStudentsByTeacherId(teacherId, keyword.trim(), pageable);
        }
        return result;
    }

    @Override
    public Page<Student> searchStudentsByTeacherAndCourse(Integer teacherId, Integer courseId, String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return courseStudentRepository.findStudentsByTeacherIdAndCourseId(teacherId, courseId, pageable);
        }
        return courseStudentRepository.searchStudentsByTeacherIdAndCourseId(teacherId, courseId, keyword.trim(), pageable);
    }

    @Override
    public List<Student> quickSearchByTeacher(Integer teacherId, String keyword) {
        List<Student> result;
        if (keyword == null || keyword.trim().isEmpty()) {
            result = courseStudentRepository.findStudentsByTeacherId(teacherId,
                    PageRequest.of(0, 10, Sort.by("name"))).getContent();
        } else {
            result = courseStudentRepository.searchStudentsByTeacherId(teacherId, keyword.trim(),
                    PageRequest.of(0, 10, Sort.by("name"))).getContent();
        }
        // 回退：兼容旧数据
        if (result.isEmpty()) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return studentRepository.findStudentsByTeacherId(teacherId,
                        PageRequest.of(0, 10, Sort.by("name"))).getContent();
            }
            return studentRepository.searchStudentsByTeacherId(teacherId, keyword.trim(),
                    PageRequest.of(0, 10, Sort.by("name"))).getContent();
        }
        return result;
    }

    @Override
    public List<Student> quickSearchByTeacherAndCourse(Integer teacherId, Integer courseId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return courseStudentRepository.findStudentsByTeacherIdAndCourseId(teacherId, courseId,
                    PageRequest.of(0, 10, Sort.by("name"))).getContent();
        }
        return courseStudentRepository.searchStudentsByTeacherIdAndCourseId(teacherId, courseId, keyword.trim(),
                PageRequest.of(0, 10, Sort.by("name"))).getContent();
    }

    @Override
    public List<Student> getAllStudentsByTeacher(Integer teacherId) {
        List<Student> result = courseStudentRepository.findAllStudentsByTeacherId(teacherId);
        // 回退：兼容旧数据
        if (result.isEmpty()) {
            return studentRepository.findAllStudentsByTeacherId(teacherId);
        }
        return result;
    }

    @Override
    public List<Student> getAllStudentsByTeacherAndCourse(Integer teacherId, Integer courseId) {
        return courseStudentRepository.findAllStudentsByTeacherIdAndCourseId(teacherId, courseId);
    }
}
