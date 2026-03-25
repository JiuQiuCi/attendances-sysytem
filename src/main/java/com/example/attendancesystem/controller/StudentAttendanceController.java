package com.example.attendancesystem.controller;

import com.example.attendancesystem.common.Result;
import com.example.attendancesystem.entity.AttendanceRecord;
import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentAttendanceController {

    @Autowired
    private StudentService studentService;

    // 根据学号查询学生信息（路径参数）
    @GetMapping("/info/{studentId}")
    public Result<Student> getStudentById(@PathVariable String studentId) {
        return studentService.getStudentById(studentId)
                .map(Result::success)
                .orElse(Result.error(404, "学生不存在"));
    }

    // 分页查询学生列表（查询参数）
    @GetMapping("/list")
    public Result<List<Student>> listStudents(
            @RequestParam(required = false) String className,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "2") int size) {
        List<Student> students = studentService.getStudentsByClass(className, page, size);
        return Result.success(students);
    }

    // 更新考勤记录（JSON体参数）
    @PostMapping("/attendance/update")
    public Result<String> updateAttendance(@RequestBody AttendanceRecord record) {
        try {
            String message = studentService.updateAttendance(record);
            return Result.success(message);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "系统异常：" + e.getMessage());
        }
    }

    // 新增学生接口（POST）
    @PostMapping("/add")
    public Result<String> addStudent(@RequestBody Student student) {
        try {
            int rows = studentService.addStudent(student);
            if (rows > 0) {
                return Result.success("学生新增成功");
            } else {
                return Result.error("学生新增失败");
            }
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "系统异常：" + e.getMessage());
        }
    }
}