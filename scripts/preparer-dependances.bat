@echo off
REM [Benjamin - #05, 2026-10-15] Telecharge toutes les dependances Maven du backend dans %USERPROFILE%\.m2
REM (utile pour travailler hors ligne et pour la verification automatisee).
if not defined JAVA_HOME set "JAVA_HOME=%USERPROFILE%\.jdks\ms-21.0.8"
set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d %~dp0..\backend
call mvnw.cmd -B -q dependency:go-offline || goto :err
call mvnw.cmd -B -q dependency:get -Dartifact=org.apache.maven.surefire:surefire-junit-platform:3.5.5 || goto :err
call mvnw.cmd -B -q dependency:get -Dartifact=org.apache.maven.surefire:common-java5:3.5.5 || goto :err
call mvnw.cmd -B -q dependency:get -Dartifact=org.apache.maven.plugins:maven-jar-plugin:3.4.2 || goto :err
echo.
echo === Dependances telechargees avec succes ===
pause
exit /b 0
:err
echo.
echo === ERREUR pendant le telechargement : verifier la connexion et relancer le script ===
pause
exit /b 1
