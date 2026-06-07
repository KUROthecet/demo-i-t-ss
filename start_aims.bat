@echo off
echo =======================================================
echo AIMS Shop - One-Click Start Script
echo =======================================================
echo.

echo [0/3] Checking Docker Desktop...
docker info >nul 2>&1
if errorlevel 1 (
  echo.
  echo  ERROR: Docker Desktop is not running.
  echo  Please start Docker Desktop first, then run this script again.
  echo.
  pause
  exit /b 1
)
echo  Docker is running. OK.
echo.

echo [1/3] Starting Infrastructure (PostgreSQL + Redis)...
docker-compose up -d
if errorlevel 1 (
  echo.
  echo  ERROR: docker-compose failed. Check docker-compose.yml.
  echo.
  pause
  exit /b 1
)
echo.
echo  Waiting 20 seconds for PostgreSQL and Redis to fully initialize...
timeout /t 20 /nobreak >nul
echo  Infrastructure ready.
echo.

echo [2/3] Starting Backend API (Spring Boot)...
start "AIMS Backend" cmd /k "cd backend && .\mvnw.cmd spring-boot:run"
echo.

echo [3/3] Starting Frontend (Angular)...
start "AIMS Frontend" cmd /k "cd frontend && echo Running npm install... && npm install && echo Starting Angular dev server... && npm start"

echo.
echo =======================================================
echo Startup initiated successfully!
echo.
echo  - Frontend: http://localhost:4200
echo  - Backend:  http://localhost:8080/api
echo  - Database: localhost:5435
echo.
echo  Backend takes ~30 seconds to start.
echo  You can close this window. The backend and frontend
echo  are running in their own terminal windows.
echo =======================================================
pause
