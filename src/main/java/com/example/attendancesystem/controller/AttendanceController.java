package com.example.attendancesystem.controller;

import com.example.attendancesystem.common.Result;
import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.entity.Course;
import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.repository.CourseRepository;
import com.example.attendancesystem.repository.CourseStudentRepository;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.Service.AttendanceService;
import com.example.attendancesystem.Service.CheckoutSessionManager;
import com.example.attendancesystem.Service.CourseService;
import com.example.attendancesystem.Service.StatisticsService;
import com.example.attendancesystem.dto.AttendanceStatsVO;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private StatisticsService statisticsService;

    @Autowired
    private CheckoutSessionManager checkoutSessionManager;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseStudentRepository courseStudentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

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

    /**
     * 扫码签到前置验证（无需登录）
     * 验证学生学号+姓名是否与课程名单匹配
     */
    @GetMapping("/verify-student")
    public Result<Map<String, Object>> verifyStudent(@RequestParam String studentId,
                                                      @RequestParam String name,
                                                      @RequestParam Integer courseId) {
        // 1. 通过学号查找学生
        Optional<Student> studentOpt = studentRepository.findByStudentId(studentId.trim());
        if (studentOpt.isEmpty()) {
            return Result.error(404, "学号 " + studentId + " 不存在于系统中，请核对学号后重试");
        }

        Student student = studentOpt.get();

        // 2. 验证姓名是否匹配
        if (!student.getName().trim().equals(name.trim())) {
            return Result.error(400, "姓名与学号不匹配，请核对后重试");
        }

        // 3. 检查学生是否在该课程的学生名单中
        boolean enrolled = courseStudentRepository.existsByCourseIdAndStudentId(courseId, student.getId());
        if (!enrolled) {
            return Result.error(403, "你不在该课程的学生名单中，请向老师说明情况后由老师手动添加");
        }

        // 验证通过，返回学生信息
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", student.getId());
        data.put("studentId", student.getStudentId());
        data.put("name", student.getName());
        data.put("className", student.getClassName());
        return Result.success("验证通过", data);
    }

    /**
     * 学生端：获取个人考勤全量统计
     */
    @GetMapping("/my-stats")
    public Result<AttendanceStatsVO> getMyStats(@RequestParam(required = false) Integer courseId) {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null || !SecurityUtil.isStudent()) {
            return Result.error(403, "仅学生可查看");
        }
        Optional<Student> studentOpt = SecurityUtil.getCurrentStudent(studentRepository);
        if (studentOpt.isEmpty()) {
            return Result.error(400, "请先完善个人信息");
        }
        AttendanceStatsVO stats = statisticsService.getStudentStats(studentOpt.get().getId(), courseId);
        return Result.success(stats);
    }

    /**
     * 学生端：获取今日课程及签到状态
     */
    @GetMapping("/my-today-courses")
    public Result<List<Map<String, Object>>> getMyTodayCourses() {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null || !SecurityUtil.isStudent()) {
            return Result.error(403, "仅学生可查看");
        }
        Optional<Student> studentOpt = SecurityUtil.getCurrentStudent(studentRepository);
        if (studentOpt.isEmpty()) {
            return Result.error(400, "请先完善个人信息");
        }
        Student student = studentOpt.get();
        LocalDate today = LocalDate.now();
        DayOfWeek todayDow = today.getDayOfWeek();

        // 获取学生的所有选课
        List<Integer> courseIds = courseStudentRepository.findCourseIdsByStudentId(student.getId());
        List<Map<String, Object>> result = new ArrayList<>();

        for (Integer cid : courseIds) {
            Optional<Course> courseOpt = courseRepository.findById(cid);
            if (courseOpt.isEmpty()) continue;
            Course course = courseOpt.get();

            // 检查是否匹配今天星期几
            if (course.getWeekDay() == null || !course.getWeekDay().equals(todayDow)) continue;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("courseId", course.getId());
            item.put("courseName", course.getName());
            item.put("code", course.getCode());
            item.put("teacherName", course.getTeacherName());
            item.put("classroom", course.getClassroom());
            item.put("startTime", course.getStartTime() != null ? course.getStartTime().toString() : null);
            item.put("endTime", course.getEndTime() != null ? course.getEndTime().toString() : null);

            // 查询今日考勤状态
            Optional<Attendance> att = attendanceRepository
                    .findByStudentIdAndCourseIdAndAttendanceDate(student.getId(), course.getId(), today);
            if (att.isPresent()) {
                item.put("checkedIn", true);
                item.put("status", att.get().getStatus());
                item.put("statusLabel", getStatusLabel(att.get().getStatus()));
            } else {
                item.put("checkedIn", false);
                item.put("status", null);
                item.put("statusLabel", "未签到");
            }

            // 判断是否在签到时间窗口内
            boolean inWindow = false;
            if (course.getStartTime() != null) {
                LocalTime now = LocalTime.now();
                LocalTime earliest = course.getStartTime().minusMinutes(15);
                LocalTime latest = course.getStartTime().plusMinutes(30);
                inWindow = !now.isBefore(earliest) && !now.isAfter(latest);
            }
            item.put("inCheckinWindow", inWindow);

            result.add(item);
        }

        return Result.success(result);
    }

    private String getStatusLabel(String status) {
        if (status == null) return "未签到";
        switch (status) {
            case "present": return "已签到";
            case "late": return "迟到";
            case "absent": return "缺勤";
            default: return status;
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

    // ===== 签退会话管理（教师端）=====

    /** 教师开启签退（发布签退任务，默认30分钟有效） */
    @PostMapping("/enable-checkout")
    public Result<Map<String, Object>> enableCheckout(
            @RequestParam Integer courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        User currentUser = SecurityUtil.getCurrentUser();
        if (!SecurityUtil.isTeacher()) {
            return Result.error(403, "仅教师可操作");
        }
        checkoutSessionManager.enable(courseId, date);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("courseId", courseId);
        data.put("date", date.toString());
        data.put("expiresInMinutes", 30);
        return Result.success("签退任务已发布（30分钟有效）", data);
    }

    /** 教师关闭签退 */
    @PostMapping("/disable-checkout")
    public Result<Void> disableCheckout(
            @RequestParam Integer courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (!SecurityUtil.isTeacher()) {
            return Result.error(403, "仅教师可操作");
        }
        checkoutSessionManager.disable(courseId, date);
        return Result.success(null);
    }

    /** 查询签退状态（教师和学生均可调用） */
    @GetMapping("/checkout-status")
    public Result<Map<String, Object>> getCheckoutStatus(
            @RequestParam Integer courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        boolean enabled = checkoutSessionManager.isEnabled(courseId, date);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", enabled);
        data.put("remainingMinutes", enabled ? checkoutSessionManager.remainingMinutes(courseId, date) : -1);
        return Result.success(data);
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

    // 按日期删除考勤记录（仅教师，删除指定日期下该教师所有课程的记录）
    @DeleteMapping("/by-date")
    public Result<Map<String, Object>> deleteAttendanceByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            if (!SecurityUtil.isTeacher()) {
                return Result.error(403, "仅教师可删除考勤记录");
            }
            int deleted = attendanceService.deleteAttendanceByDate(currentUser.getId(), date);
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("deletedCount", deleted);
            data.put("date", date.toString());
            return Result.success("已删除 " + deleted + " 条记录", data);
        } catch (Exception e) {
            return Result.error(500, "删除失败：" + e.getMessage());
        }
    }

    // 查询单条考勤记录
    @GetMapping("/{id}")
    public Result<Attendance> getAttendance(@PathVariable Integer id) {
        return attendanceService.getAttendanceById(id)
                .map(Result::success)
                .orElse(Result.error(404, "记录不存在"));
    }

    // 删除考勤记录（仅教师可删除自己课程的记录）
    @DeleteMapping("/{id}")
    public Result<Void> deleteAttendance(@PathVariable Integer id) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            if (!SecurityUtil.isTeacher()) {
                return Result.error(403, "仅教师可删除考勤记录");
            }
            Optional<Attendance> existing = attendanceService.getAttendanceById(id);
            if (existing.isEmpty()) {
                return Result.error(404, "记录不存在");
            }
            Attendance att = existing.get();
            // 教师只能删除自己课程的记录
            if (att.getCourse() == null || !att.getCourse().getTeacherId().equals(currentUser.getId())) {
                return Result.error(403, "无权删除其他教师课程的记录");
            }
            attendanceService.deleteAttendance(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(500, "删除失败：" + e.getMessage());
        }
    }

    // 批量删除（仅教师可删除自己课程的记录）
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Integer> ids) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            if (!SecurityUtil.isTeacher()) {
                return Result.error(403, "仅教师可批量删除考勤记录");
            }
            for (Integer id : ids) {
                Optional<Attendance> existing = attendanceService.getAttendanceById(id);
                if (existing.isEmpty()) {
                    return Result.error(404, "记录 " + id + " 不存在");
                }
                Attendance att = existing.get();
                if (att.getCourse() == null || !att.getCourse().getTeacherId().equals(currentUser.getId())) {
                    return Result.error(403, "无权删除其他教师课程的记录");
                }
            }
            attendanceService.deleteAttendances(ids);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(500, "批量删除失败：" + e.getMessage());
        }
    }
}
