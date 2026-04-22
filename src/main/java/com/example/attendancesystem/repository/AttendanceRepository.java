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

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer>, JpaSpecificationExecutor<Attendance> {

    // ========== 原有方法（保留） ==========
    List<Attendance> findByStudentId(Integer studentId);
    List<Attendance> findByCourseId(Integer courseId);
    List<Attendance> findByStudentIdAndAttendanceDateBetween(Integer studentId, LocalDate start, LocalDate end);

    @Query("SELECT a FROM Attendance a WHERE a.course.id = :courseId AND a.attendanceDate = :date")
    List<Attendance> findAttendancesByCourseAndDate(@Param("courseId") Integer courseId, @Param("date") LocalDate date);

    // ========== 新增分页方法 ==========
    Page<Attendance> findAll(Pageable pageable);
    Page<Attendance> findByStudentId(Integer studentId, Pageable pageable);
    Page<Attendance> findByCourseId(Integer courseId, Pageable pageable);
}