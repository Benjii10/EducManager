@echo off
REM [Benjamin - #00, 2026-10-15] Lance le backend Spring Boot (H2) et le frontend Angular dans deux fenetres.
REM Utilise JAVA_HOME s'il est defini, sinon le JDK 21 installe par IntelliJ (%USERPROFILE%\.jdks\ms-21.0.8).
if not defined JAVA_HOME set "JAVA_HOME=%USERPROFILE%\.jdks\ms-21.0.8"
set "PATH=%JAVA_HOME%\bin;%PATH%"

where node >nul 2>nul || (echo Node.js introuvable : installe Node 20 LTS ou plus depuis https://nodejs.org & pause & exit /b 1)

start "EducManager - backend" cmd /k "cd /d %~dp0backend && mvnw.cmd spring-boot:run"

cd /d %~dp0frontend
if not exist node_modules (call npm install)
start "EducManager - frontend" cmd /k "cd /d %~dp0frontend && npm start"
