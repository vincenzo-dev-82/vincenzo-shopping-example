# Vincenzo Shopping Example

캐시노트 마켓 주문 서비스 구현 프로젝트

## 프로젝트 구조

- **common**: 공통 모듈 (Kafka, gRPC Proto 파일 및 공통 클래스)
- **member-service**: 회원 서비스 (포트: 8081, gRPC: 9090)
- **product-service**: 상품 서비스 (포트: 8082, gRPC: 9091)
- **order-service**: 주문 서비스 (포트: 8083, gRPC: 9094)
- **payment-service**: 결제 서비스 (포트: 8084, gRPC: 9093)

## 기술 스택

- Kotlin
- Spring Boot 3.2.2
- JDK 17
- MySQL 8.0
- Apache Kafka
- gRPC (서비스 간 통신)
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
# Gradle Wrapper 설정 (최초 1회)
gradle wrapper --gradle-version=8.5

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

#### REST API 테스트
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

#### gRPC 통신 테스트
```bash
# Order Service에서 Member Service 호출 (gRPC)
curl http://localhost:8083/api/orders/test-grpc/member/1

# Order Service에서 Product Service 호출 (gRPC)
curl http://localhost:8083/api/orders/test-grpc/product/1
```

#### 주문 생성 (gRPC 통합)
```bash
# 주문 생성 - 회원 정보, 상품 정보를 gRPC로 조회하고 재고 차감
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ],
    "paymentMethod": "PG_KPN"
  }'
```

## 서비스 포트

### HTTP 포트
- Member Service: 8081
- Product Service: 8082
- Order Service: 8083
- Payment Service: 8084

### gRPC 포트
- Member Service: 9090
- Product Service: 9091
- Order Service: 9094
- Payment Service: 9093

### 인프라 포트
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

모든 서비스는 단일 데이터베이스를 공유합니다:
- **Database**: shop
- **Tables**: 
  - members (회원 정보)
  - products (상품 정보) 
  - orders (주문 정보)
  - payments (결제 정보)

## gRPC 설정

### Proto 파일 위치
- `common/src/main/proto/member.proto`: 회원 서비스 Proto 정의
- `common/src/main/proto/product.proto`: 상품 서비스 Proto 정의

### gRPC 서비스
- **MemberService**: 회원 조회, 포인트 업데이트
- **ProductService**: 상품 조회, 재고 업데이트

### gRPC 클라이언트 설정
Order Service와 Payment Service는 gRPC 클라이언트를 통해 다른 서비스와 통신합니다:
- `@GrpcClient` 어노테이션을 사용한 자동 설정
- application.yml에서 각 서비스 주소 설정

## Kafka 설정

### Topic
- `order-events`: 주문 이벤트
- `payment-events`: 결제 이벤트

### Producer
- Order Service: 주문 생성 시 이벤트 발행

### Consumer  
- Payment Service: 주문 이벤트 구독하여 결제 처리

## DB 접속 정보

```bash
# Docker exec로 접속
docker exec -it shopping-mysql mysql -uroot -proot shop

# 로컬 MySQL 클라이언트로 접속
mysql -h localhost -P 3306 -u root -proot shop
```

### DBeaver/MySQL Workbench 설정
- Host: localhost
- Port: 3306
- Database: shop
- Username: root
- Password: root
