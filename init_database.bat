@echo off
echo 正在初始化图书管理系统数据库...

REM 设置MySQL路径（请根据实际安装路径修改）
set MYSQL_PATH="C:\Program Files\MySQL\MySQL Server 8.0\bin"
set MYSQL_PATH2="C:\MySQL\bin"
set MYSQL_PATH3="C:\xampp\mysql\bin"

REM 数据库连接信息
set DB_HOST=localhost
set DB_PORT=3306
set DB_USER=root
set DB_PASSWORD=woaimx.0

echo 尝试连接MySQL数据库...

REM 尝试不同的MySQL路径
if exist %MYSQL_PATH%\mysql.exe (
    set MYSQL_CMD=%MYSQL_PATH%\mysql.exe
    echo 找到MySQL: %MYSQL_PATH%
    goto run_init
)

if exist %MYSQL_PATH2%\mysql.exe (
    set MYSQL_CMD=%MYSQL_PATH2%\mysql.exe
    echo 找到MySQL: %MYSQL_PATH2%
    goto run_init
)

if exist %MYSQL_PATH3%\mysql.exe (
    set MYSQL_CMD=%MYSQL_PATH3%\mysql.exe
    echo 找到MySQL: %MYSQL_PATH3%
    goto run_init
)

REM 尝试系统PATH中的mysql
mysql --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set MYSQL_CMD=mysql
    echo 使用系统PATH中的MySQL
    goto run_init
)

echo 错误：未找到MySQL客户端
echo 请确保MySQL已正确安装，或修改脚本中的MYSQL_PATH路径
pause
exit /b 1

:run_init
echo 开始执行数据库初始化脚本...
echo 连接信息: %DB_USER%@%DB_HOST%:%DB_PORT%

REM 执行数据库初始化脚本
%MYSQL_CMD% -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% < database_init.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================
    echo 数据库初始化成功！
    echo ===================================
    echo 默认管理员账户：admin / admin123
    echo 示例读者账户：reader1, reader2, reader3 / 123456
    echo ===================================
) else (
    echo.
    echo 数据库初始化失败！
    echo 请检查：
    echo 1. MySQL服务是否启动
    echo 2. 数据库连接信息是否正确
    echo 3. 是否有足够的权限
)

echo.
echo 按任意键继续...
pause >nul 