# API 명세

## 개요

캐시노트 마켓 주문 서비스의 REST API 명세입니다. 모든 API는 JSON 형식으로 요청과 응답을 처리합니다.

## 공통 사항

### Base URL
- Member Service: `http://localhost:8081`
- Product Service: `http://localhost:8082`
- Order Service: `http://localhost:8083`
- Payment Service: `http://localhost:8084`
- Point Service: `http://localhost:8085`

### Headers
```
Content-Type: application/json
```

### 공통 에러 응답
```json
{
  "error": "에러 메시지",
  "status": "FAILED",
  "timestamp": "2024-01-20T10:00:00"
}
```

---

## 1. Member Service (회원 서비스)

### 1.1 회원 생성
- **URL**: `POST /api/members`
- **설명**: 새로운 회원을 생성합니다.

#### Request Body
```json
{
  "email": "test@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678"
}
```

#### Response (201 Created)
```json
{
  "id": 1,
  "email": "test@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "point": 0
}
```

### 1.2 회원 목록 조회
- **URL**: `GET /api/members`
- **설명**: 전체 회원 목록을 조회합니다.

#### Response (200 OK)
```json
[
  {
    "id": 1,
    "email": "test1@example.com",
    "name": "테스트1",
    "phoneNumber": "010-1111-1111",
    "point": 100000
  },
  {
    "id": 2,
    "email": "test2@example.com",
    "name": "테스트2",
    "phoneNumber": "010-2222-2222",
    "point": 100000
  }
]
```

### 1.3 회원 조회
- **URL**: `GET /api/members/{memberId}`
- **설명**: 특정 회원 정보를 조회합니다.

#### Response (200 OK)
```json
{
  "id": 1,
  "email": "test@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "point": 50000
}
```

---

## 2. Product Service (상품 서비스)

### 2.1 상품 생성
- **URL**: `POST /api/products`
- **설명**: 새로운 상품을 등록합니다.

#### Request Body
```json
{
  "name": "캐시노트 포인트 충전권 10000원",
  "price": 10000,
  "stock": 100,
  "sellerId": "cashnote"
}
```

#### Response (201 Created)
```json
{
  "id": 1,
  "name": "캐시노트 포인트 충전권 10000원",
  "price": 10000,
  "stock": 100,
  "sellerId": "cashnote"
}
```

### 2.2 상품 목록 조회
- **URL**: `GET /api/products`
- **설명**: 전체 상품 목록을 조회합니다.

#### Response (200 OK)
```json
[
  {
    "id": 1,
    "name": "캐시노트 포인트 충전권 1000원",
    "price": 1000,
    "stock": 100,
    "sellerId": "cashnote"
  },
  {
    "id": 2,
    "name": "캐시노트 포인트 충전권 5000원",
    "price": 5000,
    "stock": 50,
    "sellerId": "cashnote"
  }
]
```

