@echo off
setlocal
cd /d "%~dp0"
call find-java.cmd
if not defined JAVAC_CMD exit /b 1
for %%J in ("%JAVAC_CMD%") do set "JAVA_BIN=%%~dpJ"
if not exist test-build mkdir test-build
"%JAVAC_CMD%" --release 17 -encoding UTF-8 -Xlint:all -d test-build src\neonbreaker\*.java tests\neonbreaker\*.java
if errorlevel 1 exit /b 1
"%JAVA_BIN%java.exe" -Djava.awt.headless=true -cp test-build neonbreaker.GameEngineTest
if errorlevel 1 exit /b 1
