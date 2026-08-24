@echo off
title HostelHub - Worldwide Public Tunnel
echo ==============================================================================
echo                  HOSTELHUB WORLDWIDE PUBLIC HTTPS TUNNEL
echo ==============================================================================
echo.
echo Forwarding local port 5000 to the global internet...
echo Works when backend server is already running.
echo.
cd /d "%~dp0backend"
call npx.cmd lt --port 5000
pause
