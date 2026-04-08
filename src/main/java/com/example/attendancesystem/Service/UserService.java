package com.example.attendancesystem.Service;

import com.example.attendancesystem.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    int addTeacher(User user);
    Optional<User> getUserById(Integer id);
    Optional<User> getUserByUsername(String username);
    List<User> getAllTeachers();
    int updateUser(User user);
    int deleteUser(Integer id);
}