#!/bin/bash

echo "=== EasyPay 송금 기능 테스트 ==="
echo

# 첫 번째 사용자 로그인
echo "1. 첫 번째 사용자 로그인 (송금자)"
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "01012345678",
    "password": "password123"
  }')

echo "로그인 응답: $LOGIN_RESPONSE"
JWT_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "JWT 토큰: $JWT_TOKEN"
echo

# 계좌 검증 테스트 - EasyPay 은행
echo "2. EasyPay 계좌 검증 테스트"
VERIFY_RESPONSE=$(curl -s -X POST http://localhost:8090/api/accounts/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "accountNumber": "EP0000000002",
    "bankName": "EasyPay"
  }')

echo "EasyPay 계좌 검증 응답: $VERIFY_RESPONSE"
echo

# 계좌 검증 테스트 - 카카오뱅크 (임시 데이터)
echo "3. 카카오뱅크 계좌 검증 테스트"
VERIFY_KAKAO_RESPONSE=$(curl -s -X POST http://localhost:8090/api/accounts/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "accountNumber": "3333-01-1234567",
    "bankName": "카카오뱅크"
  }')

echo "카카오뱅크 계좌 검증 응답: $VERIFY_KAKAO_RESPONSE"
echo

# 송금 테스트 (빠른 금액 선택 - 1만원)
echo "4. 송금 테스트 (빠른 선택 금액: 10,000원)"
TRANSFER_RESPONSE=$(curl -s -X POST http://localhost:8090/api/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "receiverAccountNumber": "EP0000000002",
    "amount": 10000,
    "memo": "빠른 금액 선택 테스트 - 1만원"
  }')

echo "송금 응답: $TRANSFER_RESPONSE"
echo

# 송금 테스트 (빠른 금액 선택 - 100만원)
echo "5. 송금 테스트 (빠른 선택 금액: 1,000,000원)"
TRANSFER_RESPONSE2=$(curl -s -X POST http://localhost:8090/api/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "receiverAccountNumber": "EP0000000002",
    "amount": 1000000,
    "memo": "빠른 금액 선택 테스트 - 100만원"
  }')

echo "송금 응답: $TRANSFER_RESPONSE2"
echo

echo "=== 테스트 완료 ==="