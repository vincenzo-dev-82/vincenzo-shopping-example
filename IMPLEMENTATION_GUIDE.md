# 구현 설명

## 1. 설계 구조

### 1.1 전체 아키텍처
본 프로젝트는 **마이크로서비스 아키텍처(MSA)**와 **헥사고날 아키텍처(Hexagonal Architecture)**를 조합하여 설계되었습니다.

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client (Web/Mobile)                      │
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                          API Gateway                             │
│                    (현재 버전에서는 미구현)                      │
└─────────────────────────────────────────────────────────────────┘
                                    │
        ┌───────────────┬───────────┴───────────┬─────────────┐
        ▼               ▼                       ▼             ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│Member Service │ │Product Service│ │ Order Service │ │Payment Service│
│   (8081)      │ │   (8082)      │ │   (8083)      │ │   (8084)      │
└───────────────┘ └───────────────┘ └───────────────┘ └───────────────┘
        │               │                       │             │
        └───────────────┴───────────────────────┴─────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    ▼                               ▼
            ┌─────────────┐                 ┌─────────────┐
            │Point Service│                 │    MySQL    │
            │   (8085)    │                 │   (3306)    │
            └─────────────┘                 └─────────────┘
                    │                               │
                    └───────────────┬───────────────┘
                                    ▼
                            ┌─────────────┐
                            │    Kafka    │
                            │   (9092)    │
                            └─────────────┘
```

### 1.2 헥사고날 아키텍처
각 마이크로서비스는 헥사고날 아키텍처로 구성되어 있습니다:

```
서비스 구조:
├── domain/              # 비즈니스 도메인
│   ├── Order.kt
│   ├── Payment.kt
│   └── ...
├── application/         # 애플리케이션 서비스
│   ├── port/
│   │   ├── in/         # 인바운드 포트 (UseCase)
│   │   └── out/        # 아웃바운드 포트 (Repository)
│   └── service/        # 비즈니스 로직 구현
└── adapter/            # 어댑터
    ├── in/
    │   ├── web/        # REST Controller
    │   └── grpc/       # gRPC Server
    └── out/
        ├── persistence/ # JPA Repository
        ├── grpc/       # gRPC Client
        └── kafka/      # Kafka Producer/Consumer
```

### 1.3 통신 방식

#### 동기 통신 (gRPC)
- 실시간 데이터 조회가 필요한 경우
- 트랜잭션 일관성이 중요한 경우
- 예: 주문 시 회원 정보 조회, 재고 확인 및 차감

#### 비동기 통신 (Kafka)
- 결과를 즉시 알 필요가 없는 경우
- 서비스 간 결합도를 낮추고 싶은 경우
- 예: 주문 생성 이벤트, 결제 완료 알림

## 2. 설계 시 고민사항

### 2.1 동기 vs 비동기 통신

#### 고민 1: 주문 생성 시 결제 처리
- **옵션 A**: 동기 방식 (현재 구현)
  - 주문 서비스가 결제 서비스를 직접 호출
  - 장점: 즉시 결과 확인 가능, 구현 단순
  - 단점: 서비스 간 강한 결합, 장애 전파

- **옵션 B**: 비동기 방식
  - Kafka를 통한 이벤트 기반 처리
  - 장점: 느슨한 결합, 장애 격리
  - 단점: 최종 일관성, 복잡한 에러 처리

**결정**: 초기 버전은 동기 방식으로 구현하되, 향후 Saga 패턴으로 전환 예정

#### 고민 2: 재고 관리
- **옵션 A**: 낙관적 락 (Optimistic Lock)
  - 충돌이 적을 것으로 예상
  - 성능 우선

- **옵션 B**: 비관적 락 (Pessimistic Lock) - 현재 구현
  - 재고 정확성 우선
  - 동시 주문 시 데이터 일관성 보장

**결정**: 재고의 정확성이 중요하므로 비관적 락 채택

### 2.2 트랜잭션 관리

#### 분산 트랜잭션 처리
- **문제**: 여러 서비스에 걸친 트랜잭션 관리
- **해결 방안**:
  1. **2PC (Two-Phase Commit)**: 성능 이슈로 제외
  2. **Saga Pattern**: 향후 구현 예정
  3. **현재**: 보상 트랜잭션으로 처리

```kotlin
// 주문 생성 프로세스
1. 재고 확인 (Product Service)
2. 재고 차감 (Product Service)
3. 포인트 차감 (Point Service)
4. 결제 처리 (Payment Service)
5. 주문 생성 (Order Service)

