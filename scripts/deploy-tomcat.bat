@echo off
setlocal EnableExtensions DisableDelayedExpansion

if "%~1"=="" (
    echo ERROR: Deployment environment is required. Use dev, test, or prod.
    exit /b 2
)

if /I "%~1"=="dev" (
    set "DEPLOY_ENV=dev"
) else if /I "%~1"=="test" (
    set "DEPLOY_ENV=test"
) else if /I "%~1"=="prod" (
    set "DEPLOY_ENV=prod"
) else (
    echo ERROR: Invalid environment "%~1". Use dev, test, or prod.
    exit /b 2
)

if not defined CATALINA_BASE set "CATALINA_BASE=C:\apache-tomcat-10.1.61"
if not exist "%CATALINA_BASE%\bin\" (
    echo ERROR: Tomcat bin directory does not exist: "%CATALINA_BASE%\bin"
    exit /b 2
)
if not defined TOMCAT_USERNAME (
    echo ERROR: TOMCAT_USERNAME was not supplied by the Jenkins tomcat-manager credential.
    exit /b 2
)
if not defined TOMCAT_PASSWORD (
    echo ERROR: TOMCAT_PASSWORD was not supplied by the Jenkins tomcat-manager credential.
    exit /b 2
)

if not defined TOMCAT_SERVICE_NAME set "TOMCAT_SERVICE_NAME=Tomcat10"

for %%I in ("%~dp0..") do set "PROJECT_ROOT=%%~fI"
set "WAR_FILE=%PROJECT_ROOT%\target\crop-advisory.war"
set "DEPLOY_RESPONSE_FILE=%PROJECT_ROOT%\target\tomcat-deploy-response.txt"
set "SETENV_FILE=%CATALINA_BASE%\bin\setenv.bat"
set "DB_DIRECTORY=C:\crop-advisory-data\%DEPLOY_ENV%"
set "HEALTH_URL=http://localhost:8081/crop-advisory/actuator/health"

if not exist "%WAR_FILE%" (
    echo ERROR: Packaged WAR not found: "%WAR_FILE%"
    exit /b 2
)

if not exist "%DB_DIRECTORY%\" mkdir "%DB_DIRECTORY%"
if errorlevel 1 (
    echo ERROR: Could not create database directory "%DB_DIRECTORY%".
    exit /b 2
)

>"%SETENV_FILE%" echo @echo off
if errorlevel 1 (
    echo ERROR: Could not write "%SETENV_FILE%". Check Jenkins service account permissions.
    exit /b 2
)
>>"%SETENV_FILE%" echo set "APP_ENV=%DEPLOY_ENV%"
if errorlevel 1 (
    echo ERROR: Could not write APP_ENV to "%SETENV_FILE%".
    exit /b 2
)
>>"%SETENV_FILE%" echo set "DB_PATH=C:\crop-advisory-data\%DEPLOY_ENV%\cropdb"
if errorlevel 1 (
    echo ERROR: Could not write DB_PATH to "%SETENV_FILE%".
    exit /b 2
)

echo Deploying "%WAR_FILE%" to Tomcat for environment "%DEPLOY_ENV%".
curl.exe --silent --show-error --fail --connect-timeout 5 --max-time 120 --user "%TOMCAT_USERNAME%:%TOMCAT_PASSWORD%" --upload-file "%WAR_FILE%" --output "%DEPLOY_RESPONSE_FILE%" "http://localhost:8081/manager/text/deploy?path=/crop-advisory&update=true"
if errorlevel 1 (
    del /q "%DEPLOY_RESPONSE_FILE%" >nul 2>&1
    echo ERROR: Request to the Tomcat Manager deploy API failed.
    exit /b 1
)
findstr /B /C:"OK -" "%DEPLOY_RESPONSE_FILE%" >nul
if errorlevel 1 (
    del /q "%DEPLOY_RESPONSE_FILE%" >nul 2>&1
    echo ERROR: Tomcat Manager rejected the deployment. Check its response and server logs.
    exit /b 1
)
del /q "%DEPLOY_RESPONSE_FILE%" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Deployment succeeded but the response file could not be removed: "%DEPLOY_RESPONSE_FILE%"
    exit /b 1
)
echo Tomcat Manager deployment succeeded.

sc.exe query "%TOMCAT_SERVICE_NAME%" >nul 2>&1
if errorlevel 1 (
    echo ERROR: Tomcat Windows service "%TOMCAT_SERVICE_NAME%" was not found.
    exit /b 2
)

sc.exe query "%TOMCAT_SERVICE_NAME%" | findstr /I /C:"RUNNING" >nul
if not errorlevel 1 (
    echo Stopping Tomcat service "%TOMCAT_SERVICE_NAME%".
    net.exe stop "%TOMCAT_SERVICE_NAME%" >nul
    if errorlevel 1 (
        echo ERROR: Could not stop Tomcat service "%TOMCAT_SERVICE_NAME%".
        exit /b 1
    )
)

echo Starting Tomcat service "%TOMCAT_SERVICE_NAME%".
net.exe start "%TOMCAT_SERVICE_NAME%" >nul
if errorlevel 1 (
    echo ERROR: Could not start Tomcat service "%TOMCAT_SERVICE_NAME%".
    exit /b 1
)

echo Checking application health at %HEALTH_URL%.
set "HEALTH_ATTEMPT=1"
:health_check
curl.exe --fail --silent --show-error --connect-timeout 3 --max-time 5 "%HEALTH_URL%" >nul
if not errorlevel 1 goto health_succeeded
if %HEALTH_ATTEMPT% GEQ 30 goto health_failed
echo Health check attempt %HEALTH_ATTEMPT% of 30 failed; retrying in 5 seconds.
timeout.exe /t 5 /nobreak >nul
set /a HEALTH_ATTEMPT+=1
goto health_check

:health_failed
echo ERROR: Application health check failed after 30 attempts: %HEALTH_URL%
exit /b 1

:health_succeeded
echo Application is healthy at %HEALTH_URL%.
exit /b 0
