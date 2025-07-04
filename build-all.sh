#!/bin/bash

echo "=== Building Vincenzo Shopping Example ==="

# 1. Common 모듈 빌드 (Proto 파일 컴파일)
echo "Building common module..."
./gradlew :common:clean :common:build
if [ $? -ne 0 ]; then
    echo "Common module build failed!"
    exit 1
fi

# 2. 각 서비스 빌드
echo "Building member-service..."
./gradlew :member-service:clean :member-service:build
if [ $? -ne 0 ]; then
    echo "Member service build failed!"
    exit 1
fi

echo "Building product-service..."
./gradlew :product-service:clean :product-service:build
if [ $? -ne 0 ]; then
    echo "Product service build failed!"
    exit 1
fi

echo "Building point-service..."
./gradlew :point-service:clean :point-service:build
if [ $? -ne 0 ]; then
    echo "Point service build failed!"
    exit 1
fi

echo "Building order-service..."
./gradlew :order-service:clean :order-service:build
if [ $? -ne 0 ]; then
    echo "Order service build failed!"
    exit 1
fi

echo "Building payment-service..."
./gradlew :payment-service:clean :payment-service:build
if [ $? -ne 0 ]; then
    echo "Payment service build failed!"
    exit 1
fi

echo "=== All builds completed successfully! ==="
