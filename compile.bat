@echo off
setlocal enabledelayedexpansion

REM Detectar la ubicación del JDK 25
for /f "tokens=*" %%i in ('where java') do set JAVA_BIN=%%i
if defined JAVA_BIN (
    for %%A in ("%JAVA_BIN%") do set JAVA_DIR=%%~dpA
    set JAVA_HOME=!JAVA_DIR:~0,-1!
    for %%A in ("!JAVA_HOME!") do set JAVA_HOME=%%~dpA
    set JAVA_HOME=!JAVA_HOME:~0,-1!
    echo JAVA_HOME set to: !JAVA_HOME!
)

REM Ejecutar Maven
echo Compilando proyecto con Spring Boot 4.0.0 y Java 25...
call .\mvnw.cmd clean compile

pause
