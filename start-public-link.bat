@echo off
echo ============================================================
echo   Starting Social Network Analyser Public Tunnel
echo ============================================================

cd /d "%~dp0"

echo 1. Ensuring Java server is running...
start /b "" java -cp bin backend.Main

timeout /t 3 >nul

echo 2. Launching Cloudflare Public Tunnel...
echo Look for the https://....trycloudflare.com link below:
echo ============================================================
..\cloudflared.exe tunnel --url http://localhost:8080
pause
