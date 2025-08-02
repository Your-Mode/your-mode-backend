#!/bin/bash

# 1. 기존 스프링부트 앱 프로세스 종료
echo "Stopping existing Spring Boot application..."
pkill -f 'java -jar /home/ubuntu/app/app.jar' || true

# 2. 이전 배포 파일 삭제
echo "Removing old application files..."
rm -rf /home/ubuntu/app/*

# 3. 디렉토리 생성 및 권한 설정
echo "Ensuring deployment directory exists and setting permissions..."
mkdir -p /home/ubuntu/app
chown ubuntu:ubuntu /home/ubuntu/app

echo "BeforeInstall tasks completed.
