package com.example.attendancesystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testConnection() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("✅ PostgreSQL 连接成功！");
            System.out.println("数据库 URL: " + conn.getMetaData().getURL());
        }
    }
}
