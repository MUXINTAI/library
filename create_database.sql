-- 创建图书管理系统数据库
-- 请将此文件导入到MySQL中执行

-- 创建数据库
CREATE DATABASE IF NOT EXISTS library_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE library_db;

-- 显示创建结果
SELECT 'Database library_db created successfully!' as Result;

-- 显示所有数据库
SHOW DATABASES; 