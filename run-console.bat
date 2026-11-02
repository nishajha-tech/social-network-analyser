@echo off
echo ============================================================
echo   Compiling and Starting Social Network Analyser (CLI Mode)
echo ============================================================

if not exist bin mkdir bin

javac -d bin src/backend/*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)

java -cp bin backend.ConsoleApp
pause
