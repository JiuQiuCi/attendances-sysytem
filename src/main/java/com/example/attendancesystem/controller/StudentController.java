package com.example.attendancesystem.controller;

import com.example.attendancesystem.common.Result;
import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    // 新增学生（包含新字段）
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

    // 更新学生
    @PutMapping("/update")
    public Result<Student> updateStudent(@RequestBody Student student) {
        try {
            Student updated = studentService.updateStudent(student);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(500, "更新失败：" + e.getMessage());
        }
    }

    // 分页 + 搜索 + 排序
    @GetMapping("/list")
    public Result<Page<Student>> listStudents(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        Page<Student> studentPage = studentService.searchStudents(keyword, pageable);
        return Result.success(studentPage);
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
    public Result<String> deleteStudent(@PathVariable Integer id) {
        studentService.deleteStudent(id);
        return Result.success("删除成功");
    }

    // 批量删除
    @DeleteMapping("/batch")
    public Result<String> deleteStudents(@RequestBody List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "请选择要删除的学生");
        }
        studentService.deleteStudents(ids);
        return Result.success("批量删除成功");
    }
}