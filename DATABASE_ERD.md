# 데이터베이스 ERD

## ERD Diagram

```mermaid
erDiagram
    members ||--o{ orders : places
    members ||--o{ point_transactions : has
    members ||--|| point_balances : has
    products ||--o{ order_items : contains
    orders ||--o{ order_items : contains
    orders ||--o{ payments : has
    payments ||--o{ payment_details : contains
    
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
        bigint total_amount
        varchar payment_method
        varchar status
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
        bigint total_amount
        varchar status
        timestamp created_at
        timestamp updated_at
    }
    
    payment_details {
        bigint id PK
        bigint payment_id FK
        varchar method
        bigint amount
        timestamp created_at
    }
    
    payment_detail_metadata {
        bigint payment_detail_id FK
        varchar metadata_key
        varchar metadata_value
    }
    
    point_balances {
        bigint id PK
        bigint member_id FK-UK
        bigint balance
        timestamp created_at
        timestamp updated_at
    }
    
    point_transactions {
        bigint id PK
        bigint member_id FK
        bigint amount
        varchar type
        varchar description
        varchar reference_id
        timestamp created_at
    }
```

## 테이블 설명

### 1. members (회원)
- 회원 기본 정보 관리
- email은 unique key로 중복 불가
- point 필드는 현재 보유 포인트

### 2. products (상품)
- 상품 정보 관리
- stock은 현재 재고 수량
- seller_id는 판매자 식별자

### 3. orders (주문)
- 주문 기본 정보
- payment_method: PG_KPN, CASHNOTE_POINT, BNPL, COMPOSITE
- status: PENDING, PAYMENT_PROCESSING, PAID, PAYMENT_FAILED, CANCELLED, COMPLETED

### 4. order_items (주문 항목)
- 주문에 포함된 상품 정보
- product_name과 price는 주문 시점의 정보를 저장 (히스토리 용도)

### 5. payments (결제)
- 결제 정보 관리
- 하나의 주문에 하나의 결제
- status: PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED

### 6. payment_details (결제 상세)
- 복합결제 시 각 결제 수단별 상세 정보
- method: 결제 수단 (PG_KPN, CASHNOTE_POINT, COUPON 등)

### 7. payment_detail_metadata
- 결제 상세의 추가 정보 (쿠폰 코드 등)
- key-value 형태로 저장

### 8. point_balances (포인트 잔액)
- 회원별 현재 포인트 잔액
- member_id는 unique key (회원당 하나의 잔액 레코드)

### 9. point_transactions (포인트 거래내역)
- 포인트 충전/사용 내역
- type: CHARGE(충전), USE(사용)
- reference_id: 관련 주문 번호 등

## 인덱스 전략

### 1. Primary Key Index
- 모든 테이블의 id 필드

### 2. Foreign Key Index
- 모든 FK 필드에 자동 생성

### 3. 추가 인덱스
- members.email (Unique Index)
- orders.member_id + created_at (복합 인덱스)
- order_items.order_id
- payment_details.payment_id
- point_transactions.member_id + created_at (복합 인덱스)
- point_balances.member_id (Unique Index)

## 주의사항

1. **데이터 일관성**: 현재 모든 서비스가 단일 DB를 사용하므로 FK 제약조건 활용 가능
2. **향후 분리**: 서비스별 DB 분리 시 FK 제약조건 제거 필요
3. **성능**: 대용량 처리 시 파티셔닝 고려 (orders, point_transactions)
4. **아카이빙**: 오래된 주문 및 거래내역은 별도 아카이브 테이블로 이동
