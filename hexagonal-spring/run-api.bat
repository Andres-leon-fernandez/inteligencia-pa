@echo off
setlocal
cd /d "%~dp0"
echo Iniciando Inteligencia PA en modo desarrollo con Spring Boot DevTools...
call mvnw.cmd spring-boot:run
set "EXIT_CODE=%ERRORLEVEL%"
if not "%EXIT_CODE%"=="0" (
    echo La aplicacion termino con codigo %EXIT_CODE%.
    pause
)
exit /b %EXIT_CODE%
