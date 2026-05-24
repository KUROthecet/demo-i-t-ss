@echo off
echo =======================================================
echo AIMS Shop - One-Click Start Script
echo =======================================================
echo.

echo [1/3] Starting Infrastructure (PostgreSQL, Redis)...
docker-compose up -d

echo.
echo [2/3] Starting Backend API (Spring Boot)...
:: Opens a new terminal window for the backend
start "AIMS Backend" cmd /k "cd backend && .\mvnw.cmd spring-boot:run"

echo.
echo [3/3] Starting Frontend (Angular)...
:: Opens a new terminal window for the frontend
start "AIMS Frontend" cmd /k "cd frontend && echo Running npm install... && npm install && echo Starting Angular server... && npm start"

echo.
echo =======================================================
echo Startup initiated successfully!
echo Please wait a moment for the services to boot up.
echo.
echo - Frontend: http://localhost:4200
echo - Backend:  http://localhost:8080/api
echo - Database: localhost:5435
echo.
echo You can close this window now. The backend and frontend
echo are running in their own separate terminal windows.
echo =======================================================
pause
