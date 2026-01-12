#!/bin/bash
cd /home/anilhemnani/wed-knots

echo "Stopping any existing application..."
pkill -9 -f "java.*wed-knots" 2>/dev/null || true

echo "Building application..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "Build successful! Starting application..."
    nohup java -jar target/wed-knots-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
    echo $! > app.pid

    echo "Application starting... PID: $(cat app.pid)"
    echo "Waiting 20 seconds for application to start..."
    sleep 20

    echo "Checking application status..."
    if grep -q "Started MomentsManagerApplication" app.log; then
        echo "✅ Application started successfully!"
        echo "Access at: http://localhost:8080/admin/dashboard"
    else
        echo "⚠️  Application may still be starting. Check app.log for details."
        tail -30 app.log
    fi
else
    echo "❌ Build failed!"
    exit 1
fi

