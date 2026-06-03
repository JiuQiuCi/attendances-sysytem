package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

    Optional<Course> findByName(String name);

    Optional<Course> findByCode(String code);

    List<Course> findByTeacherId(Integer teacherId);

    List<Course> findBySemester(Integer semester);

    List<Course> findByWeekDay(DayOfWeek weekDay);

    @Query("SELECT c FROM Course c WHERE " +
           "(:keyword IS NULL OR c.name LIKE %:keyword% OR c.code LIKE %:keyword% OR c.teacherName LIKE %:keyword%) AND " +
           "(:semester IS NULL OR c.semester = :semester)")
    Page<Course> searchCourses(@Param("keyword") String keyword,
                               @Param("semester") Integer semester,
                               Pageable pageable);

    @Query("SELECT DISTINCT c.semester FROM Course c ORDER BY c.semester DESC")
    List<Integer> findAllSemesters();

    // 按课程代码+教师+星期+开始时间查找（同一时间段唯一性检查）
    List<Course> findByCodeAndTeacherIdAndWeekDayAndStartTime(String code, Integer teacherId,
                                                               java.time.DayOfWeek weekDay,
                                                               java.time.LocalTime startTime);
}
