package com.example.attendancesystem.Service.impl;

import com.example.attendancesystem.DAO.UserDao;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.attendancesystem.config.PasswordEncoderConfig;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;
import java.util.Optional;

import com.example.attendancesystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.time.LocalDateTime;
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

        @Autowired
        private UserRepository userRepository;


       private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        @Override
        public Optional<User> findByUsername(String username) {
            return userRepository.findByUsername(username);
        }

        @Override
        public User save(User user) {
            return userRepository.save(user);
        }

        @Override
        public boolean authenticate(String username, String rawPassword) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return passwordEncoder.matches(rawPassword, user.getPassword());
            }
            return false;
        }

        @Override
        public User register(String username, String rawPassword, String name, String role) {
            // 检查用户名是否已存在
            if (userRepository.findByUsername(username).isPresent()) {
                throw new IllegalArgumentException("用户名已存在");
            }
            // 默认角色为 student
            String finalRole = (role == null || role.isEmpty()) ? "student" : role;
            // 加密密码
            String encodedPassword = passwordEncoder.encode(rawPassword);
            User user = new User();
            user.setUsername(username);
            user.setPassword(encodedPassword);
            user.setName(name);
            user.setRole(finalRole);
            user.setCreatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
}