package com.example.attendancesystem.specification;

import com.example.attendancesystem.entity.Attendance;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.*;
import java.time.LocalDate;

public class AttendanceSpecification {

    // 条件：学生学号（模糊匹配）
    public static Specification<Attendance> studentIdLike(String studentId) {
        return (root, query, cb) -> {
            if (studentId == null || studentId.isEmpty()) {
                return cb.conjunction();
            }
            Join<Object, Object> studentJoin = root.join("student");
            return cb.like(studentJoin.get("studentId"), "%" + studentId + "%");
        };
    }

    // 条件：考勤日期 >= startDate
    public static Specification<Attendance> attendanceDateAfter(LocalDate startDate) {
        return (root, query, cb) -> {
            if (startDate == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("attendanceDate"), startDate);
        };
    }

    // 条件：考勤日期 <= endDate
    public static Specification<Attendance> attendanceDateBefore(LocalDate endDate) {
        return (root, query, cb) -> {
            if (endDate == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("attendanceDate"), endDate);
        };
    }

    // 条件：状态精确匹配
    public static Specification<Attendance> statusEquals(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isEmpty()) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    // 条件：课程名称（模糊匹配）
    public static Specification<Attendance> courseNameLike(String courseName) {
        return (root, query, cb) -> {
            if (courseName == null || courseName.isEmpty()) return cb.conjunction();
            Join<Object, Object> courseJoin = root.join("course");
            return cb.like(courseJoin.get("name"), "%" + courseName + "%");
        };
    }

    // 条件：座位位置精确匹配
    public static Specification<Attendance> seatPositionEquals(String seatPosition) {
        return (root, query, cb) -> {
            if (seatPosition == null || seatPosition.isEmpty()) return cb.conjunction();
            return cb.equal(root.get("seatPosition"), seatPosition);
        };
    }
}