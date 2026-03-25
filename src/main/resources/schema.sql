-- 学生表
CREATE TABLE IF NOT EXISTS student (
                                       student_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    class_name VARCHAR(50)
    );

-- 如果还需要其他表，例如考勤记录表，可以继续添加
-- CREATE TABLE IF NOT EXISTS attendance ( ... );