package com.example.attendancesystem.DAO;

import com.example.attendancesystem.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class StudentDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Optional<Student> findByStudentId(String studentId) {
        String sql = "SELECT student_id, name, class_name FROM student WHERE student_id = ?";
        List<Student> list = jdbcTemplate.query(sql, (rs, rowNum) -> new Student(
                rs.getString("student_id"),
                rs.getString("name"),
                rs.getString("class_name")
        ), studentId);
        return list.stream().findFirst();
    }

    public List<Student> findAll() {
        String sql = "SELECT student_id, name, class_name FROM student";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Student(
                rs.getString("student_id"),
                rs.getString("name"),
                rs.getString("class_name")
        ));
    }

    public List<Student> findByClassName(String className) {
        String sql = "SELECT student_id, name, class_name FROM student WHERE class_name = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Student(
                rs.getString("student_id"),
                rs.getString("name"),
                rs.getString("class_name")
        ), className);
    }

    public int insertStudent(Student student) {
        String sql = "INSERT INTO student (student_id, name, class_name) VALUES (?, ?, ?)";
        return jdbcTemplate.update(sql,
                student.getStudentId(),
                student.getName(),
                student.getClassName());
    }
}