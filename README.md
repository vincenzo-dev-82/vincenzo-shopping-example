# 캐시노트 마켓 주문 서비스 (Vincenzo Shopping Example)

## 🚀 프로젝트 개요

캐시노트 마켓은 다양한 판매자가 다양한 상품을 다양한 결제수단으로 판매하는 이커머스 플랫폼입니다. 
이 프로젝트는 마이크로서비스 아키텍처와 헥사고날 아키텍처를 적용하여 구현된 주문/결제 시스템입니다.

### 주요 특징
- ✅ **다양한 결제 수단**: PG, 캐시노트 포인트, BNPL(후불결제), 쿠폰
- ✅ **복합결제 지원**: 여러 결제 수단을 조합하여 결제 가능
- ✅ **SOLID 원칙 적용**: 확장 가능하고 유지보수가 용이한 설계
- ✅ **이벤트 기반 아키텍처**: Kafka를 통한 비동기 통신
- ✅ **gRPC 통신**: 서비스 간 고성능 동기 통신
- ✅ **헥사고날 아키텍처**: 비즈니스 로직과 인프라의 명확한 분리

## 📋 목차
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [빠른 시작](#빠른-시작)
- [API 명세](#api-명세)
- [결제 수단별 제약사항](#결제-수단별-제약사항)
- [산출물](#산출물)
- [기여하기](#기여하기)

## 🛠 기술 스택

### Backend
- **Language**: Kotlin 1.9.22
- **Framework**: Spring Boot 3.2.2
- **JDK**: 17

### Database & Messaging
- **Database**: MySQL 8.0
- **Message Queue**: Apache Kafka
- **Cache**: Redis (추후 구현)

### Communication
- **REST API**: Spring Web
- **gRPC**: 서비스 간 통신
- **Event Driven**: Kafka

### Architecture
- **Microservices Architecture**
- **Hexagonal Architecture** (Ports & Adapters)
- **Domain Driven Design (DDD)**
- **SOLID Principles**

## 📁 프로젝트 구조

```
vincenzo-shopping-example/
├── common/                   # 공통 모듈 (Kafka, gRPC Proto)
│   ├── src/main/proto/      # gRPC Proto 정의
│   └── src/main/kotlin/     # 공통 클래스
├── member-service/          # 회원 서비스
│   ├── adapter/            # 인바운드/아웃바운드 어댑터
│   ├── application/        # 비즈니스 로직
│   └── domain/            # 도메인 모델
├── product-service/         # 상품 서비스
├── order-service/          # 주문 서비스
├── payment-service/        # 결제 서비스
├── docker-compose.yml      # 인프라 설정
├── docker-compose.app.yml  # 애플리케이션 설정
└── build.gradle.kts        # Gradle 빌드 설정
```

### 서비스별 포트 정보

| 서비스 | HTTP 포트 | gRPC 포트 |
|--------|-----------|-----------|
| Member Service | 8081 | 9090 |
| Product Service | 8082 | 9091 |
| Order Service | 8083 | 9094 |
| Payment Service | 8084 | 9093 |

## 🚀 빠른 시작

### 1. 프로젝트 클론
```bash
git clone https://github.com/vincenzo-dev-82/vincenzo-shopping-example.git
cd vincenzo-shopping-example
```

### 2. 인프라 실행
```bash
# MySQL과 Kafka 실행
docker-compose up -d
```

### 3. 프로젝트 빌드
```bash
./gradlew clean build
```

### 4. 서비스 실행
```bash
# 각각 다른 터미널에서 실행
./gradlew :member-service:bootRun
./gradlew :product-service:bootRun
./gradlew :order-service:bootRun
./gradlew :payment-service:bootRun
```

### 5. Docker로 전체 실행
```bash
# 인프라 + 애플리케이션 모두 실행
docker-compose up -d
docker-compose -f docker-compose.app.yml up -d
```

## 📡 API 명세

### 주문 생성 (단일 결제)
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

### 주문 생성 (복합 결제)
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
    "paymentMethod": "COMPOSITE"
  }'
```

### 결제 처리
```bash
curl -X POST http://localhost:8084/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1001,
    "memberId": 1,
    "totalAmount": 100000,
    "paymentDetails": [
      {
        "method": "PG_KPN",
        "amount": 70000
      },
      {
        "method": "CASHNOTE_POINT",
        "amount": 20000
      },
      {
        "method": "COUPON",
        "amount": 10000,
        "metadata": {
          "couponCode": "WELCOME10"
        }
      }
    ]
  }'
```

## 💳 결제 수단별 제약사항

### 1. PG (Payment Gateway)
- ✅ 단독 결제 가능
- ✅ 복합결제의 메인 결제수단으로 사용 가능
- 최소 결제 금액: 100원
- 최대 결제 금액: 50,000,000원

### 2. 캐시노트 포인트
- ✅ 단독 결제 가능 (잔액 충분 시)
- ✅ 복합결제의 서브 결제수단으로 사용 가능
- 보유 포인트 내에서만 사용 가능

### 3. BNPL (Buy Now Pay Later)
- ✅ 단독 결제만 가능
- ❌ 복합결제 불가능
- 최소 결제 금액: 10,000원
- 최대 결제 금액: 5,000,000원

### 4. 쿠폰
- ❌ 단독 결제 불가능
- ✅ 복합결제의 서브 결제수단으로만 사용 가능
- 쿠폰별 할인 금액 적용

### 5. 복합결제
- 반드시 PG가 메인 결제수단이어야 함
- 서브 결제수단: 포인트, 쿠폰만 가능
- BNPL은 복합결제에 포함 불가

## 📚 산출물

### 문서
- 📄 [실행 가이드](EXECUTION_GUIDE.md) - 상세한 실행 방법 및 트러블슈팅
- 📄 [API 명세서](API_SPECIFICATION.md) - REST API 및 gRPC API 상세 명세
- 📄 [구현 설명서](IMPLEMENTATION_GUIDE.md) - 설계 구조, 고민사항, 개선방향
- 📄 [데이터베이스 ERD](DATABASE_ERD.md) - 테이블 구조 및 관계도
- 📄 [SOLID 원칙 적용](payment-service/SOLID_PRINCIPLES.md) - 결제 서비스의 SOLID 원칙 적용 사례

### 주요 구현 내용
- ✅ 마이크로서비스 아키텍처
- ✅ 헥사고날 아키텍처 (포트 & 어댑터)
- ✅ SOLID 원칙을 적용한 결제 프로세서 설계
- ✅ 이벤트 기반 비동기 통신 (Kafka)
- ✅ gRPC를 통한 서비스 간 동기 통신
- ✅ 복합결제 트랜잭션 처리
- ✅ Docker 지원

## 🔧 개발 환경 설정

### Prerequisites
- JDK 17+
- Docker & Docker Compose
- Gradle 8.5+

### IDE 설정
- IntelliJ IDEA 권장
- Kotlin 플러그인 설치
- Enable annotation processing

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 라이센스

이 프로젝트는 MIT 라이센스를 따릅니다.

## 👨‍💻 개발자

- **Vincenzo** - [GitHub](https://github.com/vincenzo-dev-82)

## 🙏 감사의 말

캐시노트 팀에게 이런 흥미로운 과제를 주셔서 감사합니다. 
이 프로젝트를 통해 복잡한 결제 시스템을 설계하고 구현하는 좋은 경험을 할 수 있었습니다.

---

**Note**: 이 프로젝트는 캐시노트 과제를 위해 제작된 예제 프로젝트입니다.