// 실패 시 롤백
- 4번 실패: 3번 롤백 (포인트 환불)
- 3번 실패: 2번 롤백 (재고 복구)
```

### 2.3 결제 방법 설계

#### 결제 방법별 제약사항 처리
```kotlin
// Strategy Pattern 적용
interface PaymentProcessor {
    fun validate(request: PaymentRequest): ValidationResult
    fun process(request: PaymentRequest): PaymentResult
    fun rollback(payment: Payment)
}

// 각 결제 방법별 구현
class PgPaymentProcessor : PaymentProcessor { ... }
class PointPaymentProcessor : PaymentProcessor { ... }
class BnplPaymentProcessor : PaymentProcessor { ... }
class CouponPaymentProcessor : PaymentProcessor { ... }
```

#### 복합 결제 처리
```kotlin
class CompositePaymentProcessor(
    private val processors: Map<PaymentMethod, PaymentProcessor>
) {
    fun process(details: List<PaymentDetail>): PaymentResult {
        // 1. 전체 검증
        validateComposite(details)
        
        // 2. 순차 처리
        val results = mutableListOf<PaymentResult>()
        try {
            details.forEach { detail ->
                results.add(processors[detail.method]!!.process(detail))
            }
        } catch (e: Exception) {
            // 3. 실패 시 롤백
            results.forEach { rollback(it) }
            throw e
        }
    }
}
```

## 3. 개선 방향

### 3.1 단기 개선사항 (1-3개월)

#### 1. API Gateway 도입
- Kong 또는 Spring Cloud Gateway
- 인증/인가 중앙화
- Rate Limiting
- 로드 밸런싱

#### 2. 서비스 메시 도입
- Istio 또는 Linkerd
- 서비스 간 통신 암호화
- 트래픽 관리
- 서킷 브레이커

#### 3. 모니터링 강화
- Prometheus + Grafana
- 분산 추적 (Jaeger)
- 중앙 로깅 (ELK Stack)

#### 4. 테스트 커버리지 향상
- 단위 테스트: 80% 이상
- 통합 테스트: 주요 시나리오
- 부하 테스트: K6 또는 JMeter

### 3.2 장기 개선사항 (6-12개월)

#### 1. 이벤트 소싱 도입
```kotlin
// 현재: State 기반
data class Order(
    val id: Long,
    val status: OrderStatus,
    val items: List<OrderItem>
)

