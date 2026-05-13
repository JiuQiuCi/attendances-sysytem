package com.example.attendancesystem.repository;

import com.example.attendancesystem.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer>, JpaSpecificationExecutor<Student> {

    Optional<Student> findByStudentId(String studentId);
    List<Student> findByClassName(String className);

    // 模糊搜索（名称或学号）
    @Query("SELECT s FROM Student s WHERE s.name LIKE %:keyword% OR s.studentId LIKE %:keyword%")
    Page<Student> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 批量删除
    void deleteByIdIn(List<Integer> ids);
}