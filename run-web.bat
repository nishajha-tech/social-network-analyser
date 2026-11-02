@echo off
echo ============================================================
echo   Compiling and Starting Social Network Analyser (Web UI)
echo ============================================================

if not exist bin mkdir bin

javac -d bin src/backend/*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)

echo Starting Server...
start "" "http://localhost:8080"
java -cp bin backend.Main
pause
