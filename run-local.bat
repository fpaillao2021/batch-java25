@echo off
setlocal

set JAVA_HOME=C:\Program Files\Java\jdk-25
set SPRING_PROFILES_ACTIVE=local

echo Starting Spring Boot application in LOCAL profile...
echo ================================================

call mvnw.cmd clean spring-boot:run -DskipTests

endlocal
pause
