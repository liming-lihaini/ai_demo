package com.filemanager.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.sqlite.SQLiteException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * SQLite数据库初始化器
 * 启动时自动创建表结构
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final DataSource dataSource;

    public DatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            // 检查 users 表是否存在
            var metaData = conn.getMetaData();
            var tables = metaData.getTables(null, null, "USERS", null);
            boolean tableExists = tables.next();

            if (!tableExists) {
                // 表不存在，执行完整初始化
                executeFullInit(conn);
                System.out.println("[INFO] 数据库初始化完成");
            } else {
                // 表存在，检查 FTS 表
                var fileFts = metaData.getTables(null, null, "FILE_FTS", null);
                boolean fileFtsExists = fileFts.next();

                var dirFts = metaData.getTables(null, null, "DIRECTORY_FTS", null);
                boolean dirFtsExists = dirFts.next();

                if (!fileFtsExists || !dirFtsExists) {
                    executeFtsUpdate(conn);
                    System.out.println("[INFO] FTS表更新完成");
                } else {
                    System.out.println("[INFO] 数据库已存在，跳过初始化");
                }
            }
        }
    }

    private void executeFullInit(Connection conn) throws Exception {
        System.out.println("[INFO] 开始初始化数据库...");
        try (var stmt = conn.createStatement()) {
            // 用户表
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "last_login DATETIME)");

            // 目录表
            stmt.execute("CREATE TABLE IF NOT EXISTS directories (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "parent_id INTEGER REFERENCES directories(id), " +
                "user_id INTEGER NOT NULL REFERENCES users(id), " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "deleted INTEGER DEFAULT 0, " +
                "delete_at TIMESTAMP)");

            // 文件表
            stmt.execute("CREATE TABLE IF NOT EXISTS files (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "format TEXT NOT NULL, " +
                "size INTEGER NOT NULL, " +
                "md5 TEXT NOT NULL, " +
                "path TEXT NOT NULL, " +
                "parent_id INTEGER REFERENCES directories(id), " +
                "user_id INTEGER NOT NULL REFERENCES users(id), " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "deleted INTEGER DEFAULT 0, " +
                "delete_at TIMESTAMP)");

            // 操作日志表
            stmt.execute("CREATE TABLE IF NOT EXISTS operation_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "user_id INTEGER REFERENCES users(id), " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            // MD文件内容表
            stmt.execute("CREATE TABLE IF NOT EXISTS md_contents (" +
                "file_id INTEGER PRIMARY KEY REFERENCES files(id) ON DELETE CASCADE, " +
                "content TEXT NOT NULL, " +
                "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            // 索引
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_directories_parent_id ON directories(parent_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_directories_user_id ON directories(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_directories_deleted ON directories(deleted)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_files_parent_id ON files(parent_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_files_user_id ON files(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_files_md5 ON files(md5)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_files_deleted ON files(deleted)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_operation_logs_user_id ON operation_logs(user_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_operation_logs_created_at ON operation_logs(created_at)");

            // 唯一索引
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_username ON users(username)");
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_directory_name_parent ON directories(name, parent_id)");
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_file_name_parent ON files(name, parent_id)");
            stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_file_md5_parent ON files(md5, parent_id)");

            // FTS5 虚拟表
            stmt.execute("CREATE VIRTUAL TABLE IF NOT EXISTS file_fts USING fts5(name, content)");
            stmt.execute("CREATE VIRTUAL TABLE IF NOT EXISTS directory_fts USING fts5(name)");

            System.out.println("[INFO] 数据库表创建完成");
        }
    }

    private void executeFtsUpdate(Connection conn) throws Exception {
        System.out.println("[INFO] 开始执行FTS表更新...");
        try (var stmt = conn.createStatement()) {
            // 创建FTS虚拟表
            stmt.execute("CREATE VIRTUAL TABLE IF NOT EXISTS file_fts USING fts5(name, content)");
            stmt.execute("CREATE VIRTUAL TABLE IF NOT EXISTS directory_fts USING fts5(name)");

            // 触发器通过 SearchIndexService 定时同步
            System.out.println("[INFO] 跳过触发器创建（由 SearchIndexService 定时同步）");

            System.out.println("[INFO] FTS表更新完成");
        } catch (Exception e) {
            System.out.println("[ERROR] FTS表更新异常: " + e.getMessage());
        }
    }
}
