package com.example.attendancesystem.controller;

import com.example.attendancesystem.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/student/form")
    public String studentForm(@RequestParam(required = false) Integer id, Model model) {
        if (id != null) {
            studentService.getStudentById(id).ifPresent(student -> model.addAttribute("student", student));
        }
        return "student-form";
    }

    @GetMapping("/student/list-page")
    public String studentListPage() {
        return "student-list";
    }

    @GetMapping("/attendance/list-page")
    public String attendanceListPage() {
        return "attendance_list";
    }

    @GetMapping("/course/list-page")
    public String courseListPage() {
        return "course-list";
    }

    @GetMapping("/course/form")
    public String courseForm(@RequestParam(required = false) Integer id, Model model) {
        if (id != null) {
            // Course will be loaded via JS on the form page
            model.addAttribute("courseId", id);
        }
        return "course-form";
    }

    @GetMapping("/leave/list-page")
    public String leaveListPage() {
        return "leave-list";
    }

    @GetMapping("/mobile-checkin")
    public String mobileCheckin() {
        return "mobile-checkin";
    }

    @GetMapping("/")
    public String root() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:/index";
        }
        return "redirect:/login";
    }
}