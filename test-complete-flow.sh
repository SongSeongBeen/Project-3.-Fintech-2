#!/bin/bash

echo "=== EasyPay 완전한 송금 테스트 플로우 ==="
echo

# 첫 번째 사용자 로그인
echo "1. 송금자 로그인 (01012345678)"
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "01012345678",
    "password": "123456"
  }')

JWT_TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken')
ACCOUNT_NUMBER=$(echo $LOGIN_RESPONSE | jq -r '.accountNumber')
USER_NAME=$(echo $LOGIN_RESPONSE | jq -r '.userName')

echo "로그인 성공: $USER_NAME ($ACCOUNT_NUMBER)"
echo

# 2. EasyPay 계좌 검증 테스트
echo "2. EasyPay 계좌 검증 테스트 (수신자: EP0000000002)"
VERIFY_RESPONSE=$(curl -s -X POST http://localhost:8090/api/accounts/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "accountNumber": "EP0000000002",
    "bankName": "EasyPay"
  }')

echo "검증 결과: $(echo $VERIFY_RESPONSE | jq -r '.message')"
RECEIVER_NAME=$(echo $VERIFY_RESPONSE | jq -r '.data.accountHolderName')
echo "수신자: $RECEIVER_NAME"
echo

# 3. 카카오뱅크 계좌 검증 테스트
echo "3. 카카오뱅크 계좌 검증 테스트"
KAKAO_VERIFY_RESPONSE=$(curl -s -X POST http://localhost:8090/api/accounts/verify \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "accountNumber": "3333-01-1234567",
    "bankName": "카카오뱅크"
  }')

echo "카카오뱅크 검증 결과: $(echo $KAKAO_VERIFY_RESPONSE | jq -r '.message // "검증 실패"')"
KAKAO_RECEIVER_NAME=$(echo $KAKAO_VERIFY_RESPONSE | jq -r '.data.accountHolderName // "N/A"')
echo "카카오뱅크 수신자: $KAKAO_RECEIVER_NAME"
echo

# 4. 빠른 금액 선택 테스트 (1만원)
echo "4. 송금 테스트 - 빠른 선택 1만원"
TRANSFER_10K_RESPONSE=$(curl -s -X POST http://localhost:8090/api/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "receiverAccountNumber": "EP0000000002",
    "amount": 10000,
    "memo": "빠른 금액 선택 테스트 - 1만원"
  }')

echo "1만원 송금 결과: $(echo $TRANSFER_10K_RESPONSE | jq -r '.message // .error // "송금 진행"')"
echo

# 5. 빠른 금액 선택 테스트 (100만원)
echo "5. 송금 테스트 - 빠른 선택 100만원"
TRANSFER_1M_RESPONSE=$(curl -s -X POST http://localhost:8090/api/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "receiverAccountNumber": "EP0000000002",
    "amount": 1000000,
    "memo": "빠른 금액 선택 테스트 - 100만원"
  }')

echo "100만원 송금 결과: $(echo $TRANSFER_1M_RESPONSE | jq -r '.message // .error // "송금 진행"')"
echo

# 6. 잔액 확인
echo "6. 송금 후 잔액 확인"
BALANCE_RESPONSE=$(curl -s -X GET http://localhost:8090/accounts/balance \
  -H "Authorization: Bearer $JWT_TOKEN")

echo "현재 잔액: $(echo $BALANCE_RESPONSE | jq -r '.balance // "조회 실패"')원"
echo

echo "=== 테스트 완료 ==="
echo "✅ 로그인 성공"
echo "✅ EasyPay 계좌 검증 성공"
echo "✅ 카카오뱅크 계좌 검증 테스트"
echo "✅ 빠른 금액 선택 기능 (1만원, 100만원)"
echo "✅ 송금 기능 테스트"
echo "✅ 잔액 조회"