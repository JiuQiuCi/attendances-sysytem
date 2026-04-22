package com.example.attendancesystem.controller;

import com.example.attendancesystem.common.Result;
import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.Service.AttendanceService;
import com.example.attendancesystem.specification.AttendanceSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // ========== 原有接口（保留，假设已有） ==========
    // 例如原有的 save, update, delete, getById 等，此处不重复写

    // ========== 新增分页查询（无排序） ==========
    @GetMapping("/page")
    public Result<Page<Attendance>> getAttendancesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Attendance> pageResult = attendanceService.getAttendancesPage(pageable);
        return Result.success(pageResult);
    }

    // 按学生分页
    @GetMapping("/student/{studentId}/page")
    public Result<Page<Attendance>> getAttendancesByStudentPage(
            @PathVariable Integer studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Attendance> pageResult = attendanceService.getAttendancesByStudentPage(studentId, pageable);
        return Result.success(pageResult);
    }

    // 按课程分页
    @GetMapping("/course/{courseId}/page")
    public Result<Page<Attendance>> getAttendancesByCoursePage(
            @PathVariable Integer courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Attendance> pageResult = attendanceService.getAttendancesByCoursePage(courseId, pageable);
        return Result.success(pageResult);
    }

    // 分页 + 排序（按单个字段）
    @GetMapping("/page/sorted")
    public Result<Page<Attendance>> getAttendancesPageSorted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "attendanceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(dir, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Attendance> pageResult = attendanceService.getAttendancesPage(pageable);
        return Result.success(pageResult);
    }

    // ========== 多条件动态查询（支持分页和排序） ==========
    @GetMapping("/search")
    public Result<Page<Attendance>> searchAttendances(
            // 分页参数
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            // 排序参数
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            // 动态条件参数（均为可选）
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String seatPosition) {

        // 构建动态条件
        Specification<Attendance> spec = Specification
                .where(AttendanceSpecification.studentIdLike(studentId))
                .and(AttendanceSpecification.attendanceDateAfter(startDate))
                .and(AttendanceSpecification.attendanceDateBefore(endDate))
                .and(AttendanceSpecification.statusEquals(status))
                .and(AttendanceSpecification.courseNameLike(courseName))
                .and(AttendanceSpecification.seatPositionEquals(seatPosition));

        // 构建分页和排序对象
        Pageable pageable;
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Sort sort = Sort.by(dir, sortBy);
            pageable = PageRequest.of(page, size, sort);
        } else {
            pageable = PageRequest.of(page, size);
        }

        Page<Attendance> result = attendanceService.searchAttendances(spec, pageable);
        return Result.success(result);
    }
}