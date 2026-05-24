@echo off
:: Maven Wrapper Proxy Script for AIMS Shop

:: Check if global mvn is available
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    mvn %*
    exit /b %errorlevel%
)

:: Check common IntelliJ IDEA paths
set INT_MVN="C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.3.3\plugins\maven\lib\maven3\bin\mvn.cmd"
if exist %INT_MVN% (
    %INT_MVN% %*
    exit /b %errorlevel%
)

:: Try generic JetBrains search
for /d %%d in ("C:\Program Files\JetBrains\IntelliJ IDEA*") do (
    if exist "%%d\plugins\maven\lib\maven3\bin\mvn.cmd" (
        "%%d\plugins\maven\lib\maven3\bin\mvn.cmd" %*
        exit /b %errorlevel%
    )
)

echo [ERROR] Maven (mvn) was not found on your system.
echo Please install Maven or install IntelliJ IDEA Community.
exit /b 1
