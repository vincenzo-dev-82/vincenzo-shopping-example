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
- Spring Boot Actuator (Health Check)
- JDK 17
- MySQL 8.0
- Apache Kafka
- gRPC (서비스 간 통신)
- Hexagonal Architecture

## 빠른 시작

### 1. Docker로 전체 환경 실행
```bash
# 인프라 실행 (MySQL, Kafka)
docker-compose up -d

# 프로젝트 빌드
./gradlew clean build

# 전체 서비스 실행 (Docker)
docker-compose -f docker-compose-all.yml up -d
```

### 2. 헬스 체크
```bash
# Spring Boot Actuator를 통한 상태 확인
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health

# 상세 헬스 정보 (JSON)
curl http://localhost:8081/actuator/health | jq .
```

### 3. 주문 테스트
```bash
# 주문 생성 (PG 결제)
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "items": [{"productId": 1, "quantity": 2}],
    "paymentMethod": "PG_KPN"
  }'
```

## 주요 기능

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

## 모니터링

### Spring Boot Actuator 엔드포인트
- **Health Check**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Info**: `/actuator/info`
- **Database Health**: `/actuator/health/db`
- **Liveness Probe**: `/actuator/health/liveness`
- **Readiness Probe**: `/actuator/health/readiness`

## 문서

- [실행 가이드](EXECUTION_GUIDE.md)
- [API 명세](API_SPECIFICATION.md)
- [구현 설명](IMPLEMENTATION_GUIDE.md)
- [데이터베이스 ERD](DATABASE_ERD.md)

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

## 라이센스

이 프로젝트는 학습 및 포트폴리오 목적으로 작성되었습니다.
