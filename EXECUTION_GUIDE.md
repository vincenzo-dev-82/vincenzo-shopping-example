# 캐시노트 마켓 주문 서비스 실행 가이드

## 사전 요구사항

- Docker & Docker Compose
- JDK 17 이상
- Git

## 실행 방법

### 1. 프로젝트 클론

```bash
git clone https://github.com/vincenzo-dev-82/vincenzo-shopping-example.git
cd vincenzo-shopping-example
```

### 2. JAR 파일 빌드

```bash
./gradlew clean build
```

### 3. Docker 이미지 빌드

각 서비스의 Dockerfile이 필요합니다. 먼저 Dockerfile을 생성합니다:

```bash
# 각 서비스 디렉토리에 Dockerfile 생성
# member-service/Dockerfile, product-service/Dockerfile 등
```

그 후 docker-compose로 빌드:

```bash
docker-compose build
```

### 4. 전체 서비스 실행

```bash
docker-compose up -d
```

### 5. 서비스 상태 확인

```bash
docker-compose ps
```

### 6. 초기 데이터 확인

Product Service는 시작 시 자동으로 샘플 상품 데이터를 생성합니다.

### 7. 서비스 접속 정보

- **Member Service**: http://localhost:8081
- **Product Service**: http://localhost:8082
- **Order Service**: http://localhost:8083
- **Payment Service**: http://localhost:8084
- **MySQL**: localhost:3306 (root/root)
- **Kafka**: localhost:9092

### 8. 로그 확인

```bash
# 특정 서비스 로그 확인
docker-compose logs -f member-service

# 전체 서비스 로그 확인
docker-compose logs -f
```

### 9. 서비스 중지

```bash
docker-compose down

# 볼륨까지 삭제하려면
docker-compose down -v
```

## 개별 서비스 실행 (개발용)

### 1. 인프라 실행

```bash
# MySQL과 Kafka만 실행
docker-compose up -d mysql kafka zookeeper
```

### 2. 각 서비스 개별 실행

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

## 트러블슈팅

### MySQL 접속 오류

```bash
# MySQL 컨테이너 재시작
docker-compose restart mysql

# MySQL 로그 확인
docker-compose logs mysql
```

### Kafka 연결 오류

```bash
# Kafka, Zookeeper 재시작
docker-compose restart kafka zookeeper

# 토픽 수동 생성
docker exec -it shopping-kafka kafka-topics --create --topic order-events --bootstrap-server localhost:9092
docker exec -it shopping-kafka kafka-topics --create --topic payment-events --bootstrap-server localhost:9092
```
