package com.example.attendancesystem.controller;

import com.example.attendancesystem.common.Result;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // 新增教师
    @PostMapping("/teacher")
    public Result<String> addTeacher(@RequestBody User user) {
        try {
            int rows = userService.addTeacher(user);
            return rows > 0 ? Result.success("教师添加成功") : Result.error("添加失败");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "系统错误：" + e.getMessage());
        }
    }

    // 根据ID查询用户
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Integer id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(Result::success)
                .orElse(Result.error(404, "用户不存在"));
    }

    // 根据用户名查询（登录验证）
    @GetMapping("/username/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.getUserByUsername(username);
        return user.map(Result::success)
                .orElse(Result.error(404, "用户不存在"));
    }

    // 查询所有教师
    @GetMapping("/teachers")
    public Result<List<User>> listTeachers() {
        List<User> teachers = userService.getAllTeachers();
        return Result.success(teachers);
    }

    // 更新用户
    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody User user) {
        try {
            int rows = userService.updateUser(user);
            return rows > 0 ? Result.success("更新成功") : Result.error("更新失败");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "系统错误：" + e.getMessage());
        }
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Integer id) {
        try {
            int rows = userService.deleteUser(id);
            return rows > 0 ? Result.success("删除成功") : Result.error("删除失败");
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            return Result.error(500, "系统错误：" + e.getMessage());
        }
    }
}