package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByStudentId(String studentId);
    List<Student> findByClassName(String className);
}