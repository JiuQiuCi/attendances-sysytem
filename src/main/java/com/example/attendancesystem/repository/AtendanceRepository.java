package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    // 根据学生ID查询考勤记录
    List<Attendance> findByStudentId(Integer studentId);

    // 根据课程ID查询考勤记录
    List<Attendance> findByCourseId(Integer courseId);

    // 根据学生ID和日期范围查询
    List<Attendance> findByStudentIdAndAttendanceDateBetween(Integer studentId, LocalDate startDate, LocalDate endDate);

    // 自定义JPQL查询：查询某课程某日期的所有考勤
    @Query("SELECT a FROM Attendance a WHERE a.course.id = :courseId AND a.attendanceDate = :date")
    List<Attendance> findAttendancesByCourseAndDate(@Param("courseId") Integer courseId, @Param("date") LocalDate date);
}