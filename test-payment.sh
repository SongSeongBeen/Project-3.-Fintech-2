#!/bin/bash

# 결제 테스트용 스크립트

echo "=== 결제 API 테스트 시작 ==="

# H2 콘솔을 통한 테스트 데이터 생성
echo "1. H2 콘솔에서 다음 SQL을 실행하세요:"
echo "   URL: http://localhost:8080/h2-console"
echo "   JDBC URL: jdbc:h2:mem:testdb"
echo "   User Name: sa"
echo "   Password: (비워두기)"
echo ""

echo "2. 테스트 데이터 삽입 SQL:"
echo "INSERT INTO members (phone_number, password, name, email, status, is_locked, login_failed_count, created_at, updated_at) VALUES"
echo "('010-9999-8888', '\$2a\$10\$dummypasswordhash', '결제테스터', 'payment@test.com', 'ACTIVE', false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);"
echo ""
echo "INSERT INTO accounts (member_id, account_number, balance, status, version, created_at, updated_at)"
echo "SELECT m.id, '9999888800001234', 100000.00, 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP"
echo "FROM members m WHERE m.phone_number = '010-9999-8888';"
echo ""

echo "3. 결제 API 테스트 (JWT 토큰 없이 403 응답 확인):"
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "MERCHANT001",
    "merchantName": "테스트 가맹점",
    "amount": 10000,
    "memo": "결제 테스트",
    "paymentMethod": "BALANCE"
  }' \
  -w "\nHTTP Status: %{http_code}\n"

echo ""
echo "4. 결제 컨트롤러 엔드포인트 확인:"
echo "   POST /api/payments - 결제 처리"
echo "   GET /api/payments - 결제 내역 조회"
echo "   GET /api/payments/{paymentId} - 결제 상세 조회"
echo "   POST /api/payments/{paymentId}/cancel - 결제 취소"
echo "   POST /api/payments/{paymentId}/refund - 결제 환불"

echo ""
echo "=== 결제 도메인 독립 테스트 ==="
./gradlew test --tests="fintech2.easypay.payment.PaymentStandaloneTest" --no-daemon

echo ""
echo "=== 결제 API 테스트 완료 ==="