package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer>, JpaSpecificationExecutor<Student> {

    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByUserId(Integer userId);
    List<Student> findByClassName(String className);

    // 模糊搜索（名称或学号）
    @Query("SELECT s FROM Student s WHERE s.name LIKE %:keyword% OR s.studentId LIKE %:keyword%")
    Page<Student> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 批量删除
    void deleteByIdIn(List<Integer> ids);

    // ===== 数据隔离查询 =====

    // 查询教师课程下的所有学生（通过考勤记录关联，使用 EXISTS 避免 DISTINCT + ORDER BY 冲突）
    @Query("SELECT s FROM Student s WHERE EXISTS " +
           "(SELECT a FROM Attendance a JOIN a.course c WHERE a.student = s AND c.teacherId = :teacherId)")
    Page<Student> findStudentsByTeacherId(@Param("teacherId") Integer teacherId, Pageable pageable);

    // 教师在课程范围内搜索学生
    @Query("SELECT s FROM Student s WHERE EXISTS " +
           "(SELECT a FROM Attendance a JOIN a.course c WHERE a.student = s AND c.teacherId = :teacherId) AND " +
           "(s.name LIKE %:keyword% OR s.studentId LIKE %:keyword%)")
    Page<Student> searchStudentsByTeacherId(@Param("teacherId") Integer teacherId,
                                             @Param("keyword") String keyword,
                                             Pageable pageable);

    // 教师课程下的所有学生（不分页，用于导出）
    @Query("SELECT s FROM Student s WHERE EXISTS " +
           "(SELECT a FROM Attendance a JOIN a.course c WHERE a.student = s AND c.teacherId = :teacherId) ORDER BY s.id")
    List<Student> findAllStudentsByTeacherId(@Param("teacherId") Integer teacherId);

    // ===== UNION 查询：同时覆盖 CourseStudent 和 Attendance 数据源 =====

    // 教师可见的所有学生（通过 CourseStudent 或 Attendance 关联）
    @Query("SELECT s FROM Student s WHERE " +
           "EXISTS (SELECT cs FROM CourseStudent cs JOIN cs.course c WHERE cs.student = s AND c.teacherId = :teacherId) " +
           "OR EXISTS (SELECT a FROM Attendance a JOIN a.course c WHERE a.student = s AND c.teacherId = :teacherId)")
    Page<Student> findStudentsByTeacherIdUnion(@Param("teacherId") Integer teacherId, Pageable pageable);

    // 教师可见的学生（带关键字搜索）
    @Query("SELECT s FROM Student s WHERE (" +
           "EXISTS (SELECT cs FROM CourseStudent cs JOIN cs.course c WHERE cs.student = s AND c.teacherId = :teacherId) " +
           "OR EXISTS (SELECT a FROM Attendance a JOIN a.course c WHERE a.student = s AND c.teacherId = :teacherId)) " +
           "AND (s.name LIKE %:keyword% OR s.studentId LIKE %:keyword%)")
    Page<Student> searchStudentsByTeacherIdUnion(@Param("teacherId") Integer teacherId,
                                                  @Param("keyword") String keyword,
                                                  Pageable pageable);

    // 教师可见的所有学生（不分页，导出用）
    @Query("SELECT s FROM Student s WHERE " +
           "EXISTS (SELECT cs FROM CourseStudent cs JOIN cs.course c WHERE cs.student = s AND c.teacherId = :teacherId) " +
           "OR EXISTS (SELECT a FROM Attendance a JOIN a.course c WHERE a.student = s AND c.teacherId = :teacherId) ORDER BY s.id")
    List<Student> findAllStudentsByTeacherIdUnion(@Param("teacherId") Integer teacherId);
}