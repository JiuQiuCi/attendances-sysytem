package com.example.attendancesystem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class StudentController {
    static class AttendanceRequest{
        private String studentId;
        public String getStudentId(){
            return studentId;
        }
        public void setStudentId(String studentId){
            this.studentId = studentId;
        }
    }
    @GetMapping("/student/info")
    public Map<String,String>getStudentInfo(){
        Map<String,String> studentInfo = new HashMap<>();
        studentInfo.put("name:","王杰");
        studentInfo.put("studentInfo:","42411061");
        studentInfo.put("className:","JavaEE开发实践");
        return studentInfo;
    }
    @PostMapping("/student/attendance")
        public String attend(@RequestBody AttendanceRequest request){
        String studentId = request.getStudentId();
        return "学号为"+studentId+" 学生打卡成功";
    }
    @GetMapping("/student/courses")
    public List<String> getCourses(){
        List<String> courses = new ArrayList<>();
        courses.add("Java程序设计");
        courses.add( "数据结构");
        courses.add("数据库原理与应用");
        courses.add("Python数据挖掘");
        return  courses;
    }

}
