// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
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

// 로그아웃
function logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('accountNumber');
    localStorage.removeItem('userName');
    window.location.href = '/index.html';
} 