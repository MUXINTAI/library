@echo off
echo ================================================
echo 图书管理系统 - 数据库创建脚本
echo ================================================

echo.
echo 正在创建数据库 library_db...
echo.

REM 尝试不同的MySQL路径
set MYSQL_PATH=""

REM 检查常见的MySQL安装路径
if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    echo 找到MySQL: %MYSQL_PATH%
) else if exist "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe" (
    set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe"
    echo 找到MySQL: %MYSQL_PATH%
) else if exist "C:\xampp\mysql\bin\mysql.exe" (
    set MYSQL_PATH="C:\xampp\mysql\bin\mysql.exe"
    echo 找到MySQL (XAMPP): %MYSQL_PATH%
) else (
    echo 未找到MySQL安装路径，请手动执行以下步骤：
    echo.
    echo 1. 打开MySQL Workbench或命令行工具
    echo 2. 连接到MySQL服务器 (用户名: root, 密码: 7654321)
    echo 3. 执行以下SQL命令：
    echo    CREATE DATABASE library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    echo.
    echo 或者导入 create_database.sql 文件
    echo.
    pause
    exit /b 1
)

REM 执行SQL脚本创建数据库
echo 请输入MySQL密码 (当前配置: 7654321):
%MYSQL_PATH% -u root -p < create_database.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ================================================
    echo 数据库创建成功！
    echo ================================================
    echo.
    echo 现在可以运行以下命令初始化数据表：
    echo init_database.bat
    echo.
) else (
    echo.
    echo ================================================
    echo 数据库创建失败！
    echo ================================================
    echo.
    echo 请检查：
    echo 1. MySQL服务是否启动
    echo 2. 用户名和密码是否正确
    echo 3. 或者手动创建数据库
    echo.
)

pause 