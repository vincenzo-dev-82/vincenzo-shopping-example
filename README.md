# Vincenzo Shopping Example

캐시노트 마켓 주문 서비스 구현 프로젝트

## 프로젝트 구조

- **common**: 공통 모듈 (Kafka, gRPC 관련 공통 클래스)
- **member-service**: 회원 서비스 (포트: 8081, gRPC: 9090)
- **product-service**: 상품 서비스 (포트: 8082, gRPC: 9091)
- **order-service**: 주문 서비스 (포트: 8083, gRPC: 9092)
- **payment-service**: 결제 서비스 (포트: 8084, gRPC: 9093)

## 기술 스택

- Kotlin
- Spring Boot 3.2.2
- JDK 17
- MySQL 8.0
- Apache Kafka
- gRPC
- Hexagonal Architecture

## 실행 방법

### 1. Docker 환경 실행

```bash
# MySQL과 Kafka 실행
docker-compose up -d

# 컨테이너 상태 확인
docker-compose ps
```

### 2. 프로젝트 빌드

```bash
# 프로젝트 빌드
./gradlew clean build

# 또는 각 서비스별 빌드
./gradlew :member-service:build
./gradlew :product-service:build
./gradlew :order-service:build
./gradlew :payment-service:build
```

### 3. 서비스 실행

각 서비스를 별도의 터미널에서 실행:

```bash
# Member Service
./gradlew :member-service:bootRun

# Product Service
./gradlew :product-service:bootRun

# Order Service
./gradlew :order-service:bootRun

# Payment Service
./gradlew :payment-service:bootRun
```

### 4. API 테스트

```bash
# Member Service Hello World
curl http://localhost:8081/api/members/hello

# 회원 생성
curl -X POST http://localhost:8081/api/members \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "Test User",
    "phoneNumber": "010-1234-5678"
  }'
```

## 서비스 포트

- Member Service: HTTP 8081, gRPC 9090
- Product Service: HTTP 8082, gRPC 9091
- Order Service: HTTP 8083, gRPC 9092
- Payment Service: HTTP 8084, gRPC 9093
- MySQL: 3306
- Kafka: 9092
- Zookeeper: 2181

## 아키텍처

각 서비스는 헥사고날 아키텍처로 구성되어 있습니다:

- **Domain**: 비즈니스 로직 및 도메인 모델
- **Application**: Use Case 및 Port 정의
- **Adapter**: 외부 인터페이스 (REST API, gRPC, Database, Kafka)
  - In: 인바운드 어댑터 (Controller, gRPC Server)
  - Out: 아웃바운드 어댑터 (Repository, gRPC Client, Kafka Producer)

## 데이터베이스

각 서비스는 독립적인 데이터베이스를 사용합니다:
- member_db: 회원 정보
- product_db: 상품 정보
- order_db: 주문 정보
- payment_db: 결제 정보
