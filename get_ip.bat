@echo off
echo ========================================
echo 图书管理系统 - 网络访问配置
echo ========================================
echo.
echo 正在获取本机IP地址...
echo.

for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /i "IPv4"') do (
    for /f "tokens=1" %%b in ("%%a") do (
        echo 本机IP地址: %%b
        echo 其他电脑可以通过以下地址访问:
        echo http://%%b:5173/
        echo.
    )
)

echo ========================================
echo 注意事项:
echo 1. 确保防火墙允许5173端口通过
echo 2. 确保所有设备在同一局域网内
echo 3. 如果仍无法访问，请检查路由器设置
echo ========================================
echo.
pause 