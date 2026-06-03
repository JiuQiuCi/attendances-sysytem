package com.example.attendancesystem.Service.impl;

import com.example.attendancesystem.entity.Attendance;
import com.example.attendancesystem.entity.Course;
import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.repository.AttendanceRepository;
import com.example.attendancesystem.repository.CourseRepository;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.Service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    // 签到
    @Override
    public Attendance checkIn(Integer studentId, Integer courseId, LocalDate date) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));

        LocalTime now = LocalTime.now();
        LocalTime startTime = course.getStartTime();
        if (startTime == null) {
            throw new IllegalArgumentException("该课程未设置上课时间，无法打卡");
        }
        LocalTime earliest = startTime.minusMinutes(15);
        LocalTime latest = startTime.plusMinutes(30);
        if (now.isBefore(earliest) || now.isAfter(latest)) {
            throw new IllegalArgumentException("只能在课程开始前15分钟至开始后30分钟内签到");
        }

        String status = now.isAfter(startTime) ? "late" : "present";

        Optional<Attendance> existing = attendanceRepository.findByStudentIdAndCourseIdAndAttendanceDate(studentId, courseId, date);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("今日已签到，请勿重复操作");
        }

        Attendance attendance = new Attendance();
        attendance.setStudent(student);
        attendance.setCourse(course);
        attendance.setAttendanceDate(date);
        attendance.setStatus(status);
        attendance.setCreatedAt(LocalDateTime.now());
        return attendanceRepository.save(attendance);
    }

    // 手动标记考勤状态
    @Override
    public Attendance markAttendance(Integer studentId, Integer courseId, LocalDate date, String status) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));

        if (!Set.of("present", "absent", "late", "early_leave").contains(status)) {
            throw new IllegalArgumentException("无效状态: " + status);
        }

        // Upsert: if record exists for this student+course+date, update it; otherwise create
        Attendance attendance = attendanceRepository
                .findByStudentIdAndCourseIdAndAttendanceDate(studentId, courseId, date)
                .orElse(null);

        if (attendance == null) {
            attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setCourse(course);
            attendance.setAttendanceDate(date);
            attendance.setCreatedAt(LocalDateTime.now());
        }
        attendance.setStatus(status);
        return attendanceRepository.save(attendance);
    }

    // 签退（早退检测）
    @Override
    public Attendance checkOut(Integer studentId, Integer courseId, LocalDate date) {
        Attendance attendance = attendanceRepository.findByStudentIdAndCourseIdAndAttendanceDate(studentId, courseId, date)
                .orElseThrow(() -> new IllegalArgumentException("未找到今日签到记录，请先签到"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("课程不存在"));

        LocalTime now = LocalTime.now();
        LocalTime endTime = course.getEndTime();
        if (endTime == null) {
            throw new IllegalArgumentException("该课程未设置下课时间，无法签退");
        }

        if (now.isBefore(endTime)) {
            attendance.setStatus("early_leave");
        } else {
            if (!"late".equals(attendance.getStatus())) {
                attendance.setStatus("present");
            }
        }
        return attendanceRepository.save(attendance);
    }

    @Override
    public Attendance saveAttendance(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }

    @Override
    public Optional<Attendance> getAttendanceById(Integer id) {
        return attendanceRepository.findById(id);
    }

    @Override
    public List<Attendance> getAttendancesByStudent(Integer studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    @Override
    public List<Attendance> getAttendancesByCourse(Integer courseId) {
        return attendanceRepository.findByCourseId(courseId);
    }

    @Override
    public List<Attendance> getAttendancesByStudentAndDateRange(Integer studentId, LocalDate start, LocalDate end) {
        return attendanceRepository.findByStudentIdAndAttendanceDateBetween(studentId, start, end);
    }

    @Override
    public List<Attendance> getAttendancesByCourseAndDate(Integer courseId, LocalDate date) {
        return attendanceRepository.findAttendancesByCourseAndDate(courseId, date);
    }

    @Override
    public void deleteAttendance(Integer id) {
        attendanceRepository.deleteById(id);
    }

    @Override
    public void deleteAttendances(List<Integer> ids) {
        attendanceRepository.deleteAllById(ids);
    }

    @Override
    public Page<Attendance> getAttendancesPage(Pageable pageable) {
        return attendanceRepository.findAll(pageable);
    }

    @Override
    public Page<Attendance> searchAttendances(Integer courseId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Specification<Attendance> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (courseId != null) {
                Join<Object, Object> courseJoin = root.join("course");
                predicates.add(cb.equal(courseJoin.get("id"), courseId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("attendanceDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("attendanceDate"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return attendanceRepository.findAll(spec, pageable);
    }

    @Override
    public List<Attendance> exportAttendances(Integer courseId, LocalDate startDate, LocalDate endDate) {
        Specification<Attendance> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (courseId != null) {
                Join<Object, Object> courseJoin = root.join("course");
                predicates.add(cb.equal(courseJoin.get("id"), courseId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("attendanceDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("attendanceDate"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return attendanceRepository.findAll(spec);
    }
}