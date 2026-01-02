#!/bin/bash
# Build and Restart Script for Travel Info Feature
# Created: January 1, 2026

echo "========================================="
echo "Building Moments Manager with Travel Info"
echo "========================================="
echo ""

# Navigate to project directory
cd /home/anilhemnani/moments-manager

# Kill existing application
echo "1. Stopping existing application..."
if [ -f app.pid ]; then
    PID=$(cat app.pid)
    if ps -p $PID > /dev/null 2>&1; then
        echo "   Killing process $PID"
        kill $PID
        sleep 2
        # Force kill if still running
        if ps -p $PID > /dev/null 2>&1; then
            kill -9 $PID
        fi
    fi
    rm -f app.pid
fi

# Also kill any process on port 8080
echo "   Checking port 8080..."
lsof -ti:8080 | xargs kill -9 2>/dev/null || true

echo "   ✓ Application stopped"
echo ""

# Clean and build
echo "2. Building application..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "   ✗ Build failed!"
    exit 1
fi

echo "   ✓ Build successful"
echo ""

# Start application
echo "3. Starting application..."
nohup java -jar target/moments-manager-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
APP_PID=$!
echo $APP_PID > app.pid

echo "   ✓ Application started with PID: $APP_PID"
echo ""

# Wait for startup
echo "4. Waiting for application to start..."
for i in {1..30}; do
    if curl -s http://localhost:8080 > /dev/null 2>&1; then
        echo "   ✓ Application is ready!"
        echo ""
        echo "========================================="
        echo "SUCCESS!"
        echo "========================================="
        echo ""
        echo "Application URL: http://localhost:8080"
        echo "Log file: app.log"
        echo "PID file: app.pid"
        echo ""
        echo "New Features Available:"
        echo "  - Travel Information Management"
        echo "  - Navigate to: Guest → RSVP → Attendees → Manage Travel"
        echo ""
        exit 0
    fi
    echo -n "."
    sleep 2
done

echo ""
echo "   ⚠ Application may still be starting..."
echo "   Check logs: tail -f app.log"
echo ""

