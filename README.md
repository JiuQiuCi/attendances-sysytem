# 课堂考勤管理系统

> 📋 **Spring Boot + PostgreSQL + Thymeleaf** 全栈考勤管理系统  
> 支持教师管理课程/学生/考勤，学生签到/选课/请假，数据统计与 Excel 导入导出

---

## 👤 个人信息

| 项目 | 内容 |
|------|------|
| **姓名** | 王杰 |
| **学号** | 42411061 |
| **班级** | 计算机科学与技术1班 |

---

## 📖 目录

- [1. 项目概述](#1-项目概述)
- [2. 技术栈](#2-技术栈)
- [3. 功能模块](#3-功能模块)
- [4. 项目结构](#4-项目结构)
- [5. 数据库设计](#5-数据库设计)
- [6. API 接口文档](#6-api-接口文档)
- [7. 部署文档](#7-部署文档)
- [8. 用户使用手册](#8-用户使用手册)
- [9. 测试报告](#9-测试报告)

---

## 1. 项目概述

### 1.1 背景

本系统为一款基于 Web 的课堂考勤管理平台，服务于高校教师和学生的日常考勤需求。系统支持教师发布课程、管理学生、记录考勤、审批请假，学生可自助签到、选课、申请请假及查看个人考勤统计。

### 1.2 核心特性

- 🔐 **角色权限**：教师(TEACHER)与学生(STUDENT)双角色，基于 Spring Security 的 RBAC 权限控制
- 📚 **课程管理**：教师建课/管课，学生自主选课/退课，支持容量上限控制
- ✅ **考勤签到**：支持签到/签退/手动标记，含迟到判定（0.8权重）、座位定位
- 📝 **请假管理**：学生申请 → 教师审批，含时间重叠检测、≤3天限制
- 📊 **数据统计**：出勤率统计（总体/按班/按周/按月/按日期），教师可导出 Excel 报表
- 📥 **Excel 导入**：支持批量导入考勤记录和学生名单（Apache POI），格式校验 + 10MB 上限
- 📤 **Excel 导出**：考勤/课程/学生/统计报表一键导出
- 🛡️ **数据隔离**：教师仅见自己课程的数据，学生仅见个人数据
- 📱 **移动端签到**：独立的移动签到页面，适配手机浏览器

---

## 2. 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Spring Boot | 2.7.18 |
| **开发语言** | Java | 17 |
| **安全框架** | Spring Security | (同Spring Boot) |
| **模板引擎** | Thymeleaf | (同Spring Boot) |
| **ORM** | Spring Data JPA + Hibernate | (同Spring Boot) |
| **数据库** | PostgreSQL | — |
| **JDBC备用** | JdbcTemplate | (同Spring Boot) |
| **Excel处理** | Apache POI (poi-ooxml) | 5.2.5 |
| **简化代码** | Lombok | (同Spring Boot) |
| **构建工具** | Maven | (wrapper) |
| **前端** | HTML5 + CSS3 + JavaScript | Fetch API (无框架) |
| **密码加密** | BCrypt | (Spring Security) |

---

## 3. 功能模块

```
┌─────────────────────────────────────────────────┐
│               课堂考勤管理系统                      │
├─────────────┬─────────────┬─────────────────────┤
│  教师端       │  学生端       │  公共模块             │
├─────────────┼─────────────┼─────────────────────┤
│ • 课程CRUD   │ • 自主选课    │ • 用户注册/登录        │
│ • 学生管理    │ • 完善个人资料 │ • 角色路由            │
│ • 考勤签到/标记│ • 签到/签退   │ • Excel导入导出      │
│ • 请假审批    │ • 请假申请    │ • 数据统计            │
│ • 报表统计    │ • 查看出勤率  │                     │
│ • 学生名单导入│ • 移动签到    │                     │
└─────────────┴─────────────┴─────────────────────┘
```

### 3.1 教师端功能

| 功能 | 说明 |
|------|------|
| 课程管理 | 新增/编辑/删除课程，支持教室、座位布局、时间、容量设置 |
| 学生管理 | 查看/搜索/添加/删除学生，按课程筛选，Excel 导出 |
| 选课管理 | 为课程登记/移除学生，支持批量操作 |
| 考勤管理 | 签到/签退/手动标记(正常/缺勤/迟到)，按日期/课程筛选 |
| 请假审批 | 查看待审批请假，批准/拒绝（含课程归属校验），批量审批 |
| 统计报表 | 总体/按班级/按日期/按周/按月出勤率，导出Excel |

### 3.2 学生端功能

| 功能 | 说明 |
|------|------|
| 个人信息 | 完善/更新学号、班级、性别、生日、手机号 |
| 选课退课 | 查看可选课程（含是否已选、选课人数），自主选课/退课 |
| 考勤签到 | 打卡签到，含座位位置记录，迟到自动判定 |
| 请假申请 | 提交请假（时段+原因），查看审批状态 |
| 出勤统计 | 查看个人出勤率统计 |
| 移动签到 | 手机端专用签到页面 |

---

## 4. 项目结构

```
attendance-system/
├── pom.xml                          # Maven 依赖配置
├── settings.xml                     # Maven 镜像配置（阿里云）
├── mvnw / mvnw.cmd                  # Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/com/example/attendancesystem/
│   │   │   ├── AttendanceSystemApplication.java   # 启动入口
│   │   │   ├── common/
│   │   │   │   └── Result.java                    # 统一响应封装
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java            # Spring Security 配置
│   │   │   │   ├── PasswordEncoderConfig.java     # BCrypt 密码编码器
│   │   │   │   ├── CustomAuthenticationEntryPoint.java  # 未认证处理
│   │   │   │   ├── CustomAccessDeniedHandler.java       # 未授权处理
│   │   │   │   └── DatabaseFixInitializer.java         # 数据库初始化修正
│   │   │   ├── controller/
│   │   │   │   ├── PageController.java            # 页面路由
│   │   │   │   ├── AuthController.java            # 登录/注册/登出
│   │   │   │   ├── UserController.java            # 用户管理(教师)
│   │   │   │   ├── StudentController.java         # 学生管理
│   │   │   │   ├── CourseController.java          # 课程管理 + 选课
│   │   │   │   ├── AttendanceController.java      # 考勤签到/记录
│   │   │   │   ├── LeaveController.java           # 请假申请/审批
│   │   │   │   ├── ReportController.java          # 统计报表
│   │   │   │   └── FileUploadController.java      # Excel导入
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java              # 登录请求体
│   │   │   │   ├── RegisterRequest.java           # 注册请求体
│   │   │   │   ├── LeaveApplicationDTO.java       # 请假申请DTO
│   │   │   │   ├── AttendanceStatsVO.java         # 考勤统计VO
│   │   │   │   └── ImportResult.java              # 导入结果
│   │   │   ├── entity/
│   │   │   │   ├── User.java                      # 用户（含Spring Security UserDetails）
│   │   │   │   ├── Student.java                   # 学生
│   │   │   │   ├── Course.java                    # 课程
│   │   │   │   ├── CourseStudent.java             # 选课关联
│   │   │   │   ├── Attendance.java                # 考勤记录
│   │   │   │   └── LeaveApplication.java          # 请假申请
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── StudentRepository.java
│   │   │   │   ├── CourseRepository.java
│   │   │   │   ├── CourseStudentRepository.java
│   │   │   │   ├── AttendanceRepository.java
│   │   │   │   └── LeaveApplicationRepository.java
│   │   │   ├── Service/
│   │   │   │   ├── UserService.java               # 用户服务接口
│   │   │   │   ├── StudentService.java
│   │   │   │   ├── CourseService.java
│   │   │   │   ├── AttendanceService.java
│   │   │   │   ├── LeaveService.java
│   │   │   │   ├── StatisticsService.java
│   │   │   │   ├── ExcelImportService.java
│   │   │   │   └── impl/                          # 各服务实现类
│   │   │   ├── specification/
│   │   │   │   └── AttendanceSpecification.java   # JPA动态查询
│   │   │   ├── DAO/
│   │   │   │   └── UserDao.java                   # JDBC DAO（遗留）
│   │   │   └── util/
│   │   │       └── SecurityUtil.java              # 安全工具类
│   │   └── resources/
│   │       ├── application.properties             # 应用配置
│   │       ├── static/
│   │       │   ├── js/
│   │       │   │   ├── auth.js                    # 登录/注册前端逻辑
│   │       │   │   └── auth-check.js              # 前端权限检查
│   │       │   └── images/                        # 图片资源
│   │       └── templates/                         # Thymeleaf 模板页
│   │           ├── login.html                     # 登录页
│   │           ├── register.html                  # 注册页
│   │           ├── index.html                     # 仪表盘首页
│   │           ├── student-list.html              # 学生列表
│   │           ├── student-form.html              # 学生表单
│   │           ├── student-info.html              # 学生个人信息
│   │           ├── course-list.html               # 课程列表
│   │           ├── course-form.html               # 课程表单
│   │           ├── course-selection.html          # 学生选课
│   │           ├── attendance_list.html           # 考勤记录
│   │           ├── leave-list.html                # 请假管理
│   │           ├── report.html                    # 统计报表
│   │           └── mobile-checkin.html            # 移动签到
│   └── test/java/                                 # 测试代码
└── 测试检查清单.md                                  # 完整测试检查清单
```

---

## 5. 数据库设计

### 5.1 ER图概览

```
┌──────────┐     ┌─────────────┐     ┌──────────┐
│   User   │1───1│   Student   │1───*│Attendance│
│  (用户)   │     │   (学生)     │     │ (考勤记录) │
└──────────┘     └──────┬──────┘     └────┬─────┘
                        │*                │*
                        │                 │
              ┌─────────┴───────┐         │
              │  CourseStudent  │    ┌────┴─────┐
              │   (选课关联)     │    │  Course   │
              └─────────┬───────┘    │  (课程)   │
                        │*           └────┬─────┘
                        │                │1
                        │           ┌────┴──────────┐
                        │           │LeaveApplication│
                        │           │   (请假申请)    │
                        │           └───────────────┘
                        │*                 │1
              ┌─────────┴───────┐         │
              │  CourseStudent  │    ┌────┴─────┐
              │   (选课关联)     │    │  Course   │
              └─────────┬───────┘    │  (课程)   │
                        │*           └──────────┘
```

### 5.2 数据表结构

#### 📌 users（用户表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INTEGER | PK, AUTO | 用户ID |
| username | VARCHAR(255) | UNIQUE, NOT NULL | 用户名 |
| password | VARCHAR(255) | NOT NULL | 密码（BCrypt加密） |
| role | VARCHAR(50) | — | 角色：teacher / student |
| name | VARCHAR(100) | — | 真实姓名 |
| created_at | TIMESTAMP | — | 创建时间 |

#### 📌 students（学生表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INTEGER | PK, AUTO | 记录ID |
| student_id | VARCHAR(20) | UNIQUE, NOT NULL | 学号 |
| name | VARCHAR(50) | NOT NULL | 姓名 |
| class_name | VARCHAR(50) | — | 班级 |
| gender | VARCHAR(10) | — | 性别 |
| birth_date | DATE | — | 出生日期 |
| phone | VARCHAR(20) | — | 联系电话 |
| user_id | INTEGER | FK → users.id | 关联的系统用户 |
| created_at | TIMESTAMP | — | 创建时间 |

#### 📌 courses（课程表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INTEGER | PK, AUTO | 课程ID |
| name | VARCHAR(100) | NOT NULL | 课程名称 |
| code | VARCHAR(50) | — | 课程代码 |
| teacher_id | INTEGER | FK → users.id | 授课教师ID |
| teacher_name | VARCHAR(50) | — | 授课教师姓名 |
| classroom | VARCHAR(50) | — | 教室 |
| classroom_layout | VARCHAR(255) | — | 教室座位布局 |
| start_time | TIME | — | 上课时间 |
| end_time | TIME | — | 下课时间 |
| week_day | VARCHAR(10) | — | 星期（MONDAY~SUNDAY） |
| semester | INTEGER | — | 学期 |
| max_students | INTEGER | — | 课程容量上限 |
| created_at | TIMESTAMP | — | 创建时间 |

#### 📌 course_students（选课关联表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INTEGER | PK, AUTO | 记录ID |
| course_id | INTEGER | FK, UNIQUE(course_id,student_id) | 课程ID |
| student_id | INTEGER | FK, UNIQUE(course_id,student_id) | 学生ID |
| enrolled_at | TIMESTAMP | — | 选课时间 |

#### 📌 attendances（考勤记录表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INTEGER | PK, AUTO | 记录ID |
| student_id | INTEGER | FK, NOT NULL | 学生ID |
| course_id | INTEGER | FK, NOT NULL | 课程ID |
| attendance_date | DATE | NOT NULL | 考勤日期 |
| status | VARCHAR(20) | NOT NULL | 状态：present/absent/late |
| seat_position | VARCHAR(50) | — | 座位位置 |
| created_at | TIMESTAMP | — | 签到时间 |

#### 📌 leave_applications（请假申请表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | INTEGER | PK, AUTO | 请假ID |
| student_id | INTEGER | NOT NULL | 学生ID |
| course_id | INTEGER | — | 关联课程ID |
| start_time | TIMESTAMP | NOT NULL | 请假开始时间 |
| end_time | TIMESTAMP | NOT NULL | 请假结束时间 |
| reason | VARCHAR(500) | — | 请假原因 |
| status | VARCHAR(20) | NOT NULL | 状态：待审批/已批准/已拒绝 |
| apply_time | TIMESTAMP | — | 申请时间 |
| approval_time | TIMESTAMP | — | 审批时间 |
| approver_remark | VARCHAR(500) | — | 审批备注 |

### 5.3 索引建议

| 表 | 建议索引 | 用途 |
|----|---------|------|
| attendances | (course_id, attendance_date) | 按课程和日期查询 |
| attendances | (student_id, attendance_date) | 按学生和日期查询 |
| attendances | (status) | 按状态统计 |
| leave_applications | (student_id, course_id) | 按学生+课程查询 |
| leave_applications | (status) | 按审批状态筛选 |
| course_students | (course_id) | 查询课程选课人数 |
| students | (user_id) | 通过用户ID查学生 |
| courses | (teacher_id) | 教师查自己课程 |

---

## 6. API 接口文档

> **通用说明**  
> - 基础路径：`http://localhost:8081`  
> - 成功响应：`{ "code": 200, "message": "...", "data": {...} }`  
> - 错误响应：`{ "code": 4xx/5xx, "message": "...", "data": null }`  
> - 认证方式：基于 Session 的表单登录（Spring Security）  
> - CSRF：已关闭  
> - 角色标注：🔒 = 需教师角色，🔓 = 公开/登录即可  

---

### 6.1 认证模块 `/auth`

| 方法 | 路径 | 权限 | 说明 | 参数 |
|------|------|------|------|------|
| POST | `/auth/login` | 🔓 | 用户登录 | body: `{username, password}` |
| POST | `/auth/register` | 🔓 | 用户注册 | body: `{username, password, name, role}` |
| POST | `/auth/logout` | 🔓 | 退出登录 | — |
| GET | `/auth/me` | 🔓 | 获取当前用户信息 | — |

**注册示例：**
```json
POST /auth/register
{
  "username": "zhangsan",
  "password": "123456",
  "name": "张三",
  "role": "student"
}
```

---

### 6.2 用户管理 `/user`（🔒 教师）

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | `/user/teacher` | 新增教师 | body: User 对象 |
| GET | `/user/{id}` | 按ID查用户 | path: id |
| GET | `/user/username/{username}` | 按用户名查用户 | path: username |
| GET | `/user/teachers` | 查询所有教师 | — |
| PUT | `/user/update` | 更新用户信息 | body: User 对象 |
| DELETE | `/user/{id}` | 删除用户 | path: id |

---

### 6.3 学生管理 `/student`

| 方法 | 路径 | 权限 | 说明 | 参数 |
|------|------|------|------|------|
| POST | `/student/add` | 🔒 | 新增学生 | body: Student |
| PUT | `/student/update` | 🔒 | 更新学生 | body: Student |
| GET | `/student/list` | 🔒 | 分页搜索学生 | keyword, page, size, sortBy, direction, courseId |
| GET | `/student/{id}` | 🔓 | 按ID查学生 | path: id |
| GET | `/student/quick-search` | 🔓 | 快速搜索学生 | q, courseId |
| GET | `/student/by-course/{courseId}` | 🔒 | 获取课程学生列表 | path: courseId |
| DELETE | `/student/{id}` | 🔒 | 删除学生 | path: id |
| DELETE | `/student/batch` | 🔒 | 批量删除 | body: [id1, id2, ...] |
| GET | `/student/export` | 🔒 | 导出学生Excel | courseId, keyword |
| POST | `/student/profile` | 学生 | 学生自助完善信息 | body: Student |
| PUT | `/student/profile` | 学生 | 学生自助更新信息 | body: Student |
| GET | `/student/profile` | 学生 | 查看个人资料 | — |

---

### 6.4 课程管理 `/course`

| 方法 | 路径 | 权限 | 说明 | 参数 |
|------|------|------|------|------|
| POST | `/course/add` | 🔒 | 新增课程 | body: Course |
| PUT | `/course/update` | 🔒 | 更新课程 | body: Course |
| GET | `/course/list` | 🔓 | 分页搜索课程 | keyword, semester, page, size, sortBy, direction, myOnly |
| GET | `/course/all` | 🔓 | 获取所有课程 | — |
| GET | `/course/semesters` | 🔓 | 获取学期列表 | — |
| GET | `/course/{id}` | 🔓 | 按ID查课程 | path: id |
| GET | `/course/teacher/{teacherId}` | 🔓 | 按教师查课程 | path: teacherId |
| DELETE | `/course/{id}` | 🔒 | 删除课程 | path: id |
| DELETE | `/course/batch` | 🔒 | 批量删除课程 | body: [id1, id2, ...] |
| GET | `/course/export` | 🔒 | 导出课程Excel | — |
| GET | `/course/available` | 学生 | 可选课程列表 | keyword, semester |
| POST | `/course/{id}/enroll` | 学生 | 学生自主选课 | path: id |
| DELETE | `/course/{id}/enroll` | 学生 | 学生自主退课 | path: id |
| POST | `/course/{courseId}/students/{studentId}` | 🔒 | 教师登记学生 | path: courseId, studentId |
| POST | `/course/{courseId}/students/batch` | 🔒 | 批量登记学生 | path: courseId, body: [studentId, ...] |
| DELETE | `/course/{courseId}/students/{studentId}` | 🔒 | 移除学生 | path: courseId, studentId |
| DELETE | `/course/{courseId}/students/batch` | 🔒 | 批量移除学生 | path: courseId, body: [studentId, ...] |

---

### 6.5 考勤管理 `/attendance`

| 方法 | 路径 | 权限 | 说明 | 参数 |
|------|------|------|------|------|
| POST | `/attendance/checkin` | 🔓 | 签到 | studentId, courseId, date |
| POST | `/attendance/checkout` | 🔓 | 签退 | studentId, courseId, date |
| POST | `/attendance/mark` | 🔓 | 手动标记状态 | studentId, courseId, date, status |
| GET | `/attendance/list` | 🔓 | 分页查询考勤 | courseId, startDate, endDate, page, size, sortBy, direction |
| GET | `/attendance/courses` | 🔓 | 获取课程列表（数据隔离） | — |
| GET | `/attendance/{id}` | 🔓 | 按ID查考勤 | path: id |
| DELETE | `/attendance/{id}` | 🔓 | 删除考勤记录 | path: id |
| DELETE | `/attendance/batch` | 🔓 | 批量删除 | body: [id1, id2, ...] |
| GET | `/attendance/export` | 🔓 | 导出考勤Excel | courseId, startDate, endDate |

**签到业务规则：**
- 签到窗口：上课时间前15分钟 → 上课时间后30分钟
- 在此窗口内签到 → 状态 `present`
- 超过上课时间但未超30分钟 → 状态 `late`（迟到）
- 迟到出勤率按 **0.8 权重** 计算

---

### 6.6 请假管理 `/api/leave`

| 方法 | 路径 | 权限 | 说明 | 参数 |
|------|------|------|------|------|
| POST | `/api/leave/apply` | 学生 | 提交请假申请 | body: LeaveApplicationDTO |
| POST | `/api/leave/approve/{id}` | 🔒 | 审批请假 | path: id, approved, remark |
| POST | `/api/leave/batch-approve` | 🔒 | 批量审批 | body: {ids, approved, remark} |
| GET | `/api/leave/list` | 🔓 | 分页查询请假 | studentId, courseId, status, startDate, endDate, page, size, sortBy, direction |
| GET | `/api/leave/{id}` | 🔓 | 按ID查请假 | path: id |
| GET | `/api/leave/pending-count` | 🔒 | 待审批数量 | — |
| DELETE | `/api/leave/{id}` | 🔓 | 删除请假 | path: id |
| DELETE | `/api/leave/batch` | 🔒 | 批量删除 | body: [id1, id2, ...] |

**请假业务规则：**
- 单次请假最长 **≤ 3 天**
- 同一学生请假时段 **不可重叠**（与已有请假记录冲突时拒绝）
- 学生须已选课（对应课程中有 enrollment 记录）
- 学生只能删除自己 **待审批** 状态的请假

---

### 6.7 统计报表 `/report`

| 方法 | 路径 | 权限 | 说明 | 参数 |
|------|------|------|------|------|
| GET | `/report` | 🔓 | 报表页面 | — |
| GET | `/report/overall` | 🔓 | 总体/个人统计 | courseId |
| GET | `/report/by-class` | 🔒 | 按班级统计 | className, courseId |
| GET | `/report/by-date` | 🔒 | 按日期统计 | startDate, endDate, courseId |
| GET | `/report/export` | 🔒 | 导出报表Excel | courseId, startDate, endDate |

**统计指标：**
- `overallRate`：总出勤率（present + late×0.8）/ 总记录数
- `presentCount` / `absentCount` / `lateCount`：分类计数
- `weeklyRate`：每周出勤率 Map
- `monthlyRate`：每月出勤率 Map
- `classRate`：班级出勤率 Map

---

### 6.8 Excel 导入 `/file`（🔒 教师）

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| POST | `/file/upload` | 上传Excel文件 | file (multipart), courseId (可选) |

**文件校验规则：**
- 允许格式：`.xlsx` / `.xls`
- 最大体积：**10MB**
- 传入 `courseId` → 作为学生名单导入到指定课程
- 不传 `courseId` → 作为考勤记录导入

---

### 6.9 页面路由

| 路径 | 模板 | 说明 |
|------|------|------|
| `/` | (重定向) | 已登录→/index，未登录→/login |
| `/login` | login.html | 登录页面 |
| `/register` | register.html | 注册页面 |
| `/index` | index.html | 仪表盘首页 |
| `/student/list-page` | student-list.html | 学生列表 |
| `/student/form` | student-form.html | 学生表单（?id= 编辑模式） |
| `/student/info-page` | student-info.html | 个人信息 |
| `/student/course-selection` | course-selection.html | 学生选课 |
| `/course/list-page` | course-list.html | 课程列表 |
| `/course/form` | course-form.html | 课程表单 |
| `/attendance/list-page` | attendance_list.html | 考勤记录 |
| `/leave/list-page` | leave-list.html | 请假管理 |
| `/report` | report.html | 统计报表 |
| `/mobile-checkin` | mobile-checkin.html | 移动签到 |

---

## 7. 部署文档

### 7.1 环境要求

| 组件 | 版本/说明 |
|------|-----------|
| **JDK** | Java 17+ |
| **Maven** | 3.6+（项目内置 Maven Wrapper） |
| **PostgreSQL** | 10+（推荐 14+） |
| **操作系统** | Windows / Linux / macOS |
| **端口** | 8081（可配置） |

### 7.2 数据库初始化

1. 启动 PostgreSQL 服务
2. 创建数据库：

```sql
CREATE DATABASE attendance_system
  WITH ENCODING = 'UTF8'
  LC_COLLATE = 'Chinese (Simplified)_China.936'
  LC_CTYPE = 'Chinese (Simplified)_China.936'
  TEMPLATE = template0;
```

3. 确认 `application.properties` 中数据库连接信息正确：

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/attendance_system
spring.datasource.username=postgres
spring.datasource.password=你的密码
```

### 7.3 启动服务

```bash
# 1. 克隆项目
git clone <仓库地址>
cd attendance-system

# 2. 编译
mvnw compile          # Windows
./mvnw compile        # Linux/Mac

# 3. 启动（Hibernate 自动建表）
mvnw spring-boot:run

# 4. 访问
# 浏览器打开: http://localhost:8081
```

首次启动时，Hibernate 会根据实体类自动创建数据库表（`ddl-auto=update`）。

### 7.4 打包部署

```bash
# 打包为可执行 JAR
mvnw clean package -DskipTests

# 运行 JAR
java -jar target/attendance-system-0.0.1-SNAPSHOT.jar

# 指定端口
java -jar target/attendance-system-0.0.1-SNAPSHOT.jar --server.port=9090
```

### 7.5 配置说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | 8081 | 服务端口 |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/attendance_system` | 数据库连接 |
| `spring.jpa.hibernate.ddl-auto` | update | 表结构自动更新 |
| `spring.jpa.show-sql` | true | 控制台打印SQL |
| `server.servlet.session.timeout` | 30m | 会话超时 |

---

## 8. 用户使用手册

### 8.1 快速开始

1. **注册账号**：访问 `/register`，填写用户名、密码、姓名，选择角色（教师/学生）
2. **登录系统**：访问 `/login`，输入账号密码
3. **进入首页**：登录成功自动跳转仪表盘，根据角色显示不同功能入口

### 8.2 教师操作指南

#### 创建课程
1. 点击「课程管理」→「新增课程」
2. 填写课程名称、代码、教室、上课时间、星期、学期、容量上限
3. 保存后课程自动归属当前教师

#### 管理学生
1. 进入「学生管理」，可按学号/姓名搜索
2. 点击添加按钮新增学生记录
3. 在课程详情中登记/移除选课学生
4. 支持 Excel 批量导入学生名单

#### 考勤签到
1. 进入「考勤管理」，选择课程
2. 输入学生ID、课程ID、日期，点击签到/签退
3. 可手动标记为「正常/缺勤/迟到」
4. 迟到自动判定：晚于上课时间但在30分钟内

#### 审批请假
1. 进入「请假管理」，查看待审批列表
2. 查看请假详情（时段+原因）
3. 点击「批准」或「拒绝」，可填写审批备注
4. 支持批量审批

#### 查看统计
1. 进入「数据报表」
2. 选择课程和日期范围查看出勤率
3. 支持按班级/按周/按月维度分析
4. 一键导出 Excel 报表

### 8.3 学生操作指南

#### 完善个人信息
1. 登录后点击右上角头像 → 个人信息
2. 填写学号、班级、性别、出生日期、手机号
3. 保存提交

#### 选课
1. 点击「课程选修」
2. 浏览可选课程列表（显示教师、教室、时间、已选人数）
3. 点击「选课」加入课程，或点击「退课」退出

#### 签到打卡
1. 点击「考勤签到」
2. 选择课程和日期，输入座位位置
3. 点击签到（迟到会自动标记）

#### 请假申请
1. 点击「请假申请」
2. 选择关联课程、起止时间（不超过3天）
3. 填写请假原因
4. 提交后等待教师审批（状态：待审批→已批准/已拒绝）

#### 移动签到
1. 手机浏览器访问 `http://服务器地址:8081/mobile-checkin`
2. 适配移动端的签到页面，操作流程同上

### 8.4 考勤状态说明

| 状态 | 显示 | 出勤率权重 | 说明 |
|------|------|-----------|------|
| present | 正常 | 1.0 | 在上课时间前15分钟至上课后0分钟内签到 |
| late | 迟到 | 0.8 | 在上课后0~30分钟内签到 |
| absent | 缺勤 | 0.0 | 未签到或手动标记为缺勤 |

### 8.5 Excel 导入格式

**考勤导入 Excel 模板：**

| 学号 | 姓名 | 课程代码 | 课程名称 | 日期 | 状态 |
|------|------|---------|---------|------|------|
| 2024001 | 张三 | CS101 | 数据结构 | 2024-03-15 | 正常 |

**学生名单导入 Excel 模板：**

| 学号 | 姓名 | 性别 | 班级 | 联系方式 |
|------|------|------|------|---------|
| 2024001 | 张三 | 男 | 计科1班 | 13800000000 |

---

## 9. 测试报告

### 9.1 测试策略

本项目采用 **6维度测试覆盖策略**，涵盖 Controller、Service、数据库、异常、边界条件和性能测试。

### 9.2 测试范围

| 测试类别 | 用例数 | 优先级 | 说明 |
|----------|--------|--------|------|
| Controller 接口测试 | ~125 | P0-P1 | 覆盖全部9个Controller的HTTP端点 |
| Service 业务逻辑测试 | ~80 | P0-P2 | 覆盖7个Service核心方法 |
| 数据库操作测试 | ~22 | P1-P2 | CRUD、关联查询、事务验证 |
| 异常场景测试 | ~28 | P1-P3 | 认证、授权、输入验证、业务异常 |
| 边界条件测试 | ~40 | P2-P3 | 数值、时间、字符串、分页边界 |
| 性能测试 | ~38 | P3 | 大数据量读写、导出、并发 |

### 9.3 完整测试清单

详见项目根目录下的 **[测试检查清单.md](./测试检查清单.md)**，包含：
- 每条测试用例的详细描述、HTTP方法、路径、输入参数、预期结果
- 测试优先级矩阵（P0-P3）
- 测试环境要求和索引优化建议
- 全部用例均配有 ☐ 勾选框，便于逐项执行跟踪

### 9.4 运行测试

```bash
# 运行所有测试
mvnw test

# 运行指定测试类
mvnw test -Dtest=AuthControllerTest

# 跳过测试打包
mvnw clean package -DskipTests
```

---

## 📌 附录

### A. 关键业务规则汇总

| 规则 | 说明 |
|------|------|
| 签到窗口 | 上课前15分钟 ~ 上课后30分钟 |
| 迟到判定 | 超过上课时间但在30分钟内签到 |
| 迟到权重 | 出勤率统计时 late = 0.8 |
| 请假上限 | 单次请假 ≤ 3天 |
| 请假冲突 | 同一学生时段不可重叠 |
| 选课容量 | 学生数超过 maxStudents 时拒绝选课 |
| 文件上限 | Excel 导入最大 10MB |
| 数据隔离 | 教师仅可见自己课程的所有关联数据 |
| 密码加密 | BCrypt 编码 |

### B. 常见问题

**Q: 启动后数据库表未创建？**  
A: 确认 PostgreSQL 服务已启动，检查 `application.properties` 中数据库连接信息。

**Q: 登录后页面未正确跳转？**  
A: 检查浏览器是否启用了 Cookie（系统使用 Session 认证）。

**Q: 学生端看不到可选课程？**  
A: 确认已完善个人信息（学号等），教师已创建课程。

**Q: Excel 导入失败？**  
A: 确认文件格式为 `.xlsx` 或 `.xls`，文件大小 < 10MB，列名与模板一致。

---

> 📅 最后更新：2026-06-06  
> 👤 作者：王杰（42411061）
