package com.example.attendancesystem.entity;

public class AttendanceRecord {
    private String studentId;
    private String date;
    private String status;

    public AttendanceRecord() {}

    public AttendanceRecord(String studentId, String date, String status) {
        this.studentId = studentId;
        this.date = date;
        this.status = status;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