// 개선: Event 기반
sealed class OrderEvent {
    data class OrderCreated(val orderId: Long, val items: List<OrderItem>)
    data class OrderPaid(val orderId: Long, val paymentId: Long)
    data class OrderCancelled(val orderId: Long, val reason: String)
}
```

#### 2. CQRS 패턴 적용
- Command와 Query 분리
- Read Model 별도 구성
- 성능 최적화

#### 3. 멀티 테넌시 지원
- 판매자별 데이터 격리
- 동적 스키마 관리
- 테넌트별 설정 관리

#### 4. 국제화 지원
- 다중 통화 지원
- 다국어 지원
- 지역별 결제 수단

## 4. 제약사항

### 4.1 현재 구현의 제약사항

#### 1. 데이터베이스
- 단일 MySQL 인스턴스 사용
- 서비스별 스키마 분리 필요
- Read Replica 미구현

#### 2. 보안
- 인증/인가 미구현
- API Key 관리 없음
- 통신 암호화 미적용

#### 3. 성능
- 캐싱 전략 미적용
- 비동기 처리 제한적
- 배치 처리 미구현

### 4.2 트래픽 제한

#### 현재 처리 가능 용량
- **단일 서버 기준**
  - TPS: 약 100-200
  - 동시 사용자: 약 1,000명
  - 일일 주문: 약 10,000건

#### 병목 지점
1. **데이터베이스**: 단일 인스턴스
2. **동기 통신**: gRPC 호출 지연
3. **트랜잭션**: 분산 트랜잭션 오버헤드

## 5. 트래픽 증가 대응 방안

### 5.1 10배 증가 시 (TPS 1,000-2,000)

#### 1. 수평 확장
```yaml
# Kubernetes Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 5  # 5개 인스턴스로 확장
```

#### 2. 데이터베이스 최적화
- Read Replica 추가
- 커넥션 풀 최적화
- 인덱스 튜닝
- 쿼리 최적화

#### 3. 캐싱 도입
```kotlin
@Cacheable("products")
fun getProduct(productId: Long): Product {
    return productRepository.findById(productId)
}
```

### 5.2 100배 증가 시 (TPS 10,000-20,000)

#### 1. 아키텍처 전환
- CDN 도입 (CloudFront)
- API Gateway (Kong)
- Service Mesh (Istio)
- NoSQL 도입 (DynamoDB)
- RDBMS 클러스터 (Aurora)

#### 2. 데이터베이스 샤딩
```kotlin
// 회원 ID 기반 샤딩
@Component
class ShardingStrategy {
    fun getShardKey(memberId: Long): Int {
        return (memberId % SHARD_COUNT).toInt()
    }
}
```

#### 3. 이벤트 기반 아키텍처 전환
- Event Sourcing
- CQRS 패턴
- Saga Pattern

## 6. 기타 설명

### 6.1 기술 선택 이유

#### Kotlin
- Java 대비 간결한 문법
- Null Safety
- Coroutine 지원
- Spring Boot와의 우수한 호환성

#### gRPC
- 높은 성능 (HTTP/2, Protocol Buffers)
- 타입 안정성
- 다양한 언어 지원
- 스트리밍 지원

#### Hexagonal Architecture
- 비즈니스 로직 보호
- 테스트 용이성
- 기술 독립적
- 확장성

### 6.2 개발 철학

#### 1. Domain-Driven Design
- 도메인 중심 설계
- Ubiquitous Language
- Bounded Context 명확화

#### 2. Clean Code
- 의미 있는 이름
- 작은 함수
- 단일 책임 원칙

#### 3. Test-Driven Development
- Red-Green-Refactor
- 테스트 커버리지 80% 이상
- 모든 엣지 케이스 테스트

### 6.3 보안 고려사항

#### 1. API 보안
- JWT 토큰 기반 인증 (향후 구현)
- Role 기반 접근 제어
- Rate Limiting

#### 2. 데이터 암호화
- 통신: TLS 1.3
- 저장: AES-256
- 개인정보: 별도 암호화

#### 3. PCI DSS 준수
- 카드 정보 미저장
- 토큰화 사용
- 정기 보안 감사

### 6.4 팀 구조 제안

#### 1. 서비스별 팀
- Order Team: 주문 도메인
- Payment Team: 결제 도메인
- Member Team: 회원 도메인
- Platform Team: 인프라/공통

#### 2. 역할별 구성
- Backend Developer: 3-4명
- DevOps Engineer: 1-2명
- QA Engineer: 1-2명
- Product Owner: 1명

## 7. 결론

본 프로젝트는 캐시노트 마켓의 복잡한 결제 요구사항을 충족하는 확장 가능한 주문 시스템을 구현했습니다. 마이크로서비스와 헥사고날 아키텍처를 통해 비즈니스 로직을 보호하고, 향후 요구사항 변경에 유연하게 대응할 수 있는 구조를 만들었습니다.

현재는 MVP(Minimum Viable Product) 수준이지만, 명확한 개선 방향과 확장 계획을 통해 대규모 트래픽을 처리할 수 있는 시스템으로 발전시킬 수 있습니다.
