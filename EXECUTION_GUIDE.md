# 캐시노트 마켓 주문 서비스 실행 가이드

## 목차
1. [프로젝트 개요](#프로젝트-개요)
2. [사전 요구사항](#사전-요구사항)
3. [프로젝트 구조](#프로젝트-구조)
4. [빠른 시작 가이드](#빠른-시작-가이드)
5. [상세 실행 방법](#상세-실행-방법)
6. [API 테스트 가이드](#api-테스트-가이드)
7. [트러블슈팅](#트러블슈팅)
8. [모니터링 및 로깅](#모니터링-및-로깅)

## 프로젝트 개요

캐시노트 마켓 주문 서비스는 마이크로서비스 아키텍처로 구현된 이커머스 플랫폼입니다. 
다양한 결제 수단(PG, 캐시노트 포인트, BNPL, 복합결제)을 지원하며, 헥사고날 아키텍처 패턴을 적용하여 
비즈니스 로직과 인프라스트럭처를 명확히 분리했습니다.

### 주요 특징
- **마이크로서비스 아키텍처**: 서비스별 독립적인 배포와 확장 가능
- **헥사고날 아키텍처**: 비즈니스 로직의 독립성 보장
- **다양한 결제 수단**: PG, 포인트, BNPL, 쿠폰, 복합결제 지원
- **이벤트 기반 통신**: Kafka를 통한 비동기 통신
- **서비스 간 동기 통신**: gRPC를 통한 고성능 통신

## 사전 요구사항

### 필수 소프트웨어
- **JDK 17** 이상
- **Docker** 20.10.0 이상
- **Docker Compose** 2.0.0 이상
- **Git** 2.30.0 이상

### 권장 개발 도구
- **IntelliJ IDEA** (Kotlin 지원)
- **DBeaver** 또는 **MySQL Workbench** (DB 관리)
- **Postman** 또는 **curl** (API 테스트)
- **Kafka UI** (선택사항, Kafka 모니터링)

### 시스템 요구사항
- **메모리**: 최소 8GB RAM (권장 16GB)
- **디스크**: 최소 10GB 여유 공간
- **CPU**: 4코어 이상 권장

## 프로젝트 구조

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
├── docker-compose.yml      # Docker 설정
├── build.gradle.kts        # Gradle 빌드 설정
└── README.md
```

### 서비스별 포트 정보

#### HTTP REST API 포트
- **Member Service**: 8081
- **Product Service**: 8082
- **Order Service**: 8083
- **Payment Service**: 8084

#### gRPC 포트
- **Member Service**: 9090
- **Product Service**: 9091
- **Order Service**: 9094
- **Payment Service**: 9093

#### 인프라 포트
- **MySQL**: 3306
- **Kafka**: 9092
- **Zookeeper**: 2181

## 빠른 시작 가이드

### 1. 프로젝트 클론
```bash
git clone https://github.com/vincenzo-dev-82/vincenzo-shopping-example.git
cd vincenzo-shopping-example
```

### 2. 인프라 실행
```bash
# MySQL과 Kafka 컨테이너 실행
docker-compose up -d

# 컨테이너 상태 확인
docker-compose ps

# 로그 확인 (문제 발생 시)
docker-compose logs -f
```

### 3. 프로젝트 빌드
```bash
# 전체 프로젝트 빌드
./gradlew clean build

# 테스트 제외하고 빌드 (빠른 빌드)
./gradlew clean build -x test
```

### 4. 서비스 실행 (각각 다른 터미널에서)
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

### 5. 헬스 체크
```bash
# 각 서비스 헬스 체크
curl http://localhost:8081/api/members/hello
curl http://localhost:8082/api/products/hello
curl http://localhost:8083/api/orders/hello
curl http://localhost:8084/api/payments/hello
```

## 상세 실행 방법

### Docker를 사용한 전체 서비스 실행

1. **Dockerfile 빌드** (각 서비스별)
```bash
# 프로젝트 루트에서
docker build -f member-service/Dockerfile -t vincenzo/member-service:latest .
docker build -f product-service/Dockerfile -t vincenzo/product-service:latest .
docker build -f order-service/Dockerfile -t vincenzo/order-service:latest .
docker build -f payment-service/Dockerfile -t vincenzo/payment-service:latest .
```

2. **docker-compose로 전체 실행**
```bash
# 인프라 + 애플리케이션 모두 실행
docker-compose -f docker-compose.yml -f docker-compose.app.yml up -d
```

### 개발 환경에서 개별 실행

1. **데이터베이스 초기화**
```bash
# MySQL 접속
docker exec -it shopping-mysql mysql -uroot -proot

# 데이터베이스 확인
USE shop;
SHOW TABLES;
```

2. **환경변수 설정** (선택사항)
```bash
export SPRING_PROFILES_ACTIVE=local
export DB_HOST=localhost
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

3. **IDE에서 실행**
- IntelliJ IDEA에서 각 서비스의 `*ServiceApplication.kt` 파일 실행
- VM options: `-Xmx1g -Xms512m`
- Active profiles: `local`

## API 테스트 가이드

### 1. 회원 생성
```bash
curl -X POST http://localhost:8081/api/members \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "테스트 사용자",
    "phoneNumber": "010-1234-5678"
  }'
```

### 2. 상품 조회
```bash
# 전체 상품 목록
curl http://localhost:8082/api/products

# 특정 상품 조회
curl http://localhost:8082/api/products/1
```

### 3. 주문 생성 (단일 결제)
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

### 4. 주문 생성 (복합 결제)
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

### 5. 결제 내역 조회
```bash
# 주문 ID로 결제 조회
curl http://localhost:8084/api/payments/order/1001
```

## 트러블슈팅

### 1. Docker 관련 문제

#### MySQL 연결 실패
```bash
# 컨테이너 로그 확인
docker logs shopping-mysql

# 네트워크 확인
docker network ls
docker network inspect vincenzo-shopping-example_default

# 재시작
docker-compose restart mysql
```

#### Kafka 연결 실패
```bash
# Kafka 상태 확인
docker exec -it shopping-kafka kafka-topics --list --bootstrap-server localhost:9092

# 토픽 수동 생성
docker exec -it shopping-kafka kafka-topics --create \
  --topic order-events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

### 2. 애플리케이션 문제

#### 포트 충돌
```bash
# 사용 중인 포트 확인
lsof -i :8081
netstat -an | grep 8081

# 포트 변경 (application.yml)
server:
  port: 8085  # 변경된 포트
```

#### 메모리 부족
```bash
# JVM 힙 메모리 증가
export JAVA_OPTS="-Xmx2g -Xms1g"
./gradlew :order-service:bootRun
```

#### gRPC 통신 실패
```bash
# gRPC 로그 레벨 상세 설정
logging:
  level:
    io.grpc: DEBUG
    net.devh: DEBUG
```

### 3. 빌드 문제

#### Gradle 빌드 실패
```bash
# Gradle 캐시 삭제
./gradlew clean
rm -rf ~/.gradle/caches/

# 의존성 새로고침
./gradlew build --refresh-dependencies
```

#### Proto 파일 컴파일 실패
```bash
# Proto 파일 수동 컴파일
./gradlew :common:generateProto
```

## 모니터링 및 로깅

### 1. 애플리케이션 로그 확인

#### 파일 로그
```bash
# 로그 파일 위치
tail -f logs/member-service.log
tail -f logs/order-service.log
```

#### 실시간 로그
```bash
# Docker 로그
docker logs -f vincenzo-member-service

# Gradle 실행 시
./gradlew :order-service:bootRun | tee order.log
```

### 2. 데이터베이스 모니터링

```sql
-- 활성 연결 확인
SHOW PROCESSLIST;

-- 테이블 상태 확인
SHOW TABLE STATUS FROM shop;

-- 슬로우 쿼리 확인
SHOW VARIABLES LIKE 'slow_query_log%';
```

### 3. Kafka 모니터링

```bash
# 토픽 목록 확인
docker exec -it shopping-kafka kafka-topics --list \
  --bootstrap-server localhost:9092

# 컨슈머 그룹 확인
docker exec -it shopping-kafka kafka-consumer-groups --list \
  --bootstrap-server localhost:9092

# 메시지 확인
docker exec -it shopping-kafka kafka-console-consumer \
  --topic order-events \
  --from-beginning \
  --bootstrap-server localhost:9092
```

### 4. 성능 모니터링

#### JVM 메모리 사용량
```bash
# jstat을 사용한 GC 모니터링
jstat -gc <PID> 1000

# jmap을 사용한 힙 덤프
jmap -dump:format=b,file=heapdump.hprof <PID>
```

#### API 응답 시간 측정
```bash
# curl을 사용한 응답 시간 측정
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:8083/api/orders/hello

# curl-format.txt 내용
time_namelookup:  %{time_namelookup}\n
time_connect:  %{time_connect}\n
time_appconnect:  %{time_appconnect}\n
time_pretransfer:  %{time_pretransfer}\n
time_redirect:  %{time_redirect}\n
time_starttransfer:  %{time_starttransfer}\n
time_total:  %{time_total}\n
```

## 추가 팁

### 1. 개발 효율성 향상
- Spring Boot DevTools 사용으로 hot reload 활성화
- LiveReload 브라우저 확장 프로그램 설치
- IntelliJ IDEA의 "Build project automatically" 옵션 활성화

### 2. 디버깅 팁
- 각 서비스별로 다른 디버그 포트 설정 (5005, 5006, 5007, 5008)
- 조건부 브레이크포인트 활용
- 로그 레벨을 DEBUG로 설정하여 상세 정보 확인

### 3. 테스트 데이터 관리
- `src/test/resources/data.sql`에 테스트 데이터 정의
- 프로파일별 다른 데이터 세트 관리
- Testcontainers 사용으로 격리된 테스트 환경 구축

## 문의 및 지원

프로젝트 실행 중 문제가 발생하면:
1. GitHub Issues에 문제 등록
2. 로그 파일과 함께 상세한 에러 메시지 첨부
3. 실행 환경 정보 (OS, Java 버전, Docker 버전 등) 포함

---

Happy Coding! 🚀
