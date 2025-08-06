// 토큰 갱신 중복 방지 플래그
let isRefreshing = false;

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 현재 페이지가 로그인 관련 페이지인지 확인
    const isLoginPage = window.location.pathname.includes('login') || 
                       window.location.pathname.includes('register') || 
                       window.location.pathname === '/' || 
                       window.location.pathname === '/index.html';
    
    // 로그인 페이지가 아닐 때만 토큰 체크 실행
    if (!isLoginPage) {
        // 설정에서 체크 주기 가져오기 (기본값: 30초, 60초)
        const accessCheckInterval = 30000; // 30초
        const backgroundCheckInterval = 60000; // 60초
        
        // 토큰 만료 체크 시작
        setInterval(checkTokenExpiration, accessCheckInterval);
        
        // 백그라운드 토큰 체크 시작
        setInterval(backgroundTokenCheck, backgroundCheckInterval);
        
        // 초기 토큰 체크
        checkTokenExpiration();
        backgroundTokenCheck();
    }
    
    checkAuth();
    loadUserInfo();
    loadNotifications();
    
    // 30초마다 알림 개수 업데이트
    setInterval(loadNotifications, 30000);
});

// 인증 확인
function checkAuth() {
    const token = localStorage.getItem('accessToken');
    if (!token) {
        window.location.href = '/index.html';
    }
}

// 사용자 정보 로드
async function loadUserInfo() {
    const token = localStorage.getItem('accessToken');
    const accountNumber = localStorage.getItem('accountNumber');
    const userName = localStorage.getItem('userName');
    
    console.log('Loading user info:', { accountNumber, userName });
    
    if (accountNumber) {
        document.getElementById('accountNumber').textContent = accountNumber;
    } else {
        document.getElementById('accountNumber').textContent = '-';
        console.warn('Account number not found in localStorage');
    }
    
    // 사용자 이름 설정
    const displayName = userName || '고객님';
    document.getElementById('userName').textContent = displayName;
}

// 알림 개수 로드
async function loadNotifications() {
    const token = localStorage.getItem('accessToken');
    
    if (!token) {
        console.log('No token found, skipping notification count load');
        return;
    }
    
    try {
        const response = await fetch('/alarms/count', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            const count = data.count || 0;
            
            console.log('Notification count loaded:', count);
            
            const badge = document.getElementById('notificationBadge');
            if (count > 0) {
                badge.textContent = count;
                badge.style.display = 'flex';
            } else {
                badge.style.display = 'none';
            }
        } else {
            console.error('Notification count load failed:', response.status);
            const badge = document.getElementById('notificationBadge');
            badge.style.display = 'none';
        }
    } catch (error) {
        console.error('Notification count load error:', error);
        // 에러 시 배지 숨김
        const badge = document.getElementById('notificationBadge');
        badge.style.display = 'none';
    }
}

// 알림 체크
function checkNotifications() {
    alert('새로운 알림이 없습니다.');
    
    // 알림 배지 숨기기
    const badge = document.getElementById('notificationBadge');
    badge.style.display = 'none';
}

// 서비스 페이지 이동
function goToTransfer() {
    window.location.href = '/transfer.html';
}

function goToPayment() {
    window.location.href = '/payment.html';
}

function goToBalance() {
    window.location.href = '/balance.html';
}

// 알람 페이지로 이동
function goToAlarm() {
    // 알림을 읽음 처리
    localStorage.setItem('alarmsReadAt', new Date().toISOString());
    
    // 배지 숨기기
    const badge = document.getElementById('notificationBadge');
    badge.style.display = 'none';
    
    window.location.href = '/alarm.html';
}

// 로그아웃 함수는 common.js에서 제공됨 

// 토큰 만료 체크
async function checkTokenExpiration() {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');
    
    if (!accessToken || !refreshToken) {
        console.log('토큰이 없어 로그인 페이지로 이동');
        logout();
        return;
    }

    try {
        const payload = JSON.parse(atob(accessToken.split('.')[1]));
        const expirationTime = payload.exp * 1000;
        const currentTime = Date.now();
        
        console.log('메인 페이지 토큰 체크:', {
            remainingSeconds: Math.floor((expirationTime - currentTime) / 1000)
        });
        
        // 만료되었으면 갱신 시도
        if (currentTime >= expirationTime) {
            console.log('Access Token 만료됨, 갱신 시도...');
            const success = await tryRefreshToken();
            if (!success) {
                console.log('토큰 갱신 실패, 로그아웃');
                showAutoLogoutPopup();
            }
        }
    } catch (error) {
        console.error('토큰 체크 오류:', error);
        showAutoLogoutPopup();
    }
}

// 백그라운드 토큰 체크
async function backgroundTokenCheck() {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');
    
    if (!accessToken || !refreshToken) {
        return;
    }

    try {
        const payload = JSON.parse(atob(accessToken.split('.')[1]));
        const expirationTime = payload.exp * 1000;
        const currentTime = Date.now();
        
        // 만료되었거나 곧 만료될 예정이면 갱신 시도
        if (currentTime >= expirationTime || (expirationTime - currentTime < 60000)) {
            console.log('🔄 백그라운드 토큰 갱신 시도...');
            const success = await tryRefreshToken();
            if (!success) {
                console.log('❌ 백그라운드 토큰 갱신 실패, 로그아웃');
                showAutoLogoutPopup();
            }
        }
    } catch (error) {
        console.error('백그라운드 토큰 체크 오류:', error);
        showAutoLogoutPopup();
    }
}

// Refresh Token으로 토큰 갱신 시도
async function tryRefreshToken() {
    // 이미 갱신 중이면 중복 실행 방지
    if (isRefreshing) {
        console.log('🔄 이미 토큰 갱신 중... 대기');
        return false;
    }
    
    // 로그아웃 진행 중이면 갱신 시도하지 않음
    if (logoutInProgress) {
        console.log('🚪 로그아웃 진행 중... 갱신 시도 중단');
        return false;
    }
    
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
        console.log('❌ Refresh Token이 없습니다.');
        return false;
    }

    isRefreshing = true;
    
    try {
        console.log('🔄 === Refresh Token으로 토큰 갱신 시도 ===');
        
        const response = await fetch('/auth/refresh', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                refreshToken: refreshToken
            })
        });

        if (response.ok) {
            const data = await response.json();
            
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            
            console.log('✅ === 토큰 갱신 성공! ===');
            return true;
        } else {
            console.log('❌ Refresh Token 갱신 실패:', response.status);
            const errorText = await response.text();
            console.log('❌ 에러 응답:', errorText);
            
            if (response.status === 400 && (errorText.includes('EXPIRED_REFRESH_TOKEN') || errorText.includes('INVALID_REFRESH_TOKEN'))) {
                console.log('🚪 Refresh Token 만료/무효로 즉시 로그아웃 실행');
                showAutoLogoutPopup();
                return false;
            }
            
            return false;
        }
    } catch (error) {
        console.error('❌ 토큰 갱신 중 오류:', error);
        showAutoLogoutPopup();
        return false;
    } finally {
        isRefreshing = false;
    }
} 