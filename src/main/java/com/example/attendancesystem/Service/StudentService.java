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
    Optional<Student> getStudentByUserId(Integer userId);
    /** @deprecated 无数据隔离，请使用 searchStudentsByTeacher */
    @Deprecated List<Student> getStudentsByClass(String className);
    /** @deprecated 无数据隔离，请使用 searchStudentsByTeacher */
    @Deprecated Page<Student> getAllStudents(Pageable pageable);
    /** @deprecated 无数据隔离，请使用 searchStudentsByTeacher */
    @Deprecated Page<Student> searchStudents(String keyword, Pageable pageable);
    void deleteStudent(Integer id);
    void deleteStudents(List<Integer> ids);
    /** @deprecated 无数据隔离，请使用 quickSearchByTeacher */
    @Deprecated List<Student> quickSearch(String keyword);
    /** @deprecated 无数据隔离，请使用 getAllStudentsByTeacher */
    @Deprecated List<Student> getAllStudents();

    // ===== 数据隔离方法 =====
    Page<Student> searchStudentsByTeacher(Integer teacherId, String keyword, Pageable pageable);
    Page<Student> searchStudentsByTeacherAndCourse(Integer teacherId, Integer courseId, String keyword, Pageable pageable);
    List<Student> quickSearchByTeacher(Integer teacherId, String keyword);
    List<Student> quickSearchByTeacherAndCourse(Integer teacherId, Integer courseId, String keyword);
    List<Student> getAllStudentsByTeacher(Integer teacherId);
    List<Student> getAllStudentsByTeacherAndCourse(Integer teacherId, Integer courseId);
    /** 导出筛选（教师隔离 + 课程/关键字过滤） */
    List<Student> exportStudentsByTeacher(Integer teacherId, Integer courseId, String keyword);

    /** 获取学生的课程ID列表 */
    List<Integer> getStudentCourseIds(Integer studentId);

    // ===== 学生自助完善信息 =====
    Student createMyProfile(Integer userId, Student profileData);
    Student updateMyProfile(Integer userId, Student profileData);

    /** 确保学生有对应的 User 登录账号（用户名=学号，初始密码=学号），已存在则跳过 */
    void ensureUserAccount(Student student);
}
