package com.example.attendancesystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


    @Controller
    public class PageController {

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
}