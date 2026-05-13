package com.example.attendancesystem.controller;

import com.example.attendancesystem.Service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
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
}