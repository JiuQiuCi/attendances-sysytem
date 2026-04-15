package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {

    // 根据学号查询（自定义方法）
    Optional<Student> findByStudentId(String studentId);

    // 根据班级查询（自定义方法）
    List<Student> findByClassName(String className);

    // 根据班级分页查询（需传入 Pageable 参数，这里简单实现，可后续扩展）
    // Page<Student> findByClassName(String className, Pageable pageable);
}
