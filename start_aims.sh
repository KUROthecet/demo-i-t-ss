#!/bin/bash

echo "======================================================="
echo "AIMS Shop - One-Click Start Script"
echo "======================================================="
echo

if [ -f .env ]; then
    set -a
    source .env
    set +a
else
    echo "ERROR: .env not found."
    echo "  Copy .env.example to .env and fill in real values first."
    exit 1
fi

echo "[1/3] Starting Infrastructure (PostgreSQL, Redis)..."
if docker compose version &>/dev/null 2>&1; then
    docker compose up -d
elif command -v docker-compose &>/dev/null; then
    docker-compose up -d
else
    echo "ERROR: docker-compose is not installed."
    echo "  Run: sudo pacman -S docker-compose"
    exit 1
fi

echo
echo "[2/3] Starting Backend API (Spring Boot)..."
fuser -k 8080/tcp 2>/dev/null || true
(cd backend && chmod +x mvnw && ./mvnw spring-boot:run) &

echo
echo "[3/3] Starting Frontend (Angular)..."
(cd frontend && echo "Running npm install..." && npm install && echo "Starting Angular server..." && npm start) &

echo
echo "======================================================="
echo "Startup initiated successfully!"
echo "Please wait a moment for the services to boot up."
echo
echo "- Frontend: http://localhost:4200"
echo "- Backend:  http://localhost:8080/api"
echo "- Database: localhost:5435"
echo
echo "Press Ctrl+C to stop all services."
echo "======================================================="

wait
