# 데이터베이스 ERD

## ERD 다이어그램

```mermaid
erDiagram
    members ||--o{ orders : "places"
    members ||--o{ point_balances : "has"
    members ||--o{ point_transactions : "has"
    products ||--o{ order_items : "included in"
    orders ||--o{ order_items : "contains"
    orders ||--|| payments : "paid by"
    payments ||--o{ payment_details : "has"
    point_balances ||--o{ point_transactions : "tracks"

    members {
        bigint id PK
        varchar email UK
        varchar name
        varchar phone_number
        int point
        timestamp created_at
        timestamp updated_at
    }

    products {
        bigint id PK
        varchar name
        bigint price
        int stock
        varchar seller_id
        timestamp created_at
        timestamp updated_at
    }

    orders {
        bigint id PK
        bigint member_id FK
        varchar status
        bigint total_amount
        varchar payment_method
        timestamp created_at
        timestamp updated_at
    }

    order_items {
        bigint id PK
        bigint order_id FK
        bigint product_id FK
        varchar product_name
        bigint price
        int quantity
        timestamp created_at
    }

    payments {
        bigint id PK
        bigint order_id FK
        bigint member_id FK
        varchar status
        varchar payment_method
        bigint total_amount
        varchar transaction_id
        timestamp created_at
        timestamp updated_at
    }

    payment_details {
        bigint id PK
        bigint payment_id FK
        varchar method
        bigint amount
        varchar status
        varchar transaction_id
        text metadata
        timestamp created_at
    }

    point_balances {
        bigint id PK
        bigint member_id FK UK
        bigint balance
        timestamp last_updated
    }

    point_transactions {
        bigint id PK
        bigint member_id FK
        varchar type
        bigint amount
        bigint balance
        bigint order_id
        varchar description
        timestamp created_at
    }
```

## 테이블 설명

### 1. members (회원)
회원 정보를 저장하는 테이블입니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 회원 ID |
| email | VARCHAR(255) | UNIQUE, NOT NULL | 이메일 |
| name | VARCHAR(100) | NOT NULL | 이름 |
| phone_number | VARCHAR(20) | NOT NULL | 전화번호 |
| point | INT | DEFAULT 0 | 보유 포인트 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스:**
- PRIMARY KEY (id)
- UNIQUE KEY idx_members_email (email)
- INDEX idx_members_phone (phone_number)

### 2. products (상품)
상품 정보를 저장하는 테이블입니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 상품 ID |
| name | VARCHAR(255) | NOT NULL | 상품명 |
| price | BIGINT | NOT NULL | 가격 |
| stock | INT | NOT NULL, DEFAULT 0 | 재고 |
| seller_id | VARCHAR(100) | NOT NULL | 판매자 ID |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스:**
- PRIMARY KEY (id)
- INDEX idx_products_seller (seller_id)
- INDEX idx_products_name (name)

### 3. orders (주문)
주문 정보를 저장하는 테이블입니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 주문 ID |
| member_id | BIGINT | FK, NOT NULL | 회원 ID |
| status | VARCHAR(20) | NOT NULL | 주문 상태 (PENDING, PAID, CANCELLED, COMPLETED) |
| total_amount | BIGINT | NOT NULL | 총 금액 |
| payment_method | VARCHAR(20) | NOT NULL | 결제 방법 (PG_KPN, POINT, BNPL, COMPOSITE) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스:**
- PRIMARY KEY (id)
- INDEX idx_orders_member (member_id)
- INDEX idx_orders_status (status)
- INDEX idx_orders_created (created_at)

### 4. order_items (주문 상품)
주문에 포함된 상품 정보를 저장하는 테이블입니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 주문 상품 ID |
| order_id | BIGINT | FK, NOT NULL | 주문 ID |
| product_id | BIGINT | FK, NOT NULL | 상품 ID |
| product_name | VARCHAR(255) | NOT NULL | 상품명 (스냅샷) |
| price | BIGINT | NOT NULL | 단가 (스냅샷) |
| quantity | INT | NOT NULL | 수량 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |

**인덱스:**
- PRIMARY KEY (id)
- INDEX idx_order_items_order (order_id)
- INDEX idx_order_items_product (product_id)

### 5. payments (결제)
결제 정보를 저장하는 테이블입니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 결제 ID |
| order_id | BIGINT | FK, UNIQUE, NOT NULL | 주문 ID |
| member_id | BIGINT | FK, NOT NULL | 회원 ID |
| status | VARCHAR(20) | NOT NULL | 결제 상태 (PENDING, SUCCESS, FAILED, CANCELLED) |
| payment_method | VARCHAR(20) | NOT NULL | 결제 방법 |
| total_amount | BIGINT | NOT NULL | 총 결제 금액 |
| transaction_id | VARCHAR(100) | | 거래 ID |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정일시 |

**인덱스:**
- PRIMARY KEY (id)
- UNIQUE KEY idx_payments_order (order_id)
- INDEX idx_payments_member (member_id)
- INDEX idx_payments_status (status)
- INDEX idx_payments_transaction (transaction_id)

