// API 기본 설정
const API_BASE_URL = 'http://localhost:8090';

// 토큰 갱신 중복 방지 플래그
let isRefreshing = false;

// API 호출 함수 (자동 토큰 갱신 포함)
async function apiCall(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // 토큰이 있으면 헤더에 추가
    const token = localStorage.getItem('accessToken');
    if (token) {
        defaultOptions.headers['Authorization'] = `Bearer ${token}`;
    }

    const finalOptions = {
        ...defaultOptions,
        ...options,
        headers: {
            ...defaultOptions.headers,
            ...options.headers,
        },
    };

    try {
        const response = await fetch(url, finalOptions);
        
        // 401 Unauthorized - Access Token 만료
        if (response.status === 401) {
            console.log('Access Token 만료됨. Refresh Token으로 갱신 시도...');
            
            const refreshSuccess = await refreshAccessToken();
            if (refreshSuccess) {
                // 새 토큰으로 재시도
                const newToken = localStorage.getItem('accessToken');
                finalOptions.headers['Authorization'] = `Bearer ${newToken}`;
                const retryResponse = await fetch(url, finalOptions);
                
                if (!retryResponse.ok) {
                    const errorData = await retryResponse.json().catch(() => ({}));
                    throw new Error(errorData.message || `HTTP error! status: ${retryResponse.status}`);
                }
                
                return await retryResponse.json();
            } else {
                // Refresh Token도 만료됨 - 즉시 로그아웃
                console.log('Refresh Token도 만료됨. 즉시 로그아웃 처리...');
                logout();
                throw new Error('인증이 만료되었습니다. 다시 로그인해주세요.');
            }
        }
        
        // 400 Bad Request - Refresh Token 관련 에러
        if (response.status === 400) {
            const errorData = await response.json().catch(() => ({}));
            if (errorData.error === 'EXPIRED_REFRESH_TOKEN' || errorData.error === 'INVALID_REFRESH_TOKEN') {
                console.log('Refresh Token 만료/무효. 즉시 로그아웃 처리...');
                logout();
                throw new Error('인증이 만료되었습니다. 다시 로그인해주세요.');
            }
        }
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            
            // 백엔드에서 전달된 에러 코드 처리
            if (errorData.errorCode) {
                throw new Error(errorData.errorCode);
            } else if (errorData.message) {
                throw new Error(errorData.message);
            } else {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
        }
        
        return await response.json();
    } catch (error) {
        console.error('API 호출 오류:', error);
        throw error;
    }
}

// Refresh Token으로 Access Token 갱신
async function refreshAccessToken() {
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
        console.log('Refresh Token이 없습니다.');
        return false;
    }

    isRefreshing = true;
    
    try {
        console.log('Refresh Token 사용하여 Access Token 갱신 중...');
        
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
            
            // 새로운 토큰들 저장
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            
            console.log('토큰 갱신 성공!');
            console.log('새 Access Token:', data.accessToken.substring(0, 20) + '...');
            console.log('새 Refresh Token:', data.refreshToken.substring(0, 20) + '...');
            
            return true;
        } else {
            console.log('Refresh Token 갱신 실패:', response.status);
            const errorText = await response.text();
            console.log('에러 응답:', errorText);
            
            // 만료된 경우 즉시 로그아웃
            if (errorText.includes('EXPIRED_REFRESH_TOKEN') || errorText.includes('INVALID_REFRESH_TOKEN')) {
                console.log('Refresh Token 만료/무효로 즉시 로그아웃');
                showAutoLogoutPopup();
                return false;
            }
            
            return false;
        }
    } catch (error) {
        console.error('토큰 갱신 중 오류:', error);
        // 네트워크 오류 시에도 로그아웃
        showAutoLogoutPopup();
        return false;
    } finally {
        isRefreshing = false;
    }
}

// 인증 관련 API
async function register(phoneNumber, password) {
    return apiCall('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ phoneNumber, password }),
    });
}

async function login(phoneNumber, password) {
    return apiCall('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ phoneNumber, password }),
    });
}

// 로그아웃 함수는 common.js에서 제공됨

async function refreshToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
        throw new Error('Refresh token이 없습니다.');
    }
    
    return apiCall('/auth/refresh', {
        method: 'POST',
        body: JSON.stringify({ refreshToken }),
    });
}

// 계좌 관련 API
async function getAccountBalance() {
    return apiCall('/account/balance');
}

async function getTransactionHistory(type = 'ALL') {
    return apiCall(`/account/transactions?type=${type}`);
}

async function transfer(recipientAccount, amount, memo) {
    return apiCall('/account/transfer', {
        method: 'POST',
        body: JSON.stringify({ recipientAccount, amount, memo }),
    });
}

async function deposit(amount, memo) {
    return apiCall('/account/deposit', {
        method: 'POST',
        body: JSON.stringify({ amount, memo }),
    });
}

async function withdraw(amount, memo) {
    return apiCall('/account/withdraw', {
        method: 'POST',
        body: JSON.stringify({ amount, memo }),
    });
}

// 결제 관련 API
async function makePayment(merchantName, amount, category) {
    return apiCall('/account/payment', {
        method: 'POST',
        body: JSON.stringify({ merchantName, amount, category }),
    });
}

// 감사 로그 API
async function getAuditLogs() {
    return apiCall('/audit/logs');
}

async function getLoginHistory() {
    return apiCall('/audit/login-history');
}

// 유틸리티 함수
function formatCurrency(amount) {
    return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW'
    }).format(amount);
}

function formatDate(dateString) {
    return new Date(dateString).toLocaleString('ko-KR');
}

// 토큰 관리
function isTokenValid() {
    const token = localStorage.getItem('accessToken');
    if (!token) return false;
    
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.exp * 1000 > Date.now();
    } catch (error) {
        return false;
    }
}

async function ensureValidToken() {
    if (!isTokenValid()) {
        try {
            const response = await refreshToken();
            localStorage.setItem('accessToken', response.accessToken);
            return true;
        } catch (error) {
            // 토큰 갱신 실패 - 로그인 페이지로 이동
            localStorage.clear();
            window.location.href = '/index.html';
            return false;
        }
    }
    return true;
}

// 전역 함수로 노출
window.api = {
    register,
    login,
    logout,
    refreshToken,
    getAccountBalance,
    getTransactionHistory,
    transfer,
    deposit,
    withdraw,
    makePayment,
    getAuditLogs,
    getLoginHistory,
    formatCurrency,
    formatDate,
    isTokenValid,
    ensureValidToken
}; 