package com.example.attendancesystem.Service;

import com.example.attendancesystem.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {

    User register(String username, String password, String name, String role);
    // 用户认证（登录）
    boolean authenticate(String username, String rawPassword);
    // 根据用户名查找用户
    Optional<User> findByUsername(String username);
    int addTeacher(User user);
    Optional<User> getUserById(Integer id);
    Optional<User> getUserByUsername(String username);
    List<User> getAllTeachers();
    int updateUser(User user);
    int deleteUser(Integer id);
    User save(User user);


}