### 6. payment_details (결제 상세)
복합 결제 시 각 결제 수단별 상세 정보를 저장하는 테이블입니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 결제 상세 ID |
| payment_id | BIGINT | FK, NOT NULL | 결제 ID |
| method | VARCHAR(20) | NOT NULL | 결제 수단 (PG_KPN, POINT, COUPON, BNPL) |
| amount | BIGINT | NOT NULL | 금액 |
| status | VARCHAR(20) | NOT NULL | 상태 (SUCCESS, FAILED, CANCELLED) |
| transaction_id | VARCHAR(100) | | 거래 ID |
| metadata | TEXT | | 추가 정보 (JSON) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성일시 |

**인덱스:**
- PRIMARY KEY (id)
- INDEX idx_payment_details_payment (payment_id)
- INDEX idx_payment_details_method (method)

### 7. point_balances (포인트 잔액)
회원별 포인트 잔액을 저장하는 테이블입니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 잔액 ID |
| member_id | BIGINT | FK, UNIQUE, NOT NULL | 회원 ID |
| balance | BIGINT | NOT NULL, DEFAULT 0 | 잔액 |
| last_updated | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 최종 수정일시 |

**인덱스:**
- PRIMARY KEY (id)
- UNIQUE KEY idx_point_balances_member (member_id)

### 8. point_transactions (포인트 거래내역)
포인트 거래 내역을 저장하는 테이블입니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 거래 ID |
| member_id | BIGINT | FK, NOT NULL | 회원 ID |
| type | VARCHAR(20) | NOT NULL | 거래 유형 (CHARGE, USE, REFUND) |
| amount | BIGINT | NOT NULL | 거래 금액 (사용 시 음수) |
| balance | BIGINT | NOT NULL | 거래 후 잔액 |
| order_id | BIGINT | | 관련 주문 ID |
| description | VARCHAR(255) | | 거래 설명 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 거래일시 |

**인덱스:**
- PRIMARY KEY (id)
- INDEX idx_point_transactions_member (member_id)
- INDEX idx_point_transactions_order (order_id)
- INDEX idx_point_transactions_created (created_at)
- INDEX idx_point_transactions_type (type)

## 인덱스 전략

### 1. 기본 인덱스
- 모든 테이블의 Primary Key
- Foreign Key 컬럼
- UNIQUE 제약조건 컬럼

### 2. 조회 성능 개선
- 자주 검색되는 컬럼 (email, phone_number, status)
- 날짜 범위 검색 컬럼 (created_at)
- 조인에 사용되는 컬럼

### 3. 복합 인덱스
```sql
-- 주문 조회 최적화
CREATE INDEX idx_orders_member_status_created 
ON orders(member_id, status, created_at DESC);

-- 포인트 거래내역 조회 최적화
CREATE INDEX idx_point_transactions_member_created 
ON point_transactions(member_id, created_at DESC);

-- 결제 상태 조회 최적화
CREATE INDEX idx_payments_status_created 
ON payments(status, created_at DESC);
```

## 주의사항

### 1. 데이터 무결성
- 외래키 제약조건으로 참조 무결성 보장
- 트랜잭션을 통한 데이터 일관성 유지
- 비관적 락을 통한 동시성 제어 (재고, 포인트)

### 2. 성능 고려사항
- 대용량 테이블 파티셔닝 (orders, point_transactions)
- 읽기 전용 쿼리는 Read Replica 활용
- 자주 변경되지 않는 데이터는 캐싱

### 3. 확장성
- 샤딩 키: member_id
- 아카이빙: 1년 이상 된 주문 데이터
- 테이블 분리: 로그성 데이터는 별도 DB

### 4. 보안
- 개인정보 컬럼 암호화 (email, phone_number)
- 결제 정보 마스킹
- 접근 권한 최소화

## 마이그레이션 스크립트

### 초기 스키마 생성
```sql
-- Database 생성
CREATE DATABASE IF NOT EXISTS shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shop;

-- Members 테이블
CREATE TABLE members (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    point INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY idx_members_email (email),
    INDEX idx_members_phone (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 이하 다른 테이블들도 동일한 방식으로 생성...
```

### 샘플 데이터 입력
```sql
-- 테스트 회원 데이터
INSERT INTO members (email, name, phone_number, point) VALUES
('test1@example.com', '테스트1', '010-1111-1111', 0),
('test2@example.com', '테스트2', '010-2222-2222', 0),
('test3@example.com', '테스트3', '010-3333-3333', 0);

-- 테스트 상품 데이터
INSERT INTO products (name, price, stock, seller_id) VALUES
('캐시노트 포인트 충전권 1000원', 1000, 100, 'cashnote'),
('캐시노트 포인트 충전권 5000원', 5000, 50, 'cashnote'),
('캐시노트 포인트 충전권 10000원', 10000, 30, 'cashnote'),
('캐시노트 포인트 충전권 50000원', 50000, 10, 'cashnote');
```

## 향후 개선사항

### 1. 테이블 분리
- 주문 이력 테이블 (order_history)
- 결제 로그 테이블 (payment_logs)
- 쿠폰 관리 테이블 (coupons, coupon_uses)

### 2. 성능 최적화
- 파티셔닝 적용 (날짜 기준)
- 인덱스 리빌드 자동화
- 통계 정보 수집 자동화

### 3. 데이터 관리
- 아카이빙 정책 수립
- 백업/복구 전략
- 데이터 익명화 처리