### 2.3 상품 조회
- **URL**: `GET /api/products/{productId}`
- **설명**: 특정 상품 정보를 조회합니다.

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "캐시노트 포인트 충전권 10000원",
  "price": 10000,
  "stock": 95,
  "sellerId": "cashnote"
}
```

### 2.4 재고 수정
- **URL**: `PATCH /api/products/{productId}/stock`
- **설명**: 상품 재고를 수정합니다.

#### Request Body
```json
{
  "quantityChange": -5
}
```

#### Response (200 OK)
```json
{
  "id": 1,
  "name": "캐시노트 포인트 충전권 10000원",
  "price": 10000,
  "stock": 95,
  "sellerId": "cashnote"
}
```

---

## 3. Point Service (포인트 서비스)

### 3.1 포인트 충전
- **URL**: `POST /api/points/charge`
- **설명**: 회원의 포인트를 충전합니다.

#### Request Body
```json
{
  "memberId": 1,
  "amount": 50000,
  "description": "포인트 충전"
}
```

#### Response (200 OK)
```json
{
  "memberId": 1,
  "balance": 150000,
  "transaction": {
    "id": 1,
    "type": "CHARGE",
    "amount": 50000,
    "description": "포인트 충전",
    "createdAt": "2024-01-20T10:00:00"
  }
}
```

### 3.2 포인트 잔액 조회
- **URL**: `GET /api/points/balance/{memberId}`
- **설명**: 회원의 포인트 잔액을 조회합니다.

#### Response (200 OK)
```json
{
  "memberId": 1,
  "balance": 150000,
  "lastUpdated": "2024-01-20T10:00:00"
}
```

### 3.3 포인트 사용
- **URL**: `POST /api/points/use`
- **설명**: 포인트를 사용합니다.

#### Request Body
```json
{
  "memberId": 1,
  "amount": 10000,
  "orderId": 12345,
  "description": "주문 결제"
}
```

#### Response (200 OK)
```json
{
  "memberId": 1,
  "balance": 140000,
  "transaction": {
    "id": 2,
    "type": "USE",
    "amount": -10000,
    "orderId": 12345,
    "description": "주문 결제",
    "createdAt": "2024-01-20T10:05:00"
  }
}
```

### 3.4 포인트 환불
- **URL**: `POST /api/points/refund`
- **설명**: 사용한 포인트를 환불합니다.

#### Request Body
```json
{
  "memberId": 1,
  "amount": 10000,
  "orderId": 12345,
  "description": "주문 취소 환불"
}
```

#### Response (200 OK)
```json
{
  "memberId": 1,
  "balance": 150000,
  "transaction": {
    "id": 3,
    "type": "REFUND",
    "amount": 10000,
    "orderId": 12345,
    "description": "주문 취소 환불",
    "createdAt": "2024-01-20T10:10:00"
  }
}
```

### 3.5 포인트 거래 내역 조회
- **URL**: `GET /api/points/transactions/{memberId}`
- **설명**: 회원의 포인트 거래 내역을 조회합니다.

#### Query Parameters
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 20)

#### Response (200 OK)
```json
{
  "memberId": 1,
  "transactions": [
    {
      "id": 3,
      "type": "REFUND",
      "amount": 10000,
      "balance": 150000,
      "orderId": 12345,
      "description": "주문 취소 환불",
      "createdAt": "2024-01-20T10:10:00"
    },
    {
      "id": 2,
      "type": "USE",
      "amount": -10000,
      "balance": 140000,
      "orderId": 12345,
      "description": "주문 결제",
      "createdAt": "2024-01-20T10:05:00"
    },
    {
      "id": 1,
      "type": "CHARGE",
      "amount": 50000,
      "balance": 150000,
      "description": "포인트 충전",
      "createdAt": "2024-01-20T10:00:00"
    }
  ],
  "totalElements": 3,
  "totalPages": 1,
  "currentPage": 0
}
```

---

## 4. Order Service (주문 서비스)

### 4.1 주문 생성 (단일 결제)
- **URL**: `POST /api/orders`
- **설명**: 새로운 주문을 생성합니다.

#### Request Body - PG 결제
```json
{
  "memberId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "paymentMethod": "PG_KPN"
}
```

#### Request Body - 포인트 결제
```json
{
  "memberId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 1
    }
  ],
  "paymentMethod": "POINT"
}
```

#### Request Body - BNPL 결제
```json
{
  "memberId": 1,
  "items": [
    {
      "productId": 2,
      "quantity": 1
    }
  ],
  "paymentMethod": "BNPL"
}
```

#### Response (201 Created)
```json
{
  "orderId": 12345,
  "status": "COMPLETED",
  "totalAmount": 2000,
  "paymentMethod": "PG_KPN",
  "items": [
    {
      "productId": 1,
      "productName": "캐시노트 포인트 충전권 1000원",
      "price": 1000,
      "quantity": 2
    }
  ],
  "paymentId": 67890,
  "createdAt": "2024-01-20T10:00:00"
}
```

### 4.2 주문 생성 (복합 결제)
- **URL**: `POST /api/orders`
- **설명**: 복합 결제로 주문을 생성합니다.

#### Request Body - PG + 포인트
```json
{
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
}
```

#### Request Body - PG + 쿠폰
```json
{
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
}
```

#### Response (201 Created)
```json
{
  "orderId": 12346,
  "status": "COMPLETED",
  "totalAmount": 50000,
  "paymentMethod": "COMPOSITE",
  "items": [
    {
      "productId": 4,
      "productName": "캐시노트 포인트 충전권 50000원",
      "price": 50000,
      "quantity": 1
    }
  ],
  "paymentId": 67891,
  "paymentDetails": [
    {
      "method": "COUPON",
      "amount": 10000,
      "status": "SUCCESS"
    },
    {
      "method": "PG_KPN",
      "amount": 40000,
      "status": "SUCCESS"
    }
  ],
  "createdAt": "2024-01-20T10:00:00"
}
```

### 4.3 주문 조회
- **URL**: `GET /api/orders/{orderId}`
- **설명**: 주문 정보를 조회합니다.

#### Response (200 OK)
```json
{
  "orderId": 12345,
  "memberId": 1,
  "status": "COMPLETED",
  "totalAmount": 2000,
  "paymentMethod": "PG_KPN",
  "items": [
    {
      "productId": 1,
      "productName": "캐시노트 포인트 충전권 1000원",
      "price": 1000,
      "quantity": 2
    }
  ],
  "paymentId": 67890,
  "createdAt": "2024-01-20T10:00:00",
  "updatedAt": "2024-01-20T10:00:30"
}
```

### 4.4 회원별 주문 목록 조회
- **URL**: `GET /api/orders/member/{memberId}`
- **설명**: 특정 회원의 주문 목록을 조회합니다.

#### Query Parameters
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 20)
- `status`: 주문 상태 필터 (PENDING, PAID, COMPLETED, CANCELLED)

#### Response (200 OK)
```json
{
  "orders": [
    {
      "orderId": 12346,
      "status": "COMPLETED",
      "totalAmount": 50000,
      "paymentMethod": "COMPOSITE",
      "itemCount": 1,
      "createdAt": "2024-01-20T10:05:00"
    },
    {
      "orderId": 12345,
      "status": "COMPLETED",
      "totalAmount": 2000,
      "paymentMethod": "PG_KPN",
      "itemCount": 1,
      "createdAt": "2024-01-20T10:00:00"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "currentPage": 0
}
```

### 4.5 주문 취소
- **URL**: `POST /api/orders/{orderId}/cancel`
- **설명**: 주문을 취소합니다.

#### Request Body
```json
{
  "reason": "단순 변심"
}
```

#### Response (200 OK)
```json
{
  "orderId": 12345,
  "status": "CANCELLED",
  "cancelledAt": "2024-01-20T11:00:00",
  "refundAmount": 2000,
  "refundStatus": "COMPLETED"
}
```

---

## 5. Payment Service (결제 서비스)

### 5.1 결제 처리
- **URL**: `POST /api/payments`
- **설명**: 결제를 처리합니다. (주로 Order Service에서 내부 호출)

#### Request Body
```json
{
  "orderId": 12345,
  "memberId": 1,
  "totalAmount": 10000,
  "paymentMethod": "PG_KPN",
  "metadata": {
    "pg_key": "test_key"
  }
}
```

#### Response (200 OK)
```json
{
  "paymentId": 67890,
  "orderId": 12345,
  "status": "SUCCESS",
  "paymentMethod": "PG_KPN",
  "amount": 10000,
  "transactionId": "TXN_123456",
  "processedAt": "2024-01-20T10:00:00"
}
```

### 5.2 결제 조회
- **URL**: `GET /api/payments/{paymentId}`
- **설명**: 결제 정보를 조회합니다.

#### Response (200 OK)
```json
{
  "paymentId": 67890,
  "orderId": 12345,
  "memberId": 1,
  "status": "SUCCESS",
  "paymentMethod": "PG_KPN",
  "totalAmount": 10000,
  "details": [
    {
      "method": "PG_KPN",
      "amount": 10000,
      "status": "SUCCESS",
      "transactionId": "TXN_123456"
    }
  ],
  "createdAt": "2024-01-20T10:00:00",
  "updatedAt": "2024-01-20T10:00:00"
}
```

### 5.3 결제 환불
- **URL**: `POST /api/payments/{paymentId}/refund`
- **설명**: 결제를 환불합니다.

#### Request Body
```json
{
  "amount": 10000,
  "reason": "주문 취소"
}
```

#### Response (200 OK)
```json
{
  "refundId": 78901,
  "paymentId": 67890,
  "amount": 10000,
  "status": "COMPLETED",
  "reason": "주문 취소",
  "refundedAt": "2024-01-20T11:00:00"
}
```

---

## 에러 코드

### HTTP Status Codes
- `200 OK`: 성공
- `201 Created`: 리소스 생성 성공
- `400 Bad Request`: 잘못된 요청
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스를 찾을 수 없음
- `409 Conflict`: 충돌 (예: 재고 부족)
- `500 Internal Server Error`: 서버 오류

### 비즈니스 에러 코드

#### Order Service
- `ORDER_001`: 회원을 찾을 수 없습니다
- `ORDER_002`: 상품을 찾을 수 없습니다
- `ORDER_003`: 재고가 부족합니다
- `ORDER_004`: 잘못된 결제 방법입니다
- `ORDER_005`: 포인트가 부족합니다
- `ORDER_006`: 쿠폰은 단독 결제가 불가능합니다
- `ORDER_007`: BNPL은 복합 결제가 불가능합니다
- `ORDER_008`: 주문을 찾을 수 없습니다
- `ORDER_009`: 취소 불가능한 상태입니다

#### Payment Service
- `PAY_001`: 결제 처리 실패
- `PAY_002`: PG 승인 실패
- `PAY_003`: BNPL 신용 평가 실패
- `PAY_004`: 이미 처리된 결제입니다
- `PAY_005`: 환불 처리 실패
- `PAY_006`: 환불 가능 기간이 지났습니다

#### Point Service
- `POINT_001`: 포인트 잔액이 부족합니다
- `POINT_002`: 유효하지 않은 포인트 금액입니다
- `POINT_003`: 회원을 찾을 수 없습니다
- `POINT_004`: 중복된 거래입니다

### 에러 응답 예시
```json
{
  "error": "재고가 부족합니다",
  "code": "ORDER_003",
  "details": {
    "productId": 1,
    "requestedQuantity": 10,
    "availableStock": 5
  },
  "timestamp": "2024-01-20T10:00:00"
}
```

---

## 테스트 시나리오

### 1. 정상 케이스

#### 1.1 PG 단독 결제
```bash
# 1. 회원 생성
curl -X POST http://localhost:8081/api/members \
  -H "Content-Type: application/json" \
  -d '{"email":"pg.test@example.com","name":"PG테스트","phoneNumber":"010-1111-1111"}'

# 2. 주문 생성
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"items":[{"productId":1,"quantity":2}],"paymentMethod":"PG_KPN"}'
```

#### 1.2 포인트 단독 결제
```bash
# 1. 포인트 충전
curl -X POST http://localhost:8085/api/points/charge \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"amount":50000,"description":"테스트 충전"}'

# 2. 포인트로 주문
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"items":[{"productId":2,"quantity":1}],"paymentMethod":"POINT"}'
```

#### 1.3 복합 결제
```bash
# PG + 포인트 복합 결제
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "items": [{"productId": 3, "quantity": 1}],
    "compositePayment": {
      "details": [
        {"method": "POINT", "amount": 5000},
        {"method": "PG_KPN", "amount": 5000}
      ]
    }
  }'
```

### 2. 실패 케이스

#### 2.1 포인트 부족
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"items":[{"productId":4,"quantity":10}],"paymentMethod":"POINT"}'
```

#### 2.2 쿠폰 단독 결제 시도
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"items":[{"productId":1,"quantity":1}],"paymentMethod":"COUPON"}'
```

#### 2.3 BNPL 복합 결제 시도
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "items": [{"productId": 2, "quantity": 1}],
    "compositePayment": {
      "details": [
        {"method": "BNPL", "amount": 3000},
        {"method": "POINT", "amount": 2000}
      ]
    }
  }'
```

---

## 추가 정보

### Rate Limiting
- 모든 API는 분당 1000회로 제한됩니다.
- 초과 시 `429 Too Many Requests` 응답을 받습니다.

### 인증
- 현재 버전에서는 인증이 구현되어 있지 않습니다.
- 실제 운영 환경에서는 JWT 토큰 기반 인증을 추가해야 합니다.

### API 버전 관리
- 현재 버전: v1
- 향후 버전 업그레이드 시 URL에 버전을 포함할 예정입니다.
  - 예: `/api/v2/orders`
