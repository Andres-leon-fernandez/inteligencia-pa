@echo off
setlocal
cd /d "%~dp0"
set "APP_JAR=dist\inteligencia-pa-api.jar"
if exist "target\inteligencia-pa-api-0.0.1-SNAPSHOT.jar" set "APP_JAR=target\inteligencia-pa-api-0.0.1-SNAPSHOT.jar"
if not exist "%APP_JAR%" (
    echo No encuentro el JAR ejecutable.
    echo Construyelo con: mvnw.cmd -DskipTests package
    pause
    exit /b 1
)
java -jar "%APP_JAR%"
if errorlevel 1 pause
