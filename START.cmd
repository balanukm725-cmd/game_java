@echo off
setlocal
cd /d "%~dp0"
call find-java.cmd
if not defined JAVA_CMD (
  echo Java 17 or newer is required. Install a JDK and run again.
  pause
  exit /b 1
)
"%JAVA_CMD%" -jar NeonBreaker.jar
if errorlevel 1 pause
