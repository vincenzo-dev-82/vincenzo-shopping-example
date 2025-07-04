# 코드 실행 가이드

## 사전 준비사항

### 필수 설치 프로그램
- JDK 17 이상
- Docker & Docker Compose
- Git

### 권장 개발 도구
- IntelliJ IDEA (Community/Ultimate)
- DBeaver 또는 MySQL Workbench (DB 관리용)
- Postman 또는 curl (API 테스트용)

## 프로젝트 설정

### 1. 소스코드 다운로드
```bash
git clone https://github.com/vincenzo-dev-82/vincenzo-shopping-example.git
cd vincenzo-shopping-example
```

### 2. Gradle Wrapper 설정
```bash
# Windows
gradlew.bat wrapper --gradle-version=8.5

# Mac/Linux
./gradlew wrapper --gradle-version=8.5
```

## Docker 환경 실행

### 1. Docker Compose로 인프라 실행
```bash
# MySQL, Kafka, Zookeeper 실행
docker-compose up -d

# 실행 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f
```

### 2. MySQL 초기화 확인
```bash
# MySQL 접속 테스트
docker exec -it shopping-mysql mysql -uroot -proot -e "SHOW DATABASES;"

# shop 데이터베이스 확인
docker exec -it shopping-mysql mysql -uroot -proot -e "USE shop; SHOW TABLES;"
```

### 3. Kafka 토픽 확인
```bash
# Kafka 토픽 목록 확인
docker exec -it shopping-kafka kafka-topics --list --bootstrap-server localhost:9092
```

## 애플리케이션 실행

### 방법 1: 전체 서비스 빌드 후 실행

```bash
# 1. Common 모듈 먼저 빌드 (Proto 파일 컴파일)
./gradlew :common:build

# 2. 전체 프로젝트 빌드
./gradlew clean build

# 3. 각 서비스 실행 (별도 터미널에서)
./gradlew :member-service:bootRun
./gradlew :product-service:bootRun
./gradlew :point-service:bootRun
./gradlew :order-service:bootRun
./gradlew :payment-service:bootRun
```

### 방법 2: Docker로 전체 서비스 실행

```bash
# 1. 프로젝트 빌드
./gradlew clean build

# 2. Docker 이미지 빌드
docker-compose -f docker-compose-all.yml build

# 3. 전체 서비스 실행
docker-compose -f docker-compose-all.yml up -d

# 4. 로그 확인
docker-compose -f docker-compose-all.yml logs -f
```

### 방법 3: IntelliJ IDEA에서 실행

1. 프로젝트를 IntelliJ IDEA로 열기
2. Gradle 프로젝트로 Import
3. 각 서비스의 Application 클래스 실행:
   - `MemberServiceApplication`
   - `ProductServiceApplication`
   - `PointServiceApplication`
   - `OrderServiceApplication`
   - `PaymentServiceApplication`

## 서비스 실행 순서 (중요!)

서비스 간 의존성 때문에 다음 순서로 실행해야 합니다:

1. **Member Service** (8081) - 기본 회원 서비스
2. **Product Service** (8082) - 상품 관리 서비스
3. **Point Service** (8085) - 포인트 관리 서비스
4. **Order Service** (8083) - 주문 처리 서비스
5. **Payment Service** (8084) - 결제 처리 서비스

## 초기 데이터 설정

각 서비스가 시작되면 자동으로 테스트 데이터가 생성됩니다:

- **회원**: 3명의 테스트 회원
- **상품**: 4개의 캐시노트 포인트 충전권
- **포인트**: 각 회원에게 100,000 포인트 충전

## 서비스 상태 확인

### 1. Health Check
```bash
# 각 서비스 상태 확인
curl http://localhost:8081/api/members/hello
curl http://localhost:8082/api/products/hello
curl http://localhost:8083/api/orders/hello
curl http://localhost:8084/api/payments/hello
curl http://localhost:8085/api/points/hello
```

### 2. 테스트 데이터 확인
```bash
# 회원 목록 조회
curl http://localhost:8081/api/members

# 상품 목록 조회
curl http://localhost:8082/api/products

# 포인트 잔액 조회 (회원 ID: 1)
curl http://localhost:8085/api/points/balance/1
```

## 트러블슈팅

### 1. 포트 충돌 문제
```bash
# 사용 중인 포트 확인
lsof -i :8081 # Mac/Linux
netstat -ano | findstr :8081 # Windows

# 포트 변경이 필요한 경우 application.yml 수정
```

### 2. MySQL 연결 실패
```bash
# MySQL 컨테이너 상태 확인
docker ps | grep mysql

# MySQL 재시작
docker-compose restart mysql

# 연결 테스트
mysql -h 127.0.0.1 -P 3306 -u root -proot
```

### 3. Kafka 연결 실패
```bash
# Kafka 컨테이너 상태 확인
docker ps | grep kafka

# Kafka 재시작
docker-compose restart kafka

# Kafka 로그 확인
docker-compose logs kafka
```

### 4. gRPC 연결 실패
- 서비스 실행 순서 확인
- gRPC 포트 확인 (9090, 9091, 9093, 9094, 9095)
- 방화벽 설정 확인

### 5. 빌드 실패
```bash
# Gradle 캐시 삭제
./gradlew clean

# 의존성 새로고침
./gradlew --refresh-dependencies

# Common 모듈 먼저 빌드
./gradlew :common:build
```

## 종료 방법

### 1. 애플리케이션 종료
- 터미널에서 실행 중인 경우: `Ctrl + C`
- IntelliJ에서 실행 중인 경우: Stop 버튼 클릭

### 2. Docker 환경 종료
```bash
# 컨테이너 중지
docker-compose down

# 볼륨까지 삭제 (데이터 초기화)
docker-compose down -v
```

## 주의사항

1. **Java 버전**: 반드시 JDK 17 이상을 사용해야 합니다.
2. **메모리**: 전체 서비스 실행 시 최소 8GB RAM 권장
3. **포트**: 8081-8085, 9090-9095 포트가 사용 가능해야 합니다.
4. **시작 순서**: 서비스 간 의존성이 있으므로 순서를 지켜야 합니다.

## 도움말

문제가 발생하면 다음을 확인하세요:
1. 모든 Docker 컨테이너가 실행 중인지
2. 각 서비스의 로그에 에러가 없는지
3. 필요한 포트가 사용 가능한지
4. Java 버전이 17 이상인지
