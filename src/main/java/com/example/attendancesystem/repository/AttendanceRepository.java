package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer>, JpaSpecificationExecutor<Attendance> {

    List<Attendance> findByStudentId(Integer studentId);
    List<Attendance> findByCourseId(Integer courseId);
    List<Attendance> findByStudentIdAndAttendanceDateBetween(Integer studentId, LocalDate start, LocalDate end);

    @Query("SELECT a FROM Attendance a WHERE a.course.id = :courseId AND a.attendanceDate = :date")
    List<Attendance> findAttendancesByCourseAndDate(@Param("courseId") Integer courseId, @Param("date") LocalDate date);

    // 新增：根据学生、课程、日期查找唯一考勤记录
    Optional<Attendance> findByStudentIdAndCourseIdAndAttendanceDate(Integer studentId, Integer courseId, LocalDate date);

    // 统计某学生的考勤记录数（用于删除前检查）
    long countByStudentId(Integer studentId);

    // ===== 数据隔离查询 =====

    // 查询教师所有课程下的考勤记录（分页）
    @Query("SELECT a FROM Attendance a JOIN a.course c WHERE c.teacherId = :teacherId")
    Page<Attendance> findByTeacherId(@Param("teacherId") Integer teacherId, Pageable pageable);

    // 查询教师所有课程下的考勤记录（不分页）
    @Query("SELECT a FROM Attendance a JOIN a.course c WHERE c.teacherId = :teacherId")
    List<Attendance> findAllByTeacherId(@Param("teacherId") Integer teacherId);

    // 教师在课程范围内按日期过滤考勤
    @Query("SELECT a FROM Attendance a JOIN a.course c WHERE c.teacherId = :teacherId " +
           "AND (:startDate IS NULL OR a.attendanceDate >= :startDate) " +
           "AND (:endDate IS NULL OR a.attendanceDate <= :endDate)")
    Page<Attendance> findByTeacherIdAndDateRange(@Param("teacherId") Integer teacherId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate,
                                                  Pageable pageable);

    // 教师在指定课程中按日期过滤考勤
    @Query("SELECT a FROM Attendance a JOIN a.course c WHERE c.teacherId = :teacherId " +
           "AND a.course.id = :courseId " +
           "AND (:startDate IS NULL OR a.attendanceDate >= :startDate) " +
           "AND (:endDate IS NULL OR a.attendanceDate <= :endDate)")
    Page<Attendance> findByTeacherIdAndCourseIdAndDateRange(@Param("teacherId") Integer teacherId,
                                                             @Param("courseId") Integer courseId,
                                                             @Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate,
                                                             Pageable pageable);
}