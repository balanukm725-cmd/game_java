@echo off
set "JAVA_CMD="
set "JAVAC_CMD="
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
if not defined JAVA_CMD for /f "delims=" %%J in ('where java.exe 2^>nul') do if not defined JAVA_CMD set "JAVA_CMD=%%J"
if not defined JAVAC_CMD for /f "delims=" %%J in ('where javac.exe 2^>nul') do if not defined JAVAC_CMD set "JAVAC_CMD=%%J"
for /d %%J in ("%ProgramFiles%\Java\jdk-*" "%ProgramFiles%\Eclipse Adoptium\jdk-*" "%ProgramFiles%\Microsoft\jdk-*") do (
  if not defined JAVA_CMD if exist "%%~J\bin\java.exe" set "JAVA_CMD=%%~J\bin\java.exe"
  if not defined JAVAC_CMD if exist "%%~J\bin\javac.exe" set "JAVAC_CMD=%%~J\bin\javac.exe"
)
exit /b 0
