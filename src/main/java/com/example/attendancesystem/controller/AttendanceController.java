package com.example.attendancesystem.controller;

import com.example.attendancesystem.common.Result;
import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.entity.Course;
import com.example.attendancesystem.repository.CourseRepository;
import com.example.attendancesystem.Service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private CourseRepository courseRepository;

    // 签到
    @PostMapping("/checkin")
    public Result<Attendance> checkIn(@RequestParam Integer studentId,
                                      @RequestParam Integer courseId,
                                      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Attendance attendance = attendanceService.checkIn(studentId, courseId, date);
            return Result.success(attendance);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "签到失败：" + e.getMessage());
        }
    }

    // 签退
    @PostMapping("/checkout")
    public Result<Attendance> checkOut(@RequestParam Integer studentId,
                                       @RequestParam Integer courseId,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Attendance attendance = attendanceService.checkOut(studentId, courseId, date);
            return Result.success(attendance);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "签退失败：" + e.getMessage());
        }
    }

    // 分页+筛选考勤记录
    @GetMapping("/list")
    public Result<Page<Attendance>> listAttendances(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "attendanceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        Page<Attendance> pageResult = attendanceService.searchAttendances(courseId, startDate, endDate, pageable);
        return Result.success(pageResult);
    }

    // 获取所有课程（用于下拉筛选）
    @GetMapping("/courses")
    public Result<List<Course>> getAllCourses() {
        return Result.success(courseRepository.findAll());
    }

    // 导出 CSV
    @GetMapping("/export")
    public void exportAttendances(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        List<Attendance> list = attendanceService.exportAttendances(courseId, startDate, endDate);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=attendances.csv");
        PrintWriter writer = response.getWriter();
        writer.println("\uFEFFID,学生姓名,学号,课程名称,考勤日期,状态,座位位置,签到时间");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Attendance a : list) {
            String studentName = a.getStudent() != null ? a.getStudent().getName() : "";
            String studentId = a.getStudent() != null ? a.getStudent().getStudentId() : "";
            String courseName = a.getCourse() != null ? a.getCourse().getName() : "";
            String date = a.getAttendanceDate().format(dateFormatter);
            String status = a.getStatus();
            String seat = a.getSeatPosition() == null ? "" : a.getSeatPosition();
            String createdAt = a.getCreatedAt() == null ? "" : a.getCreatedAt().toString();
            writer.printf("%d,%s,%s,%s,%s,%s,%s,%s%n",
                    a.getId(), studentName, studentId, courseName, date, status, seat, createdAt);
        }
        writer.flush();
    }
}