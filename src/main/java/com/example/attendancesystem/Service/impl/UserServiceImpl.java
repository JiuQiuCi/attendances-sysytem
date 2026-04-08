package com.example.attendancesystem.Service.impl;

import com.example.attendancesystem.DAO.UserDao;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public int addTeacher(User user) {
        // 业务校验：用户名不能为空，且角色必须是 teacher
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!"teacher".equals(user.getRole())) {
            throw new IllegalArgumentException("只能添加教师角色");
        }
        // 可添加其他校验（如用户名是否已存在）
        return userDao.insert(user);
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        return userDao.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public List<User> getAllTeachers() {
        return userDao.findAllTeachers();
    }

    @Override
    public int updateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return userDao.update(user);
    }

    @Override
    public int deleteUser(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        return userDao.deleteById(id);
    }
}