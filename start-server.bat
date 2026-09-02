@echo off
title HostelHub - Worldwide Server Tunnel
echo ==============================================================================
echo             HOSTELHUB WORLDWIDE SERVER ^& PUBLIC HTTPS TUNNEL
echo ==============================================================================
echo.
echo This tool makes your HostelHub backend accessible from ANYWHERE IN THE WORLD:
echo   * Works across all Wi-Fi networks and 4G/5G mobile cellular data.
echo   * No router port forwarding or firewall configuration needed.
echo   * Gives you a secure Public HTTPS URL to enter in the Android App!
echo.
echo ==============================================================================
echo.

cd /d "%~dp0backend"

echo [1/2] Building backend TypeScript...
call npm.cmd run build

echo.
echo [2/2] Starting Backend Server and Worldwide HTTPS Tunnel...
echo.
echo ------------------------------------------------------------------------------
echo HOW TO CONNECT FROM YOUR PHONE (ANY NETWORK / 4G / 5G / OTHER WI-FI):
echo   1. Copy the public https://... URL shown below (from localtunnel).
echo   2. Open the HostelHub Android App.
echo   3. Tap the "Server IP" button at top-right of the login screen.
echo   4. Paste your public URL and tap "Save & Apply".
echo ------------------------------------------------------------------------------
echo.

call npm.cmd run dev:tunnel
pause
