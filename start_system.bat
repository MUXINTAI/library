@echo off
echo ========================================
echo 图书管理系统启动脚本
echo ========================================
echo.

echo 正在获取本机IP地址...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /i "IPv4"') do (
    for /f "tokens=1" %%b in ("%%a") do (
        set LOCAL_IP=%%b
    )
)

echo 本机IP地址: %LOCAL_IP%
echo.
echo 其他电脑可以通过以下地址访问:
echo http://%LOCAL_IP%:5173/
echo.
echo ========================================
echo 正在启动服务...
echo ========================================
echo.

echo 1. 启动后端服务 (端口 8080)...
start "后端服务" cmd /k "cd /d %~dp0 && java -jar target\library-0.0.1-SNAPSHOT.jar"

echo 等待后端服务启动...
timeout /t 10 /nobreak > nul

echo.
echo 2. 启动前端服务 (端口 5173)...
start "前端服务" cmd /k "cd /d %~dp0library-ui && npm run dev"

echo.
echo ========================================
echo 服务启动完成！
echo ========================================
echo 本地访问: http://localhost:5173/
echo 网络访问: http://%LOCAL_IP%:5173/
echo.
echo 管理员账号: admin / admin123
echo 读者账号: reader / reader123
echo ========================================
echo.
pause 