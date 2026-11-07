@echo off
setlocal
cd /d "%~dp0"
call find-java.cmd
if not defined JAVAC_CMD (
  echo JDK 17 or newer is required. Set JAVA_HOME to your JDK folder.
  pause
  exit /b 1
)
for %%J in ("%JAVAC_CMD%") do set "JAVA_BIN=%%~dpJ"
if not exist build mkdir build
"%JAVAC_CMD%" --release 17 -encoding UTF-8 -Xlint:all -d build src\neonbreaker\*.java
if errorlevel 1 exit /b 1
"%JAVA_BIN%jar.exe" "-J-Djava.io.tmpdir=%CD%\build" --create --file NeonBreaker.jar --main-class neonbreaker.Main -C build neonbreaker
if errorlevel 1 exit /b 1
echo Build successful: NeonBreaker.jar
