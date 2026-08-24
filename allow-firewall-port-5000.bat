@echo off
echo ===================================================
echo   HostelHub - Windows Firewall Port 5000 Opener
echo ===================================================
echo.
echo Please ensure you ran this file as Administrator (Right click -> Run as administrator).
echo.
netsh advfirewall firewall add rule name="HostelHub Backend (Port 5000)" dir=in action=allow protocol=TCP localport=5000
echo.
if %errorlevel% equ 0 (
    echo [SUCCESS] Port 5000 is now open for incoming connections from your phone!
) else (
    echo [NOTE] If it failed with elevation required, please Right Click -> Run as administrator.
)
echo.
pause
