package com.example.attendancesystem.Service.impl;

import com.example.attendancesystem.entity.Course;
import com.example.attendancesystem.entity.CourseStudent;
import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.repository.CourseRepository;
import com.example.attendancesystem.repository.CourseStudentRepository;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.repository.UserRepository;
import com.example.attendancesystem.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private CourseRepository courseRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Student addStudent(Student student, Integer courseId) {
        if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        if (studentRepository.findByStudentId(student.getStudentId()).isPresent()) {
            throw new IllegalArgumentException("学号 " + student.getStudentId() + " 已存在");
        }
        Student saved = studentRepository.save(student);
        ensureUserAccount(saved);

        // 如果指定了课程，创建 CourseStudent 关联
        if (courseId != null) {
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course != null) {
                // 创建 CourseStudent 选课记录
                if (courseStudentRepository.findByCourseIdAndStudentId(courseId, saved.getId()).isEmpty()) {
                    CourseStudent cs = new CourseStudent();
                    cs.setCourse(course);
                    cs.setStudent(saved);
                    courseStudentRepository.save(cs);
                }
                // 创建今日考勤记录（与 Excel 导入行为一致）
                LocalDate today = LocalDate.now();
                try {
                    Boolean exists = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) > 0 FROM attendances WHERE student_id = ? AND course_id = ? AND attendance_date = ?",
                        Boolean.class, saved.getId(), course.getId(), today);
                    if (exists == null || !exists) {
                        jdbcTemplate.update(
                            "INSERT INTO attendances (student_id, course_id, attendance_date, status, created_at) VALUES (?, ?, ?, 'present', ?)",
                            saved.getId(), course.getId(), today, LocalDateTime.now());
                    }
                } catch (Exception ignored) {
                    // 考勤记录创建失败不影响学生保存
                }
            }
        }
        return saved;
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
        existing.setCollege(student.getCollege());
        existing.setMajor(student.getMajor());
        existing.setGrade(student.getGrade());
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
        // UNION 查询：同时覆盖 CourseStudent 和 Attendance 两个数据源
        Page<Student> result;
        if (keyword == null || keyword.trim().isEmpty()) {
            result = studentRepository.findStudentsByTeacherIdUnion(teacherId, pageable);
        } else {
            result = studentRepository.searchStudentsByTeacherIdUnion(teacherId, keyword.trim(), pageable);
        }
        // 回退：如果 UNION 查询无结果，直接查全表（覆盖孤儿数据和首次使用场景）
        if (result.getTotalElements() == 0) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return studentRepository.findAll(pageable);
            }
            return studentRepository.searchByKeyword(keyword.trim(), pageable);
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
        // 回退：如果指定课程下无结果，先尝试 UNION 查询，再回退到全表查询
        if (result.getTotalElements() == 0) {
            if (keyword == null || keyword.trim().isEmpty()) {
                result = studentRepository.findStudentsByTeacherIdUnion(teacherId, pageable);
            } else {
                result = studentRepository.searchStudentsByTeacherIdUnion(teacherId, keyword.trim(), pageable);
            }
        }
        // 最终回退：全表查询（覆盖孤儿数据）
        if (result.getTotalElements() == 0) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return studentRepository.findAll(pageable);
            }
            return studentRepository.searchByKeyword(keyword.trim(), pageable);
        }
        return result;
    }

    @Override
    public List<Student> quickSearchByTeacher(Integer teacherId, String keyword) {
        // UNION 查询：同时覆盖 CourseStudent 和 Attendance 两个数据源
        List<Student> result;
        if (keyword == null || keyword.trim().isEmpty()) {
            result = studentRepository.findStudentsByTeacherIdUnion(teacherId,
                    PageRequest.of(0, 10, Sort.by("name"))).getContent();
        } else {
            result = studentRepository.searchStudentsByTeacherIdUnion(teacherId, keyword.trim(),
                    PageRequest.of(0, 10, Sort.by("name"))).getContent();
        }
        // 回退：如果 UNION 查询无结果，直接查全表
        if (result.isEmpty()) {
            if (keyword == null || keyword.trim().isEmpty()) {
                return studentRepository.findAll(PageRequest.of(0, 10, Sort.by("name"))).getContent();
            }
            return studentRepository.searchByKeyword(keyword.trim(),
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
        // UNION 查询：同时覆盖 CourseStudent 和 Attendance 两个数据源
        List<Student> result = studentRepository.findAllStudentsByTeacherIdUnion(teacherId);
        // 回退：如果 UNION 查询无结果，直接查全表（覆盖孤儿数据）
        if (result.isEmpty()) {
            return studentRepository.findAll(Sort.by("id"));
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
        student.setCollege(profileData.getCollege());
        student.setMajor(profileData.getMajor());
        student.setGrade(profileData.getGrade());
        student.setGender(profileData.getGender());
        student.setBirthDate(profileData.getBirthDate());
        student.setPhone(profileData.getPhone());
        Student saved = studentRepository.save(student);
        ensureUserAccount(saved);
        return saved;
    }

    /**
     * 确保学生有对应的 User 登录账号（用户名=学号，初始密码=学号）
     */
    @Override
    public void ensureUserAccount(Student student) {
        String username = student.getStudentId();
        if (username == null || username.isEmpty()) return;

        java.util.Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            // User 已存在 → 确保 Student 与 User 关联
            if (student.getUserId() == null) {
                student.setUserId(existingUser.get().getId());
                studentRepository.save(student);
            }
            return;
        }
        // 创建新的 User 账号（初始密码 = 学号）
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(username));
        user.setName(student.getName());
        user.setRole("student");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        // 关联 userId 到 Student
        if (student.getUserId() == null) {
            student.setUserId(user.getId());
            studentRepository.save(student);
        }
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
        if (profileData.getCollege() != null) {
            existing.setCollege(profileData.getCollege().trim());
        }
        if (profileData.getMajor() != null) {
            existing.setMajor(profileData.getMajor().trim());
        }
        if (profileData.getGrade() != null) {
            existing.setGrade(profileData.getGrade().trim());
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
            // 按课程筛选：优先从 CourseStudent 查询
            if (hasKeyword) {
                result = courseStudentRepository.searchStudentsByTeacherIdAndCourseId(
                        teacherId, courseId, keyword.trim(),
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by("studentId"))).getContent();
            } else {
                result = courseStudentRepository.findAllStudentsByTeacherIdAndCourseId(teacherId, courseId);
            }
            // 回退1：课程筛选无结果，尝试 UNION 查询
            if (result.isEmpty()) {
                if (hasKeyword) {
                    result = studentRepository.searchStudentsByTeacherIdUnion(teacherId, keyword.trim(),
                            PageRequest.of(0, Integer.MAX_VALUE, Sort.by("studentId"))).getContent();
                } else {
                    result = studentRepository.findAllStudentsByTeacherIdUnion(teacherId);
                }
            }
        } else {
            // 教师全量：使用 UNION 查询
            if (hasKeyword) {
                result = studentRepository.searchStudentsByTeacherIdUnion(teacherId, keyword.trim(),
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by("studentId"))).getContent();
            } else {
                result = studentRepository.findAllStudentsByTeacherIdUnion(teacherId);
            }
        }

        // 最终回退：全表查询（覆盖孤儿数据）
        if (result.isEmpty()) {
            if (hasKeyword) {
                return studentRepository.searchByKeyword(keyword.trim(),
                        PageRequest.of(0, Integer.MAX_VALUE, Sort.by("studentId"))).getContent();
            }
            return studentRepository.findAll(Sort.by("studentId"));
        }
        return result;
    }
}
