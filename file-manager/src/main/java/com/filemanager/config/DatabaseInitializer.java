package com.filemanager.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

/**
 * SQLite数据库初始化器
 * 启动时自动执行schema.sql创建表结构
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final DataSource dataSource;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        // 检查表是否存在
        try (Connection conn = dataSource.getConnection()) {
            var metaData = conn.getMetaData();
            var tables = metaData.getTables(null, null, "USERS", null);

            if (!tables.next()) {
                // 表不存在，执行初始化SQL
                executeSqlFile(conn);
                System.out.println("[INFO] 数据库初始化完成");
            } else {
                System.out.println("[INFO] 数据库已存在，跳过初始化");
            }
        }
    }

    private void executeSqlFile(Connection conn) throws Exception {
        // 确保db目录存在
        Files.createDirectories(Paths.get("db"));

        // 读取classpath下的schema.sql
        var resource = new ClassPathResource("schema.sql");
        if (resource.exists()) {
            try (InputStream is = resource.getInputStream();
                 var reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                 Statement stmt = conn.createStatement()) {

                StringBuilder sql = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    // 跳过空行和注释
                    if (line.isEmpty() || line.startsWith("--")) {
                        continue;
                    }
                    sql.append(line).append(" ");

                    // 遇到分号执行
                    if (line.endsWith(";")) {
                        stmt.execute(sql.toString());
                        sql.setLength(0);
                    }
                }
            }
        }
    }
}