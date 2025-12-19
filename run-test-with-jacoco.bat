@echo off
REM Script para ejecutar Maven con JAVA_HOME 25
REM Este script ejecuta: mvnw.cmd clean test jacoco:report

setlocal enabledelayedexpansion

REM Configurar JAVA_HOME
set "JAVA_HOME=C:\Program Files\Java\jdk-25"
echo [INFO] JAVA_HOME configurado a: %JAVA_HOME%
echo.

REM Ejecutar el comando
echo [INFO] Ejecutando: mvnw.cmd clean test jacoco:report
echo.
call mvnw.cmd clean test jacoco:report

REM Capturar el resultado
set "RESULT=%ERRORLEVEL%"

echo.
echo ========================================================================
if %RESULT% equ 0 (
    echo [SUCCESS] Compilacion completada exitosamente
    echo [INFO] Resultado: BUILD SUCCESS
) else (
    echo [ERROR] Compilacion fallida
    echo [INFO] Resultado: BUILD FAILURE (Code: %RESULT%)
)
echo ========================================================================

endlocal
exit /b %RESULT%
