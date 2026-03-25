package com.example.attendancesystem.Service.impl;

import com.example.attendancesystem.DAO.StudentDao;
import com.example.attendancesystem.entity.AttendanceRecord;
import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentDao studentDao;

    @Override
    public Optional<Student> getStudentById(String studentId) {
        return studentDao.findByStudentId(studentId);
    }

    @Override
    public List<Student> getStudentsByClass(String className, int page, int pageSize) {
        List<Student> students;
        if (className == null || className.trim().isEmpty()) {
            students = studentDao.findAll();
        } else {
            students = studentDao.findByClassName(className);
        }
        // 分页
        int start = (page - 1) * pageSize;
        if (start >= students.size()) {
            return List.of();
        }
        int end = Math.min(start + pageSize, students.size());
        return students.subList(start, end);
    }

    @Override
    public String updateAttendance(AttendanceRecord record) {
        // 业务校验
        if (record.getStudentId() == null || record.getStudentId().isEmpty()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        // 调用Dao保存考勤（假设有 AttendanceDao，此处为示例）
        // studentDao.updateAttendance(record);
        return String.format("学生 %s 的 %s 考勤已更新为：%s",
                record.getStudentId(), record.getDate(), record.getStatus());
    }

    @Override
    public int addStudent(Student student) {
        if (student.getStudentId() == null || student.getStudentId().isEmpty()) {
            throw new IllegalArgumentException("学号不能为空");
        }
        return studentDao.insertStudent(student);
    }
}