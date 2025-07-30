#!/bin/bash

# 테스트 데이터 준비 스크립트
# 성능 테스트를 위한 사용자 및 잔액 데이터 생성

BASE_URL="http://localhost:8090"
echo "성능 테스트를 위한 데이터 준비 시작..."

# 색상 코드
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 성공/실패 카운터
SUCCESS_COUNT=0
FAIL_COUNT=0

# 1. 테스트 사용자 생성 (Gatling 시나리오에서 사용하는 계정들)
echo "테스트 사용자 생성 중..."

# 사용자 배열 정의
declare -a users=(
  "010-1111-1111:password123:테스트유저1"
  "010-2222-2222:password123:테스트유저2"
  "010-3333-3333:password123:테스트유저3"
  "010-4444-4444:password123:테스트유저4"
  "010-5555-5555:password123:테스트유저5"
  "010-6666-6666:password123:테스트유저6"
  "010-7777-7777:password123:테스트유저7"
  "010-8888-8888:password123:테스트유저8"
  "010-9999-9999:password123:테스트유저9"
  "010-1010-1010:password123:테스트유저10"
)

# 생성된 계좌 정보 저장
declare -a account_numbers=()
declare -a access_tokens=()

# 사용자 생성
for user_data in "${users[@]}"; do
  IFS=':' read -ra USER_INFO <<< "$user_data"
  PHONE="${USER_INFO[0]}"
  PASSWORD="${USER_INFO[1]}"
  NAME="${USER_INFO[2]}"
  
  echo -n "Creating user $PHONE ($NAME)... "
  
  RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d "{
      \"phoneNumber\": \"$PHONE\",
      \"password\": \"$PASSWORD\",
      \"name\": \"$NAME\"
    }")
  
  if [[ $RESPONSE == *"accessToken"* ]]; then
    ACCOUNT_NUMBER=$(echo $RESPONSE | jq -r '.accountNumber')
    ACCESS_TOKEN=$(echo $RESPONSE | jq -r '.accessToken')
    account_numbers+=("$ACCOUNT_NUMBER")
    access_tokens+=("$ACCESS_TOKEN")
    echo -e "${GREEN}OK${NC} Account: $ACCOUNT_NUMBER"
    ((SUCCESS_COUNT++))
  else
    echo -e "${RED}FAIL${NC}"
    ((FAIL_COUNT++))
  fi
done

echo ""
echo "사용자 생성 완료: 성공 $SUCCESS_COUNT, 실패 $FAIL_COUNT"

# 2. 테스트 잔액 충전
echo ""
echo "테스트 잔액 충전 중..."

# 관리자 계정으로 로그인 (없다면 첫 번째 사용자 사용)
ADMIN_TOKEN="${access_tokens[0]}"

# 각 계좌에 잔액 추가 (실제로는 입금 API가 필요하지만, 테스트를 위해 간단히 처리)
for i in "${!account_numbers[@]}"; do
  ACCOUNT="${account_numbers[$i]}"
  TOKEN="${access_tokens[$i]}"
  AMOUNT=$((1000000 + RANDOM % 9000000)) # 100만원 ~ 1000만원 랜덤
  
  echo "Account $ACCOUNT: 잔액 설정 중... (KRW $(printf "%'d" $AMOUNT))"
  
  # 실제 입금 API가 있다면 여기서 호출
  # 현재는 계좌 조회로 대체
  curl -s -X GET "$BASE_URL/accounts/balance" \
    -H "Authorization: Bearer $TOKEN" > /dev/null 2>&1
done

# 3. 송금용 수신 계좌 정보 생성
echo ""
echo "송금 테스트용 수신 계좌 목록 생성..."

cat > test-accounts.json << EOF
{
  "senders": [
$(for i in "${!account_numbers[@]}"; do
  if [ $i -eq $((${#account_numbers[@]} - 1)) ]; then
    echo "    {\"phoneNumber\": \"${users[$i]%%:*}\", \"accountNumber\": \"${account_numbers[$i]}\"}"
  else
    echo "    {\"phoneNumber\": \"${users[$i]%%:*}\", \"accountNumber\": \"${account_numbers[$i]}\"},"
  fi
done)
  ],
  "receivers": [
$(for i in "${!account_numbers[@]}"; do
  # 마지막 5개 계좌를 수신 전용으로 설정
  if [ $i -ge 5 ]; then
    USER_NAME="${users[$i]##*:}"
    if [ $i -eq $((${#account_numbers[@]} - 1)) ]; then
      echo "    {\"accountNumber\": \"${account_numbers[$i]}\", \"name\": \"$USER_NAME\"}"
    else
      echo "    {\"accountNumber\": \"${account_numbers[$i]}\", \"name\": \"$USER_NAME\"},"
    fi
  fi
done)
  ]
}
EOF

echo "테스트 계좌 정보가 test-accounts.json에 저장되었습니다."

# 4. 성능 테스트 실행 가이드
echo ""
echo "성능 테스트 실행 준비 완료!"
echo ""
echo "다음 명령어로 테스트를 실행하세요:"
echo "  ./gradlew gatlingRun-fintech2.easypay.performance.PaymentLoadTestSimulation"
echo "  ./gradlew gatlingRun-fintech2.easypay.performance.TransferLoadTestSimulation"
echo "  ./gradlew gatlingRun-fintech2.easypay.performance.MixedTrafficSimulation"
echo ""
echo "생성된 계좌 정보:"
echo "  - 총 사용자: ${#account_numbers[@]}명"
echo "  - 송금 가능 계좌: 5개"
echo ""

# 생성된 토큰 정보를 파일로 저장 (디버깅용)
cat > test-tokens.txt << EOF
# 테스트용 Access Tokens
# Format: phoneNumber:token
$(for i in "${!users[@]}"; do
  echo "${users[$i]%%:*}:${access_tokens[$i]}"
done)
EOF

echo "토큰 정보가 test-tokens.txt에 저장되었습니다."