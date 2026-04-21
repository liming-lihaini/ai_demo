-- 文件管理系统数据库表结构
-- 版本: 1.0.0
-- 日期: 2026-04-16

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME
);

-- 目录表
CREATE TABLE IF NOT EXISTS directories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    parent_id INTEGER REFERENCES directories(id),
    user_id INTEGER NOT NULL REFERENCES users(id),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 文件表
CREATE TABLE IF NOT EXISTS files (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    format TEXT NOT NULL,
    size INTEGER NOT NULL,
    md5 TEXT NOT NULL,
    path TEXT NOT NULL,
    parent_id INTEGER REFERENCES directories(id),
    user_id INTEGER NOT NULL REFERENCES users(id),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    type TEXT NOT NULL,
    content TEXT NOT NULL,
    user_id INTEGER REFERENCES users(id),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- MD文件内容表
CREATE TABLE IF NOT EXISTS md_contents (
    file_id INTEGER PRIMARY KEY REFERENCES files(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 索引创建
CREATE INDEX IF NOT EXISTS idx_directories_parent_id ON directories(parent_id);
CREATE INDEX IF NOT EXISTS idx_directories_user_id ON directories(user_id);
CREATE INDEX IF NOT EXISTS idx_directories_deleted ON directories(deleted);

CREATE INDEX IF NOT EXISTS idx_files_parent_id ON files(parent_id);
CREATE INDEX IF NOT EXISTS idx_files_user_id ON files(user_id);
CREATE INDEX IF NOT EXISTS idx_files_md5 ON files(md5);
CREATE INDEX IF NOT EXISTS idx_files_deleted ON files(deleted);

CREATE INDEX IF NOT EXISTS idx_operation_logs_user_id ON operation_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_operation_logs_created_at ON operation_logs(created_at);

-- 唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS idx_username ON users(username);
CREATE UNIQUE INDEX IF NOT EXISTS idx_directory_name_parent ON directories(name, parent_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_file_name_parent ON files(name, parent_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_file_md5_parent ON files(md5, parent_id);


ALTER TABLE files ADD COLUMN delete_at TIMESTAMP;
ALTER TABLE directories ADD COLUMN delete_at TIMESTAMP;


-- 文件全文检索虚拟表
CREATE VIRTUAL TABLE IF NOT EXISTS file_fts USING fts5(
    name,
    content,
    content='files',
    content_rowid='id'
);

-- 目录全文检索虚拟表
CREATE VIRTUAL TABLE IF NOT EXISTS directory_fts USING fts5(
    name,
    content='directories',
    content_rowid='id'
);


-- 文件 INSERT 触发器
CREATE TRIGGER IF NOT EXISTS file_ai AFTER INSERT ON files BEGIN
    INSERT INTO file_fts(rowid, name, content)
    VALUES (new.id, new.name, '');
END;

-- 文件 DELETE 触发器
CREATE TRIGGER IF NOT EXISTS file_ad AFTER DELETE ON files BEGIN
    INSERT INTO file_fts(file_fts, rowid, name, content)
    VALUES('delete', old.id, old.name, '');
END;

-- 文件 UPDATE 触发器
CREATE TRIGGER IF NOT EXISTS file_au AFTER UPDATE ON files BEGIN
    INSERT INTO file_fts(file_fts, rowid, name, content)
    VALUES('delete', old.id, old.name, '');
    INSERT INTO file_fts(rowid, name, content)
    VALUES (new.id, new.name, '');
END;

-- 目录 INSERT 触发器
CREATE TRIGGER IF NOT EXISTS directory_ai AFTER INSERT ON directories BEGIN
    INSERT INTO directory_fts(rowid, name)
    VALUES (new.id, new.name);
END;

-- 目录 DELETE 触发器
CREATE TRIGGER IF NOT EXISTS directory_ad AFTER DELETE ON directories BEGIN
    INSERT INTO directory_fts(directory_fts, rowid, name)
    VALUES('delete', old.id, old.name);
END;

-- 目录 UPDATE 触发器
CREATE TRIGGER IF NOT EXISTS directory_au AFTER UPDATE ON directories BEGIN
    INSERT INTO directory_fts(directory_fts, rowid, name)
    VALUES('delete', old.id, old.name);
    INSERT INTO directory_fts(rowid, name)
    VALUES (new.id, new.name);
END;