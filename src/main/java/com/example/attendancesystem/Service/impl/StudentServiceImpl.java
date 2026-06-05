package com.example.attendancesystem.Service.impl;

import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.repository.AttendanceRepository;
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

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Override
    public Student addStudent(Student student) {
        if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        if (studentRepository.findByStudentId(student.getStudentId()).isPresent()) {
            throw new IllegalArgumentException("学号 " + student.getStudentId() + " 已存在");
        }
        return studentRepository.save(student);
    }

    @Override
    public Student updateStudent(Student student) {
        Student existing = studentRepository.findById(student.getId())
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        // 检查学号是否被其他学生占用
        if (!existing.getStudentId().equals(student.getStudentId())
                && studentRepository.findByStudentId(student.getStudentId()).isPresent()) {
            throw new IllegalArgumentException("学号 " + student.getStudentId() + " 已被占用");
        }
        existing.setStudentId(student.getStudentId());
        existing.setName(student.getName());
        existing.setClassName(student.getClassName());
        existing.setGender(student.getGender());
        existing.setBirthDate(student.getBirthDate());
        existing.setPhone(student.getPhone());
        return studentRepository.save(existing);
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
        // 检查是否有考勤记录
        long count = attendanceRepository.countByStudentId(id);
        if (count > 0) {
            throw new IllegalStateException("该学生有 " + count + " 条考勤记录，请先删除考勤记录后再删除学生");
        }
        // 删除选课关联
        courseStudentRepository.deleteByStudentId(id);
        studentRepository.deleteById(id);
    }

    @Override
    public void deleteStudents(List<Integer> ids) {
        // 检查是否有考勤记录
        for (Integer id : ids) {
            long count = attendanceRepository.countByStudentId(id);
            if (count > 0) {
                throw new IllegalStateException("学生(ID=" + id + ")有 " + count + " 条考勤记录，请先删除考勤记录后再操作");
            }
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
        Page<Student> result;
        if (keyword == null || keyword.trim().isEmpty()) {
            result = courseStudentRepository.findStudentsByTeacherIdAndCourseId(teacherId, courseId, pageable);
        } else {
            result = courseStudentRepository.searchStudentsByTeacherIdAndCourseId(teacherId, courseId, keyword.trim(), pageable);
        }
        // 回退：如果 CourseStudent 表为空，通过 Attendance 表查询（兼容旧数据）
        if (result.getTotalElements() == 0) {
            // Attendance 回退只支持 teacherId 范围，courseId 在外部无直接查询，此处退回 teacher 级
            if (keyword == null || keyword.trim().isEmpty()) {
                return studentRepository.findStudentsByTeacherId(teacherId, pageable);
            }
            return studentRepository.searchStudentsByTeacherId(teacherId, keyword.trim(), pageable);
        }
        return result;
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

    @Override
    public List<Integer> getStudentCourseIds(Integer studentId) {
        return courseStudentRepository.findCourseIdsByStudentId(studentId);
    }

    // ===== 学生自助完善信息 =====

    @Override
    public Student createMyProfile(Integer userId, Student profileData) {
        // 检查是否已有学生记录
        if (studentRepository.findByUserId(userId).isPresent()) {
            throw new IllegalArgumentException("您已完善过个人信息，请使用编辑功能");
        }
        if (profileData.getStudentId() == null || profileData.getStudentId().trim().isEmpty()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        if (profileData.getName() == null || profileData.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (studentRepository.findByStudentId(profileData.getStudentId().trim()).isPresent()) {
            throw new IllegalArgumentException("学号 " + profileData.getStudentId() + " 已被占用，请检查是否输入正确");
        }
        Student student = new Student();
        student.setUserId(userId);
        student.setStudentId(profileData.getStudentId().trim());
        student.setName(profileData.getName().trim());
        student.setClassName(profileData.getClassName());
        student.setGender(profileData.getGender());
        student.setBirthDate(profileData.getBirthDate());
        student.setPhone(profileData.getPhone());
        return studentRepository.save(student);
    }

    @Override
    public Student updateMyProfile(Integer userId, Student profileData) {
        Student existing = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("未找到您的学生信息，请先完善个人信息"));
        // 如果修改了学号，检查是否被占用
        if (profileData.getStudentId() != null && !profileData.getStudentId().isEmpty()
                && !existing.getStudentId().equals(profileData.getStudentId())) {
            if (studentRepository.findByStudentId(profileData.getStudentId().trim()).isPresent()) {
                throw new IllegalArgumentException("学号 " + profileData.getStudentId() + " 已被占用");
            }
            existing.setStudentId(profileData.getStudentId().trim());
        }
        if (profileData.getName() != null && !profileData.getName().trim().isEmpty()) {
            existing.setName(profileData.getName().trim());
        }
        if (profileData.getClassName() != null) {
            existing.setClassName(profileData.getClassName().trim());
        }
        if (profileData.getGender() != null) {
            existing.setGender(profileData.getGender());
        }
        if (profileData.getBirthDate() != null) {
            existing.setBirthDate(profileData.getBirthDate());
        }
        if (profileData.getPhone() != null) {
            existing.setPhone(profileData.getPhone().trim());
        }
        return studentRepository.save(existing);
    }

    @Override
    public List<Student> exportStudentsByTeacher(Integer teacherId, Integer courseId, String keyword) {
        List<Student> result;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (courseId != null) {
            // 按课程筛选
            if (hasKeyword) {
                result = courseStudentRepository.searchStudentsByTeacherIdAndCourseId(
                        teacherId, courseId, keyword.trim(),
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by("studentId"))).getContent();
            } else {
                result = courseStudentRepository.findAllStudentsByTeacherIdAndCourseId(teacherId, courseId);
            }
        } else {
            // 教师全量（支持关键字过滤）
            if (hasKeyword) {
                result = courseStudentRepository.searchStudentsByTeacherId(teacherId, keyword.trim(),
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by("studentId"))).getContent();
            } else {
                result = courseStudentRepository.findAllStudentsByTeacherId(teacherId);
            }
        }

        // 回退：兼容仅通过 Attendance 关联的旧数据
        if (result.isEmpty()) {
            if (hasKeyword) {
                return studentRepository.searchStudentsByTeacherId(teacherId, keyword.trim(),
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by("studentId"))).getContent();
            }
            return studentRepository.findAllStudentsByTeacherId(teacherId);
        }
        return result;
    }
}
