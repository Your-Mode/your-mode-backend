#!/bin/bash

APP_NAME=your-mode-backend-0.0.1-SNAPSHOT.jar
DEPLOY_PATH=/home/ubuntu/app
LOG_PATH=/home/ubuntu/logs

mkdir -p $LOG_PATH

# 기존 실행 중인 프로세스 종료
PID=$(pgrep -f $APP_NAME)
if [ -n "$PID" ]; then
  echo "Stopping running app: $PID"
  kill -15 $PID
  sleep 5
fi

# 새로 실행
echo "Starting app..."
nohup java -jar $DEPLOY_PATH/$APP_NAME > $LOG_PATH/app.log 2>&1 &
