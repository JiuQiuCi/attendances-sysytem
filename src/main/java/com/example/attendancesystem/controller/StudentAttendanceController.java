package com.example.attendancesystem.controller;

import com.example.attendancesystem.common.Result;
import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/student")
public class StudentAttendanceController {

    @Autowired
    private StudentService studentService;

    // 新增学生
    @PostMapping("/add")
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

    // 根据ID查询
    @GetMapping("/{id}")
    public Result<Student> getStudentById(@PathVariable Integer id) {
        Optional<Student> student = studentService.getStudentById(id);
        return student.map(Result::success)
                .orElse(Result.error(404, "学生不存在"));
    }

    // 根据学号查询
    @GetMapping("/studentId/{studentId}")
    public Result<Student> getStudentByStudentId(@PathVariable String studentId) {
        Optional<Student> student = studentService.getStudentByStudentId(studentId);
        return student.map(Result::success)
                .orElse(Result.error(404, "学生不存在"));
    }

    // 根据班级查询
    @GetMapping("/class/{className}")
    public Result<List<Student>> getStudentsByClass(@PathVariable String className) {
        List<Student> students = studentService.getStudentsByClass(className);
        return Result.success(students);
    }

    // 查询所有学生
    @GetMapping("/all")
    public Result<List<Student>> getAllStudents() {
        return Result.success(studentService.getAllStudents());
    }

    // 删除学生
    @DeleteMapping("/{id}")
    public Result<String> deleteStudent(@PathVariable Integer id) {
        studentService.deleteStudent(id);
        return Result.success("删除成功");
    }
}