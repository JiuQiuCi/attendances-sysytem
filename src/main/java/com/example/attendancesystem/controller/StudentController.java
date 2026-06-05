package com.example.attendancesystem.controller;

import com.example.attendancesystem.common.Result;
import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.Service.StudentService;
import com.example.attendancesystem.util.SecurityUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // 新增学生
    @PostMapping("/add")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Student> addStudent(@RequestBody Student student) {
        try {
            Student saved = studentService.addStudent(student);
            return Result.success(saved);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "系统错误：" + e.getMessage());
        }
    }

    // 更新学生
    @PutMapping("/update")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Student> updateStudent(@RequestBody Student student) {
        try {
            Student updated = studentService.updateStudent(student);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(500, "更新失败：" + e.getMessage());
        }
    }

    // 分页 + 搜索 + 排序（数据隔离：教师只看自己课程的学生）
    @GetMapping("/list")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Page<Student>> listStudents(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) Integer courseId) {
        try {
            Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));

            // 数据隔离：教师只显示自己课程的学生
            User currentUser = SecurityUtil.getCurrentUser();
            if (currentUser == null) {
                return Result.error(401, "请先登录");
            }
            Integer teacherId = currentUser.getId();
            Page<Student> studentPage;
            if (courseId != null) {
                studentPage = studentService.searchStudentsByTeacherAndCourse(teacherId, courseId, keyword, pageable);
            } else {
                studentPage = studentService.searchStudentsByTeacher(teacherId, keyword, pageable);
            }
            return Result.success(studentPage);
        } catch (Exception e) {
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }

    // 根据ID查询单个学生
    @GetMapping("/{id}")
    public Result<Student> getStudent(@PathVariable Integer id) {
        return studentService.getStudentById(id)
                .map(Result::success)
                .orElse(Result.error(404, "学生不存在"));
    }

    // 删除单个学生
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<String> deleteStudent(@PathVariable Integer id) {
        try {
            studentService.deleteStudent(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(500, "删除失败：" + e.getMessage());
        }
    }

    // 快速搜索（教师搜索自己课程的学生，学生只能搜自己的记录）
    @GetMapping("/quick-search")
    public Result<List<Student>> quickSearch(@RequestParam String q,
                                              @RequestParam(required = false) Integer courseId) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            if (currentUser == null) {
                return Result.error(401, "请先登录");
            }
            List<Student> students;
            if (SecurityUtil.isTeacher()) {
                Integer teacherId = currentUser.getId();
                if (courseId != null) {
                    students = studentService.quickSearchByTeacherAndCourse(teacherId, courseId, q);
                } else {
                    students = studentService.quickSearchByTeacher(teacherId, q);
                }
            } else {
                // 学生：只能搜索自己的记录
                students = studentService.getStudentByUserId(currentUser.getId())
                        .map(s -> {
                            // 只返回匹配关键字的自己的记录
                            if (q == null || q.trim().isEmpty()
                                    || s.getName().contains(q.trim())
                                    || s.getStudentId().contains(q.trim())) {
                                return List.of(s);
                            }
                            return List.<Student>of();
                        })
                        .orElse(List.of());
            }
            return Result.success(students);
        } catch (Exception e) {
            return Result.error(500, "搜索失败：" + e.getMessage());
        }
    }

    // 导出 Excel（数据隔离：教师只导出自己课程的学生，支持按课程和关键字筛选）
    @GetMapping("/export")
    @PreAuthorize("hasRole('TEACHER')")
    public void exportStudents(@RequestParam(required = false) Integer courseId,
                               @RequestParam(required = false) String keyword,
                               HttpServletResponse response) throws IOException {
        Integer teacherId = SecurityUtil.getCurrentUser().getId();
        List<Student> list = studentService.exportStudentsByTeacher(teacherId, courseId, keyword);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("学生名单");

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
        String[] headers = {"ID", "学号", "姓名", "性别", "出生日期", "联系方式", "班级"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        for (int i = 0; i < list.size(); i++) {
            Student s = list.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(s.getId());
            row.createCell(1).setCellValue(s.getStudentId() != null ? s.getStudentId() : "");
            row.createCell(2).setCellValue(s.getName() != null ? s.getName() : "");
            row.createCell(3).setCellValue(s.getGender() != null ? s.getGender() : "");
            if (s.getBirthDate() != null) {
                Cell dateCell = row.createCell(4);
                dateCell.setCellValue(s.getBirthDate());
                dateCell.setCellStyle(dateStyle);
            } else {
                row.createCell(4).setCellValue("");
            }
            row.createCell(5).setCellValue(s.getPhone() != null ? s.getPhone() : "");
            row.createCell(6).setCellValue(s.getClassName() != null ? s.getClassName() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=students.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // 获取指定课程的所有学生（用于手动点名）
    @GetMapping("/by-course/{courseId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<List<Student>> getStudentsByCourse(@PathVariable Integer courseId) {
        Integer teacherId = SecurityUtil.getCurrentUser().getId();
        return Result.success(studentService.getAllStudentsByTeacherAndCourse(teacherId, courseId));
    }

    // 批量删除
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<String> deleteStudents(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "请选择要删除的学生");
        }
        try {
            studentService.deleteStudents(ids);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            return Result.error(500, "批量删除失败：" + e.getMessage());
        }
    }

    // ===== 学生自助完善个人信息 =====

    // 学生自助创建个人信息
    @PostMapping("/profile")
    public Result<Student> createMyProfile(@RequestBody Student profileData) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            if (currentUser == null) {
                return Result.error(401, "请先登录");
            }
            if (!SecurityUtil.isStudent()) {
                return Result.error(403, "仅学生可操作");
            }
            // 自动使用当前用户姓名（如果未填写）
            if (profileData.getName() == null || profileData.getName().trim().isEmpty()) {
                profileData.setName(currentUser.getName());
            }
            Student saved = studentService.createMyProfile(currentUser.getId(), profileData);
            return Result.success("个人信息完善成功", saved);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "操作失败：" + e.getMessage());
        }
    }

    // 学生自助更新个人信息
    @PutMapping("/profile")
    public Result<Student> updateMyProfile(@RequestBody Student profileData) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            if (currentUser == null) {
                return Result.error(401, "请先登录");
            }
            if (!SecurityUtil.isStudent()) {
                return Result.error(403, "仅学生可操作");
            }
            // 自动使用当前用户姓名（如果未填写）
            if (profileData.getName() != null && profileData.getName().trim().isEmpty()) {
                profileData.setName(null);
            }
            Student saved = studentService.updateMyProfile(currentUser.getId(), profileData);
            return Result.success("个人信息更新成功", saved);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "操作失败：" + e.getMessage());
        }
    }

    // 获取当前学生的个人资料（含课程信息）
    @GetMapping("/profile")
    public Result<Map<String, Object>> getStudentProfile() {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            if (currentUser == null) {
                return Result.error(401, "请先登录");
            }
            Map<String, Object> profile = new java.util.LinkedHashMap<>();
            profile.put("username", currentUser.getUsername());
            profile.put("name", currentUser.getName());
            profile.put("role", currentUser.getRole());

            if (SecurityUtil.isStudent()) {
                // 获取学生记录
                java.util.Optional<Student> studentOpt = studentService.getStudentByUserId(currentUser.getId());
                if (studentOpt.isPresent()) {
                    Student s = studentOpt.get();
                    profile.put("id", s.getId());
                    profile.put("studentId", s.getStudentId());
                    profile.put("className", s.getClassName());
                    profile.put("gender", s.getGender());
                    profile.put("birthDate", s.getBirthDate() != null ? s.getBirthDate().toString() : null);
                    profile.put("phone", s.getPhone());

                    // 获取学生选修的课程
                    List<Integer> courseIds = studentService.getStudentCourseIds(s.getId());
                    profile.put("courseIds", courseIds);
                    profile.put("courseCount", courseIds.size());
                } else {
                    profile.put("studentInfo", null);
                    profile.put("courseCount", 0);
                }
            }
            return Result.success(profile);
        } catch (Exception e) {
            return Result.error(500, "查询失败：" + e.getMessage());
        }
    }
}
