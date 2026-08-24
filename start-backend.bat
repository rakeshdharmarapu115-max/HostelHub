@echo off
title HostelHub Backend Server
echo ===================================================
echo           STARTING HOSTELHUB BACKEND SERVER
echo ===================================================
echo.
echo Computer LAN IP: 192.168.1.2
echo Listening on: http://0.0.0.0:5000/api/
echo.
cd /d "%~dp0backend"
echo Installing any missing packages...
call npm.cmd install
echo.
echo Starting development server on port 5000...
call npm.cmd run dev
pause
