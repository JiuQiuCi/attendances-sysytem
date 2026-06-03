package com.example.attendancesystem.controller;

import com.example.attendancesystem.common.Result;
import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.entity.Course;
import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.CourseRepository;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.Service.AttendanceService;
import com.example.attendancesystem.Service.CourseService;
import com.example.attendancesystem.util.SecurityUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentRepository studentRepository;

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

    // 手动标记考勤状态
    @PostMapping("/mark")
    public Result<Attendance> markAttendance(@RequestParam Integer studentId,
                                             @RequestParam Integer courseId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                             @RequestParam String status) {
        try {
            Attendance attendance = attendanceService.markAttendance(studentId, courseId, date, status);
            return Result.success(attendance);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "标记失败：" + e.getMessage());
        }
    }

    // 分页+筛选考勤记录（数据隔离）
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

        User currentUser = SecurityUtil.getCurrentUser();
        Page<Attendance> pageResult;

        if (SecurityUtil.isTeacher()) {
            // 教师：只看自己课程的考勤
            pageResult = attendanceService.searchAttendancesByTeacher(
                    currentUser.getId(), courseId, startDate, endDate, pageable);
        } else {
            // 学生：只看自己的考勤
            Optional<Student> studentOpt = SecurityUtil.getCurrentStudent(studentRepository);
            if (studentOpt.isPresent()) {
                pageResult = attendanceService.getAttendancesByStudentPaged(
                        studentOpt.get().getId(), pageable);
            } else {
                // 学生未关联 Student 记录，返回空
                pageResult = Page.empty(pageable);
            }
        }
        return Result.success(pageResult);
    }

    // 获取课程列表（数据隔离：教师只看自己的课程，学生只看自己参与的课程）
    @GetMapping("/courses")
    public Result<List<Course>> getAllCourses() {
        User currentUser = SecurityUtil.getCurrentUser();
        List<Course> courses;
        if (SecurityUtil.isTeacher()) {
            courses = courseRepository.findByTeacherId(currentUser.getId());
        } else {
            // 学生：看自己考勤记录中的课程
            Optional<Student> studentOpt = SecurityUtil.getCurrentStudent(studentRepository);
            if (studentOpt.isPresent()) {
                courses = courseService.getActiveCoursesForStudent(studentOpt.get().getId());
                if (courses.isEmpty()) {
                    courses = courseRepository.findAll(); // 无考勤记录时显示全部课程
                }
            } else {
                courses = courseRepository.findAll();
            }
        }
        return Result.success(courses);
    }

    // 导出 Excel（数据隔离）
    @GetMapping("/export")
    public void exportAttendances(
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        User currentUser = SecurityUtil.getCurrentUser();
        List<Attendance> list;
        if (SecurityUtil.isTeacher()) {
            list = attendanceService.exportAttendancesByTeacher(
                    currentUser.getId(), courseId, startDate, endDate);
        } else {
            Optional<Student> studentOpt = SecurityUtil.getCurrentStudent(studentRepository);
            if (studentOpt.isPresent()) {
                list = attendanceService.getAttendancesByStudent(studentOpt.get().getId());
            } else {
                list = List.of();
            }
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("考勤记录");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(workbook.createDataFormat().getFormat("yyyy-MM-dd"));

        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "学生姓名", "学号", "课程名称", "考勤日期", "状态", "座位位置", "签到时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < list.size(); i++) {
            Attendance a = list.get(i);
            Row row = sheet.createRow(i + 1);

            row.createCell(0).setCellValue(a.getId());
            row.createCell(1).setCellValue(a.getStudent() != null ? a.getStudent().getName() : "");
            row.createCell(2).setCellValue(a.getStudent() != null ? a.getStudent().getStudentId() : "");
            row.createCell(3).setCellValue(a.getCourse() != null ? a.getCourse().getName() : "");

            Cell dateCell = row.createCell(4);
            dateCell.setCellValue(a.getAttendanceDate());
            dateCell.setCellStyle(dateStyle);

            String statusLabel = a.getStatus();
            switch (a.getStatus()) {
                case "present": statusLabel = "正常"; break;
                case "absent": statusLabel = "缺勤"; break;
                case "late": statusLabel = "迟到"; break;
            }
            row.createCell(5).setCellValue(statusLabel);
            row.createCell(6).setCellValue(a.getSeatPosition() != null ? a.getSeatPosition() : "");
            row.createCell(7).setCellValue(a.getCreatedAt() != null ? a.getCreatedAt().toString() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=attendances.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // 查询单条考勤记录
    @GetMapping("/{id}")
    public Result<Attendance> getAttendance(@PathVariable Integer id) {
        return attendanceService.getAttendanceById(id)
                .map(Result::success)
                .orElse(Result.error(404, "记录不存在"));
    }

    // 删除考勤记录
    @DeleteMapping("/{id}")
    public Result<Void> deleteAttendance(@PathVariable Integer id) {
        try {
            attendanceService.deleteAttendance(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(500, "删除失败：" + e.getMessage());
        }
    }

    // 批量删除
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Integer> ids) {
        try {
            attendanceService.deleteAttendances(ids);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(500, "批量删除失败：" + e.getMessage());
        }
    }
}
