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
    List<Attendance> findByStudentId(Integer studentId);
    List<Attendance> findByCourseId(Integer courseId);
    List<Attendance> findByStudentIdAndAttendanceDateBetween(Integer studentId, LocalDate start, LocalDate end);

    @Query("SELECT a FROM Attendance a WHERE a.course.id = :courseId AND a.attendanceDate = :date")
    List<Attendance> findAttendancesByCourseAndDate(@Param("courseId") Integer courseId, @Param("date") LocalDate date);
}