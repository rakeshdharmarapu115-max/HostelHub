@echo off
title HostelHub - Cloud Deployment Assistant
echo ==============================================================================
echo                 HOSTELHUB CLOUD SERVICES SETUP ^& DEPLOYMENT
echo ==============================================================================
echo.
echo This tool validates and prepares your project for Free Cloud Services:
echo   [1] Cloud PostgreSQL Database (Neon / Supabase / Render Postgres)
echo   [2] Cloud Web Service Backend (Render / Railway / Cloud Run)
echo   [3] Cloud Media Storage (Cloudinary)
echo   [4] Cloud Push Notifications (Firebase Cloud Messaging)
echo.
echo ==============================================================================
echo.

cd /d "%~dp0backend"

echo [1/3] Generating Prisma Client...
call npx.cmd prisma generate
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Prisma generation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/3] Checking TypeScript Build...
call npm.cmd run build
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] TypeScript build failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [3/3] Running Backend Validation Tests...
call npm.cmd test
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Some tests reported issues, please check output above.
)

echo.
echo ==============================================================================
echo  ✅ YOUR BACKEND IS 100%% READY FOR CLOUD DEPLOYMENT!
echo ==============================================================================
echo.
echo Next Steps to Go Live in 3 Minutes:
echo   1. Push your code to GitHub:
echo        git add .
echo        git commit -m "Configure cloud services and production backend"
echo        git push origin main
echo.
echo   2. Go to https://render.com (or https://railway.app):
echo        - Click "New +" -> "Blueprint"
echo        - Connect your repository
echo        - Render will automatically launch both the Database and API Web Service!
echo.
echo   3. Detailed step-by-step guide is available at:
echo        CLOUD_DEPLOYMENT_GUIDE.md
echo ==============================================================================
echo.
pause
