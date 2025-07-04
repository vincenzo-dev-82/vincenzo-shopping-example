# Vincenzo Shopping Example

캐시노트 마켓 주문 서비스 구현 프로젝트

## 프로젝트 구조

- **common**: 공통 모듈 (Kafka, gRPC Proto 파일 및 공통 클래스)
- **member-service**: 회원 서비스 (포트: 8081, gRPC: 9090)
- **product-service**: 상품 서비스 (포트: 8082, gRPC: 9091)
- **order-service**: 주문 서비스 (포트: 8083, gRPC: 9094)
- **payment-service**: 결제 서비스 (포트: 8084, gRPC: 9093)
- **point-service**: 포인트 서비스 (포트: 8085, gRPC: 9095)

## 기술 스택

- Kotlin
- Spring Boot 3.2.2
- JDK 17
- MySQL 8.0
- Apache Kafka
- gRPC (서비스 간 통신)
- Hexagonal Architecture
- JUnit 5 & MockK (테스트)

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

# Common 모듈 먼저 빌드 (Proto 파일 컴파일)
./gradlew :common:build

# 전체 프로젝트 빌드
./gradlew clean build

# 또는 각 서비스별 빌드
./gradlew :member-service:build
./gradlew :product-service:build
./gradlew :order-service:build
./gradlew :payment-service:build
./gradlew :point-service:build
```

### 3. 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 서비스 테스트만 실행
./gradlew :member-service:test
./gradlew :product-service:test
./gradlew :point-service:test
./gradlew :order-service:test
./gradlew :payment-service:test

# 테스트 리포트 확인
# 각 서비스의 build/reports/tests/test/index.html 파일 확인
```

### 4. 서비스 실행

각 서비스를 별도의 터미널에서 실행:

```bash
# Member Service
./gradlew :member-service:bootRun

# Product Service
./gradlew :product-service:bootRun

# Point Service
./gradlew :point-service:bootRun

# Order Service
./gradlew :order-service:bootRun

# Payment Service
./gradlew :payment-service:bootRun
```

### 5. API 테스트

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

# 포인트 충전
curl -X POST http://localhost:8085/api/points/charge \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "amount": 50000,
    "description": "포인트 충전"
  }'

# 포인트 잔액 조회
curl http://localhost:8085/api/points/balance/1
```

#### 주문 API

##### 1. PG 단독 결제
```bash
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

##### 2. 포인트 단독 결제
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 1
      }
    ],
    "paymentMethod": "POINT"
  }'
```

##### 3. BNPL(외상결제) 단독 결제
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "items": [
      {
        "productId": 2,
        "quantity": 1
      }
    ],
    "paymentMethod": "BNPL"
  }'
```

##### 4. 복합 결제 (PG + 포인트)
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "items": [
      {
        "productId": 3,
        "quantity": 1
      }
    ],
    "compositePayment": {
      "details": [
        {
          "method": "POINT",
          "amount": 5000
        },
        {
          "method": "PG_KPN",
          "amount": 5000
        }
      ]
    }
  }'
```

##### 5. 복합 결제 (PG + 쿠폰)
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "items": [
      {
        "productId": 4,
        "quantity": 1
      }
    ],
    "compositePayment": {
      "details": [
        {
          "method": "COUPON",
          "amount": 10000,
          "metadata": {
            "coupon_code": "WELCOME10"
          }
        },
        {
          "method": "PG_KPN",
          "amount": 40000
        }
      ]
    }
  }'
```

## 테스트 구조

### 단위 테스트 (Service Layer)
- MockK를 사용한 의존성 모킹
- 비즈니스 로직 검증
- 예외 케이스 테스트

### 통합 테스트 (Controller Layer)
- MockMvc를 사용한 HTTP 엔드포인트 테스트
- H2 인메모리 DB 사용
- 실제 요청/응답 검증

### 테스트 커버리지
- Member Service: 회원 생성, 조회, 포인트 업데이트
- Product Service: 상품 생성, 조회, 재고 업데이트
- Point Service: 포인트 충전, 사용, 환불, 잔액 조회
- Order Service: 주문 생성, 재고 검증, gRPC 통신
- Payment Service: 결제 처리, 취소, 환불, 결제 제약사항 검증

## 결제 시스템 특징

### 결제 방법
1. **PG_KPN**: PG 결제 (카드, 계좌이체 등)
2. **POINT**: 캐시노트 포인트
3. **COUPON**: 쿠폰 할인
4. **BNPL**: Buy Now Pay Later (외상결제)

### 결제 제약사항
- **복합결제**: PG를 메인으로 포인트, 쿠폰 조합 가능
- **BNPL**: 단독 결제만 가능
- **포인트**: 잔액이 충분한 경우 단독 결제 가능
- **쿠폰**: 단독 결제 불가, 반드시 다른 결제수단과 함께 사용

### 결제 프로세스
1. 주문 생성 시 결제 방법 검증
2. 재고 차감
3. 결제 처리 (각 결제수단별 프로세서 실행)
4. 실패 시 롤백 처리
5. 주문 상태 업데이트

## 서비스 포트

### HTTP 포트
- Member Service: 8081
- Product Service: 8082
- Order Service: 8083
- Payment Service: 8084
- Point Service: 8085

### gRPC 포트
- Member Service: 9090
- Product Service: 9091
- Order Service: 9094
- Payment Service: 9093
- Point Service: 9095

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
  - payment_details (결제 상세)
  - point_balances (포인트 잔액)
  - point_transactions (포인트 거래내역)

## gRPC 설정

### Proto 파일 위치
- `common/src/main/proto/member.proto`: 회원 서비스 Proto 정의
- `common/src/main/proto/product.proto`: 상품 서비스 Proto 정의
- `common/src/main/proto/point.proto`: 포인트 서비스 Proto 정의
- `common/src/main/proto/payment.proto`: 결제 서비스 Proto 정의

### gRPC 서비스
- **MemberService**: 회원 조회
- **ProductService**: 상품 조회, 재고 업데이트
- **PointService**: 포인트 조회, 사용, 충전, 환불
- **PaymentService**: 결제 처리, 결제 조회

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
- Payment Service: 주문 이벤트 구독하여 결제 상태 모니터링

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

## 테스트 시나리오

### 1. 기본 데이터 생성
- 서비스 시작 시 자동으로 테스트 데이터 생성
- 회원 3명, 상품 4개, 각 회원에게 100,000 포인트 충전

### 2. 결제 시나리오 테스트
- PG 단독 결제
- 포인트 단독 결제 (잔액 확인)
- BNPL 단독 결제 (신용 평가)
- 복합 결제 (PG + 포인트)
- 복합 결제 (PG + 쿠폰)

### 3. 실패 시나리오
- 포인트 부족
- BNPL 신용 평가 실패
- 쿠폰 단독 결제 시도
- BNPL 복합 결제 시도
