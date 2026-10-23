@echo off
title AirShare Spring Boot Backend
echo ===================================================
echo   AirShare: Airport Mobility Dispatch Engine
echo   Starting Spring Boot 3 + PostGIS Backend...
echo ===================================================

set "MVN_PATH=C:\Users\DEEKSHITH\.m2\wrapper\dists\apache-maven-3.9.7-bin\3k9n615lchs6mp84v355m633uo\apache-maven-3.9.7\bin\mvn.cmd"

if exist "%MVN_PATH%" (
    call "%MVN_PATH%" spring-boot:run
) else (
    echo Maven wrapper not found at default location, trying system mvn...
    mvn spring-boot:run
)

pause
