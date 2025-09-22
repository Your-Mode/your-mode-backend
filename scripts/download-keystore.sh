#!/bin/bash

# EC2에서 실행할 keystore 다운로드 스크립트
# CodeDeploy 배포 시 실행

set -e

echo "Downloading keystore from S3..."

# keystore 디렉토리 생성
sudo mkdir -p /opt/yourmode/ssl
sudo chown ubuntu:ubuntu /opt/yourmode/ssl

# S3에서 keystore 다운로드
aws s3 cp s3://yourmode-spring-prod/ssl/keystore.p12 /opt/yourmode/ssl/keystore.p12

# 권한 설정
chmod 600 /opt/yourmode/ssl/keystore.p12

# keystore 파일 검증
if [ -f "/opt/yourmode/ssl/keystore.p12" ]; then
    echo "Keystore downloaded successfully!"
    echo "Location: /opt/yourmode/ssl/keystore.p12"
    ls -la /opt/yourmode/ssl/keystore.p12
else
    echo "Error: Keystore download failed!"
    exit 1
fi
