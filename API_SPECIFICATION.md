# API 명세서

## Member Service API

### 1. 회원 생성
- **Endpoint**: `POST /api/members`
- **Description**: 새로운 회원을 생성합니다.
- **Request Body**:
```json
{
  "email": "test@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678"
}
```
- **Response**:
```json
{
  "id": 1,
  "email": "test@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "point": 0
}
```

### 2. Hello World
- **Endpoint**: `GET /api/members/hello`
- **Response**: `Hello from Member Service!`

## Product Service API

### 1. 상품 생성
- **Endpoint**: `POST /api/products`
- **Description**: 새로운 상품을 등록합니다.
- **Request Body**:
```json
{
  "name": "캐시노트 포인트 충전권 10000원",
  "price": 10000,
  "stock": 100,
  "sellerId": "cashnote"
}
```
- **Response**:
```json
{
  "id": 1,
  "name": "캐시노트 포인트 충전권 10000원",
  "price": 10000,
  "stock": 100,
  "sellerId": "cashnote"
}
```

### 2. 상품 조회
- **Endpoint**: `GET /api/products/{productId}`
- **Response**:
```json
{
  "id": 1,
  "name": "캐시노트 포인트 충전권 10000원",
  "price": 10000,
  "stock": 100,
  "sellerId": "cashnote"
}
```

### 3. 전체 상품 목록
- **Endpoint**: `GET /api/products`
- **Response**:
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

### 4. 판매자별 상품 조회
- **Endpoint**: `GET /api/products/seller/{sellerId}`

### 5. 재고 업데이트
- **Endpoint**: `PATCH /api/products/{productId}/stock`
- **Request Body**:
```json
{
  "quantityChange": -5
}
```

## Order Service API

### 1. 주문 생성
- **Endpoint**: `POST /api/orders`
- **Description**: 새로운 주문을 생성합니다.
- **Request Body**:
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
- **Response (성공)**:
```json
{
  "orderId": 1234567890,
  "status": "PENDING",
  "totalAmount": 2000,
  "items": [
    {
      "productId": 1,
      "productName": "캐시노트 포인트 충전권 1000원",
      "price": 1000,
      "quantity": 2
    }
  ]
}
```
- **Response (실패)**:
```json
{
  "error": "재고가 부족합니다. 상품: 캐시노트 포인트 충전권 1000원, 재고: 10, 요청수량: 20",
  "status": "FAILED"
}
```

### 2. gRPC 테스트 - 회원 정보 조회
- **Endpoint**: `GET /api/orders/test-grpc/member/{memberId}`
- **Description**: gRPC를 통해 회원 정보를 조회합니다.
- **Response**:
```json
{
  "id": 1,
  "email": "test@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "point": 1000
}
```

### 3. gRPC 테스트 - 상품 정보 조회
- **Endpoint**: `GET /api/orders/test-grpc/product/{productId}`
- **Description**: gRPC를 통해 상품 정보를 조회합니다.

## 결제 방법 (PaymentMethod)

- **PG_KPN**: PG 결제 (메인 결제수단)
- **CASHNOTE_POINT**: 캐시노트 포인트
- **COUPON**: 쿠폰 (단독 결제 불가)
- **BNPL**: 외상결제 (단독 결제만 가능)
- **COMPOSITE**: 복합결제

## 주문 상태 (OrderStatus)

- **PENDING**: 주문 생성
- **PAYMENT_PROCESSING**: 결제 진행중
- **PAID**: 결제 완료
- **PAYMENT_FAILED**: 결제 실패
- **CANCELLED**: 주문 취소
- **COMPLETED**: 주문 완료

## 에러 코드

### 400 Bad Request
- 잘못된 요청 데이터
- 필수 파라미터 누락
- 잘못된 결제 방법

### 404 Not Found
- 존재하지 않는 회원
- 존재하지 않는 상품

### 409 Conflict
- 재고 부족
- 포인트 부족
- 쿠폰 단독 결제 시도
- BNPL 복합결제 시도

### 500 Internal Server Error
- 서버 내부 오류
- gRPC 통신 실패
- Kafka 메시지 발행 실패
