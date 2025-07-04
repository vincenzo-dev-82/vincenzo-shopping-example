# 캐시노트 마켓 API 명세서

## 목차
1. [개요](#개요)
2. [공통 규격](#공통-규격)
3. [Member Service API](#member-service-api)
4. [Product Service API](#product-service-api)
5. [Order Service API](#order-service-api)
6. [Payment Service API](#payment-service-api)
7. [에러 코드](#에러-코드)
8. [gRPC API](#grpc-api)

## 개요

이 문서는 캐시노트 마켓의 REST API 및 gRPC API 명세를 정의합니다.

### API 버전
- 현재 버전: v1
- Base URL 형식: `http://{service-host}:{port}/api/{resource}`

### 인증
현재는 인증 없이 사용 가능 (추후 JWT 기반 인증 추가 예정)

## 공통 규격

### 요청 헤더
```
Content-Type: application/json
Accept: application/json
```

### 응답 형식
모든 API 응답은 다음과 같은 형식을 따릅니다:

#### 성공 응답
```json
{
  "status": "success",
  "data": { ... },
  "timestamp": "2024-01-20T10:30:00Z"
}
```

#### 에러 응답
```json
{
  "status": "error",
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지",
    "details": { ... }
  },
  "timestamp": "2024-01-20T10:30:00Z"
}
```

### HTTP 상태 코드
- `200 OK`: 성공
- `201 Created`: 리소스 생성 성공
- `400 Bad Request`: 잘못된 요청
- `404 Not Found`: 리소스를 찾을 수 없음
- `500 Internal Server Error`: 서버 에러

## Member Service API

### Base URL
`http://localhost:8081/api/members`

### 1. 회원 생성
회원을 새로 등록합니다.

**Endpoint**
```
POST /api/members
```

**Request Body**
```json
{
  "email": "user@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678"
}
```

**Response**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "point": 0
}
```

**Validation Rules**
- `email`: 필수, 이메일 형식, 중복 불가
- `name`: 필수, 2-50자
- `phoneNumber`: 필수, 한국 휴대폰 번호 형식

### 2. 회원 조회
회원 정보를 조회합니다.

**Endpoint**
```
GET /api/members/{memberId}
```

**Path Parameters**
- `memberId`: 회원 ID (Long)

**Response**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "point": 50000
}
```

### 3. 회원 포인트 조회
회원의 현재 포인트를 조회합니다.

**Endpoint**
```
GET /api/members/{memberId}/point
```

**Response**
```json
{
  "memberId": 1,
  "currentPoint": 50000,
  "lastUpdated": "2024-01-20T10:30:00Z"
}
```

### 4. 헬스 체크
서비스 상태를 확인합니다.

**Endpoint**
```
GET /api/members/hello
```

**Response**
```
Hello from Member Service!
```

## Product Service API

### Base URL
`http://localhost:8082/api/products`

### 1. 상품 생성
새로운 상품을 등록합니다.

**Endpoint**
```
POST /api/products
```

**Request Body**
```json
{
  "name": "캐시노트 포인트 충전권 10000원",
  "price": 10000,
  "stock": 100,
  "sellerId": "cashnote"
}
```

**Response**
```json
{
  "id": 1,
  "name": "캐시노트 포인트 충전권 10000원",
  "price": 10000,
  "stock": 100,
  "sellerId": "cashnote"
}
```

**Validation Rules**
- `name`: 필수, 1-200자
- `price`: 필수, 0 이상
- `stock`: 필수, 0 이상
- `sellerId`: 필수

### 2. 상품 조회
특정 상품 정보를 조회합니다.

**Endpoint**
```
GET /api/products/{productId}
```

**Path Parameters**
- `productId`: 상품 ID (Long)

**Response**
```json
{
  "id": 1,
  "name": "캐시노트 포인트 충전권 10000원",
  "price": 10000,
  "stock": 95,
  "sellerId": "cashnote"
}
```

### 3. 전체 상품 목록 조회
모든 상품을 조회합니다.

**Endpoint**
```
GET /api/products
```

**Response**
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
특정 판매자의 상품을 조회합니다.

**Endpoint**
```
GET /api/products/seller/{sellerId}
```

**Path Parameters**
- `sellerId`: 판매자 ID (String)

**Response**
```json
[
  {
    "id": 1,
    "name": "캐시노트 포인트 충전권 1000원",
    "price": 1000,
    "stock": 100,
    "sellerId": "cashnote"
  }
]
```

### 5. 재고 수정
상품의 재고를 수정합니다.

**Endpoint**
```
PATCH /api/products/{productId}/stock
```

**Request Body**
```json
{
  "quantityChange": -5
}
```

**Response**
```json
{
  "id": 1,
  "name": "캐시노트 포인트 충전권 10000원",
  "price": 10000,
  "stock": 95,
  "sellerId": "cashnote"
}
```

**Notes**
- `quantityChange`: 양수면 증가, 음수면 감소
- 재고가 0 미만이 되는 경우 에러 발생

## Order Service API

### Base URL
`http://localhost:8083/api/orders`

### 1. 주문 생성
새로운 주문을 생성합니다.

**Endpoint**
```
POST /api/orders
```

**Request Body - 단일 결제**
```json
{
  "memberId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ],
  "paymentMethod": "PG_KPN"
}
```

**Request Body - 복합 결제**
```json
{
  "memberId": 1,
  "items": [
    {
      "productId": 3,
      "quantity": 1
    }
  ],
  "paymentMethod": "COMPOSITE"
}
```

**Response - 성공**
```json
{
  "orderId": 1001,
  "status": "PENDING",
  "totalAmount": 15000,
  "items": [
    {
      "productId": 1,
      "productName": "캐시노트 포인트 충전권 5000원",
      "price": 5000,
      "quantity": 2
    },
    {
      "productId": 2,
      "productName": "캐시노트 포인트 충전권 5000원",
      "price": 5000,
      "quantity": 1
    }
  ]
}
```

**Response - 실패**
```json
{
  "error": "재고가 부족합니다. 상품: 캐시노트 포인트 충전권 10000원, 재고: 0, 요청수량: 2",
  "status": "FAILED"
}
```

**Payment Methods**
- `PG_KPN`: PG 결제
- `CASHNOTE_POINT`: 캐시노트 포인트
- `BNPL`: 후불결제
- `COMPOSITE`: 복합결제

**Validation Rules**
- `memberId`: 필수, 존재하는 회원
- `items`: 필수, 최소 1개 이상
- `quantity`: 각 아이템당 1개 이상

### 2. gRPC 통신 테스트 - 회원 조회
Order Service에서 Member Service를 gRPC로 호출합니다.

**Endpoint**
```
GET /api/orders/test-grpc/member/{memberId}
```

**Response**
```json
{
  "id": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "phoneNumber": "010-1234-5678",
  "point": 50000
}
```

### 3. gRPC 통신 테스트 - 상품 조회
Order Service에서 Product Service를 gRPC로 호출합니다.

**Endpoint**
```
GET /api/orders/test-grpc/product/{productId}
```

**Response**
```json
{
  "id": 1,
  "name": "캐시노트 포인트 충전권 10000원",
  "price": 10000,
  "stock": 95,
  "sellerId": "cashnote"
}
```

## Payment Service API

### Base URL
`http://localhost:8084/api/payments`

### 1. 결제 처리
주문에 대한 결제를 처리합니다.

**Endpoint**
```
POST /api/payments
```

**Request Body - 단일 결제 (PG)**
```json
{
  "orderId": 1001,
  "memberId": 1,
  "totalAmount": 50000,
  "paymentDetails": [
    {
      "method": "PG_KPN",
      "amount": 50000,
      "metadata": {
        "cardNumber": "****-****-****-1234"
      }
    }
  ]
}
```

**Request Body - 단일 결제 (포인트)**
```json
{
  "orderId": 1002,
  "memberId": 1,
  "totalAmount": 10000,
  "paymentDetails": [
    {
      "method": "CASHNOTE_POINT",
      "amount": 10000,
      "metadata": {}
    }
  ]
}
```

**Request Body - 복합 결제**
```json
{
  "orderId": 1003,
  "memberId": 1,
  "totalAmount": 100000,
  "paymentDetails": [
    {
      "method": "PG_KPN",
      "amount": 70000,
      "metadata": {}
    },
    {
      "method": "CASHNOTE_POINT",
      "amount": 20000,
      "metadata": {}
    },
    {
      "method": "COUPON",
      "amount": 10000,
      "metadata": {
        "couponCode": "WELCOME10"
      }
    }
  ]
}
```

**Response - 성공**
```json
{
  "paymentId": 2001,
  "orderId": 1001,
  "status": "COMPLETED",
  "totalAmount": 50000,
  "paymentMethod": "PG_KPN",
  "paymentDetails": [
    {
      "method": "PG_KPN",
      "amount": 50000,
      "status": "SUCCESS",
      "transactionId": "PG_550e8400-e29b-41d4-a716-446655440000"
    }
  ],
  "createdAt": "2024-01-20T10:30:00Z",
  "completedAt": "2024-01-20T10:30:05Z"
}
```

**Response - 실패**
```json
{
  "error": {
    "code": "PAYMENT_FAILED",
    "message": "결제 처리 실패: PG 결제 실패: 카드 잔액 부족"
  }
}
```

### 2. 결제 조회 (결제 ID)
결제 ID로 결제 정보를 조회합니다.

**Endpoint**
```
GET /api/payments/{paymentId}
```

**Response**
```json
{
  "paymentId": 2001,
  "orderId": 1001,
  "memberId": 1,
  "totalAmount": 50000,
  "paymentMethod": "PG_KPN",
  "status": "COMPLETED",
  "paymentDetails": [
    {
      "method": "PG_KPN",
      "amount": 50000,
      "status": "SUCCESS",
      "transactionId": "PG_550e8400-e29b-41d4-a716-446655440000"
    }
  ],
  "createdAt": "2024-01-20T10:30:00Z",
  "completedAt": "2024-01-20T10:30:05Z"
}
```

### 3. 결제 조회 (주문 ID)
주문 ID로 결제 정보를 조회합니다.

**Endpoint**
```
GET /api/payments/order/{orderId}
```

**Response**
위와 동일한 형식

### 4. 결제 취소
결제를 취소합니다.

**Endpoint**
```
POST /api/payments/{paymentId}/cancel
```

**Request Body**
```json
{
  "reason": "고객 요청으로 인한 취소"
}
```

**Response**
```json
{
  "paymentId": 2001,
  "status": "CANCELLED",
  "cancelledAt": "2024-01-20T11:00:00Z",
  "cancelReason": "고객 요청으로 인한 취소"
}
```

## 에러 코드

### 공통 에러 코드
| 코드 | 설명 | HTTP 상태 |
|------|------|-----------|
| `INVALID_REQUEST` | 잘못된 요청 형식 | 400 |
| `RESOURCE_NOT_FOUND` | 리소스를 찾을 수 없음 | 404 |
| `INTERNAL_ERROR` | 서버 내부 오류 | 500 |
| `SERVICE_UNAVAILABLE` | 서비스 일시 중단 | 503 |

### Member Service 에러 코드
| 코드 | 설명 | HTTP 상태 |
|------|------|-----------|
| `MEMBER_NOT_FOUND` | 회원을 찾을 수 없음 | 404 |
| `EMAIL_ALREADY_EXISTS` | 이미 존재하는 이메일 | 409 |
| `INVALID_PHONE_NUMBER` | 잘못된 전화번호 형식 | 400 |

### Product Service 에러 코드
| 코드 | 설명 | HTTP 상태 |
|------|------|-----------|
| `PRODUCT_NOT_FOUND` | 상품을 찾을 수 없음 | 404 |
| `INSUFFICIENT_STOCK` | 재고 부족 | 400 |
| `INVALID_PRICE` | 잘못된 가격 | 400 |

### Order Service 에러 코드
| 코드 | 설명 | HTTP 상태 |
|------|------|-----------|
| `ORDER_NOT_FOUND` | 주문을 찾을 수 없음 | 404 |
| `INVALID_ORDER_STATUS` | 잘못된 주문 상태 | 400 |
| `MEMBER_SERVICE_ERROR` | 회원 서비스 오류 | 502 |
| `PRODUCT_SERVICE_ERROR` | 상품 서비스 오류 | 502 |

### Payment Service 에러 코드
| 코드 | 설명 | HTTP 상태 |
|------|------|-----------|
| `PAYMENT_NOT_FOUND` | 결제 정보를 찾을 수 없음 | 404 |
| `PAYMENT_FAILED` | 결제 실패 | 400 |
| `INVALID_PAYMENT_METHOD` | 잘못된 결제 수단 | 400 |
| `INSUFFICIENT_POINT` | 포인트 부족 | 400 |
| `INVALID_COUPON` | 유효하지 않은 쿠폰 | 400 |
| `COMPOSITE_PAYMENT_ERROR` | 복합결제 오류 | 400 |

## gRPC API

### Member Service gRPC

**Proto 파일**: `common/src/main/proto/member.proto`

#### 1. GetMember
회원 정보를 조회합니다.

**Request**
```protobuf
message GetMemberRequest {
  int64 member_id = 1;
}
```

**Response**
```protobuf
message MemberResponse {
  int64 id = 1;
  string email = 2;
  string name = 3;
  string phone_number = 4;
  int32 point = 5;
}
```

#### 2. UpdateMemberPoint
회원 포인트를 업데이트합니다.

**Request**
```protobuf
message UpdateMemberPointRequest {
  int64 member_id = 1;
  int32 point_change = 2;
}
```

**Response**
```protobuf
message MemberResponse {
  // 위와 동일
}
```

### Product Service gRPC

**Proto 파일**: `common/src/main/proto/product.proto`

#### 1. GetProduct
상품 정보를 조회합니다.

**Request**
```protobuf
message GetProductRequest {
  int64 product_id = 1;
}
```

**Response**
```protobuf
message ProductResponse {
  int64 id = 1;
  string name = 2;
  int64 price = 3;
  int32 stock = 4;
  string seller_id = 5;
}
```

#### 2. GetProductList
여러 상품 정보를 조회합니다.

**Request**
```protobuf
message GetProductListRequest {
  repeated int64 product_ids = 1;
}
```

**Response**
```protobuf
message ProductListResponse {
  repeated ProductResponse products = 1;
}
```

#### 3. UpdateStock
상품 재고를 업데이트합니다.

**Request**
```protobuf
message UpdateStockRequest {
  int64 product_id = 1;
  int32 quantity_change = 2;
}
```

**Response**
```protobuf
message ProductResponse {
  // 위와 동일
}
```

## 사용 예시

### 전체 주문 플로우 예시

1. **회원 생성**
```bash
curl -X POST http://localhost:8081/api/members \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "테스트 사용자",
    "phoneNumber": "010-1234-5678"
  }'
```

2. **상품 조회**
```bash
curl http://localhost:8082/api/products
```

3. **주문 생성**
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

4. **결제 조회**
```bash
curl http://localhost:8084/api/payments/order/1001
```

### 복합결제 플로우 예시

1. **포인트 충전 (테스트용)**
```bash
# Payment Service 내부에서만 가능
# 실제로는 별도의 충전 API 필요
```

2. **복합결제 주문**
```bash
curl -X POST http://localhost:8083/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": 1,
    "items": [
      {
        "productId": 3,
        "quantity": 1
      }
    ],
    "paymentMethod": "COMPOSITE"
  }'
```

## 주의사항

1. **결제 수단별 제약사항**
   - 쿠폰은 단독 결제 불가능
   - BNPL은 복합결제에 포함 불가능
   - 복합결제는 반드시 PG가 메인 결제수단이어야 함

2. **트랜잭션 처리**
   - 주문 생성과 결제는 별도 트랜잭션으로 처리
   - 결제 실패 시 주문 상태는 FAILED로 변경
   - 복합결제에서 일부 실패 시 전체 롤백

3. **성능 고려사항**
   - gRPC를 통한 서비스 간 통신으로 지연시간 최소화
   - Kafka를 통한 비동기 이벤트 처리
   - 재고 차감은 동기적으로 처리하여 데이터 일관성 보장

---

최종 업데이트: 2024년 1월 20일
