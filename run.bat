@echo off
REM =============================================================================
REM Hostel Management System - Execution Script (Windows)
REM =============================================================================

echo ==========================================================
echo     Compiling Hostel Management System...
echo ==========================================================

if not exist bin mkdir bin

javac -cp "lib/*;src" -d bin src/database/*.java src/model/*.java src/dao/*.java src/ui/*.java src/Main.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed! Please check JDK installation and classpath.
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful!
echo ----------------------------------------------------------
echo Starting Application...
echo ==========================================================

java -cp "bin;lib/*" Main
pause
