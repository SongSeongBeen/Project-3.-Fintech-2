// 중복검사 완료 여부 추적 변수
let emailDuplicateChecked = false;
let phoneDuplicateChecked = false;

// 페이지 로드 시 실행
document.addEventListener('DOMContentLoaded', function() {
    // 로그인 폼 처리
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // 회원가입 폼 처리
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }

    // 휴대폰 번호 자동 포맷팅 이벤트 리스너 추가
    const phoneInputs = document.querySelectorAll('#phoneNumber, #regPhoneNumber');
    phoneInputs.forEach(input => {
        if (input) {
            input.addEventListener('input', formatPhoneNumber);
            input.addEventListener('keydown', filterPhoneInput);
        }
    });

    // 비밀번호 검증 이벤트 리스너 추가
    const passwordInput = document.getElementById('regPassword');
    const confirmPasswordInput = document.getElementById('regConfirmPassword');
    
    if (passwordInput) {
        passwordInput.addEventListener('input', validatePassword);
    }
    
    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', validateConfirmPassword);
    }

    // 중복검사 상태 초기화 이벤트 리스너 추가
    const phoneInput = document.getElementById('regPhoneNumber');
    const emailInput = document.getElementById('regEmail');
    
    if (phoneInput) {
        phoneInput.addEventListener('input', function() {
            phoneDuplicateChecked = false;
            validatePhoneFormat();
        });
    }
    
    if (emailInput) {
        emailInput.addEventListener('input', function() {
            emailDuplicateChecked = false;
            validateEmailFormat();
        });
    }
});

// 로그인 처리
async function handleLogin(e) {
    e.preventDefault();
    
    const phoneNumber = document.getElementById('phoneNumber').value;
    const password = document.getElementById('password').value;
    const loading = document.getElementById('loginLoading');
    
    if (!phoneNumber || !password) {
        showAlert('loginAlert', '휴대폰 번호와 비밀번호를 입력해주세요.', 'error');
        return;
    }

    loading.classList.add('show');
    
    try {
        const response = await fetch('/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                phoneNumber: phoneNumber,
                password: password
            })
        });

        const data = await response.json();

        if (response.ok) {
            // 토큰 저장
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('userPhone', phoneNumber);
            
            // 계좌번호와 사용자 이름이 응답에 포함되어 있다면 저장
            if (data.accountNumber) {
                localStorage.setItem('accountNumber', data.accountNumber);
                console.log('Account number saved:', data.accountNumber);
            } else {
                console.warn('No account number in login response');
            }
            if (data.userName) {
                localStorage.setItem('userName', data.userName);
                console.log('User name saved:', data.userName);
            } else {
                console.warn('No user name in login response');
            }
            
            console.log('Login response data:', data);
            
            showAlert('loginAlert', '로그인 성공!', 'success');
            setTimeout(() => {
                window.location.href = '/main.html';
            }, 1000);
        } else {
            showAlert('loginAlert', data.message || '로그인에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showAlert('loginAlert', '서버 연결에 실패했습니다.', 'error');
    } finally {
        loading.classList.remove('show');
    }
}

// 회원가입 처리
async function handleRegister(e) {
    e.preventDefault();
    
    const phoneNumber = document.getElementById('regPhoneNumber').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const name = document.getElementById('regName').value;
    const loading = document.getElementById('registerLoading');
    
    const confirmPassword = document.getElementById('regConfirmPassword').value;
    
    if (!phoneNumber || !password || !name || !email || !confirmPassword) {
        showAlert('registerAlert', '모든 필수 항목을 입력해주세요.', 'error');
        return;
    }

    // 중복검사 완료 여부 확인
    let missingChecks = [];
    let focusField = null;

    if (!phoneDuplicateChecked) {
        missingChecks.push('휴대폰 번호');
        if (!focusField) focusField = 'regPhoneNumber';
    }

    if (!emailDuplicateChecked) {
        missingChecks.push('이메일');
        if (!focusField) focusField = 'regEmail';
    }

    if (missingChecks.length > 0) {
        const message = `${missingChecks.join(', ')} 중복검사를 해주세요.`;
        showAlert('registerAlert', message, 'error');
        
        // 해당 필드로 커서 이동
        if (focusField) {
            const field = document.getElementById(focusField);
            if (field) {
                field.focus();
                field.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
        return;
    }

    // 휴대폰 번호 형식 검증
    const phonePattern = /^010-\d{4}-\d{4}$/;
    if (!phonePattern.test(phoneNumber)) {
        showAlert('registerAlert', '휴대폰 번호 형식이 올바르지 않습니다. (010-1234-5678)', 'error');
        return;
    }

    // 이메일 형식 검증 (이메일 필수)
    if (!isValidEmail(email)) {
        showAlert('registerAlert', '올바른 이메일 형식이 아닙니다.', 'error');
        return;
    }

    // 비밀번호 규칙 검증
    if (!isValidPassword(password)) {
        if (password.length < 6) {
            showAlert('registerAlert', '비밀번호는 6자 이상이어야 합니다.', 'error');
        } else if (password.length >= 8) {
            showAlert('registerAlert', '8자 이상인 경우 영문+숫자 조합이어야 합니다.', 'error');
        }
        return;
    }

    // 비밀번호 확인 검증
    if (password !== confirmPassword) {
        showAlert('registerAlert', '비밀번호가 일치하지 않습니다.', 'error');
        return;
    }

    loading.classList.add('show');
    
    try {
        const response = await fetch('/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                phoneNumber: phoneNumber,
                email: email || null,
                password: password,
                name: name
            })
        });

        const data = await response.json();

        if (response.ok) {
            // 회원가입 완료 팝업
            alert('회원가입이 완료되었습니다!');
            
            // 계좌번호가 응답에 포함되어 있다면 저장
            if (data.accountNumber) {
                localStorage.setItem('accountNumber', data.accountNumber);
            }
            
            // 로그인 페이지로 이동
            window.location.href = '/index.html';
        } else {
            showAlert('registerAlert', data.message || '회원가입에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('Register error:', error);
        showAlert('registerAlert', '서버 연결에 실패했습니다.', 'error');
    } finally {
        loading.classList.remove('show');
    }
}

// 이메일 중복 검증
async function checkEmailDuplicate() {
    console.log('이메일 중복검사 함수 호출됨');
    const email = document.getElementById('regEmail').value.trim();
    const statusElement = document.getElementById('emailStatus');
    
    if (!email) {
        statusElement.textContent = '이메일은 필수입니다.';
        statusElement.className = 'email-status error';
        return;
    }
    
    if (!isValidEmail(email)) {
        statusElement.textContent = '올바른 이메일 형식이 아닙니다. (@ 포함)';
        statusElement.className = 'email-status error';
        return;
    }
    
    try {
        const response = await fetch(`/auth/check-email?email=${encodeURIComponent(email)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();

        if (response.ok) {
            statusElement.textContent = '사용 가능한 이메일입니다.';
            statusElement.className = 'email-status success';
            emailDuplicateChecked = true;
        } else if (response.status === 409) {
            statusElement.textContent = '이미 사용 중인 이메일입니다.';
            statusElement.className = 'email-status error';
            emailDuplicateChecked = false;
        } else {
            statusElement.textContent = data.message || '이메일 확인 중 오류가 발생했습니다.';
            statusElement.className = 'email-status error';
            emailDuplicateChecked = false;
        }
    } catch (error) {
        console.error('Email check error:', error);
        statusElement.textContent = '서버 연결에 실패했습니다.';
        statusElement.className = 'email-status error';
    }
}

// 휴대폰 번호 중복 검증
async function checkPhoneDuplicate() {
    console.log('휴대폰 번호 중복검사 함수 호출됨');
    const phoneNumber = document.getElementById('regPhoneNumber').value.trim();
    const statusElement = document.getElementById('phoneStatus');
    
    if (!phoneNumber) {
        statusElement.textContent = '휴대폰 번호는 필수입니다.';
        statusElement.className = 'phone-status error';
        return;
    }
    
    // 휴대폰 번호 형식 검증
    const phonePattern = /^010-\d{4}-\d{4}$/;
    if (!phonePattern.test(phoneNumber)) {
        statusElement.textContent = '휴대폰 번호는 010-0000-0000 형태로 입력해주세요.';
        statusElement.className = 'phone-status error';
        return;
    }
    
    try {
        const response = await fetch(`/auth/check-phone?phoneNumber=${encodeURIComponent(phoneNumber)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const data = await response.json();

        if (response.ok) {
            statusElement.textContent = '사용 가능한 휴대폰 번호입니다.';
            statusElement.className = 'phone-status success';
            phoneDuplicateChecked = true;
        } else if (response.status === 409) {
            statusElement.textContent = '이미 사용 중인 휴대폰 번호입니다.';
            statusElement.className = 'phone-status error';
            phoneDuplicateChecked = false;
        } else {
            statusElement.textContent = data.message || '휴대폰 번호 확인 중 오류가 발생했습니다.';
            statusElement.className = 'phone-status error';
            phoneDuplicateChecked = false;
        }
    } catch (error) {
        console.error('Phone check error:', error);
        statusElement.textContent = '서버 연결에 실패했습니다.';
        statusElement.className = 'phone-status error';
    }
}

// 이메일 유효성 검사
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// 비밀번호 유효성 검사
function isValidPassword(password) {
    // 6자 이상 (기본), 8자 이상인 경우 영문+숫자 조합
    if (password.length < 6) {
        return false;
    }
    if (password.length >= 8) {
        const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/;
        return passwordRegex.test(password);
    }
    return true; // 6-7자리는 기본 규칙만 만족하면 OK
}

// 비밀번호 검증
function validatePassword() {
    const password = document.getElementById('regPassword').value;
    const statusElement = document.getElementById('passwordStatus');
    
    if (!password) {
        statusElement.textContent = '';
        statusElement.className = 'password-status';
        return;
    }
    
    if (isValidPassword(password)) {
        statusElement.textContent = '사용 가능한 비밀번호입니다.';
        statusElement.className = 'password-status success';
    } else {
        if (password.length < 6) {
            statusElement.textContent = '비밀번호는 6자 이상이어야 합니다.';
        } else if (password.length >= 8) {
            statusElement.textContent = '8자 이상인 경우 영문+숫자 조합으로 입력해주세요.';
        } else {
            statusElement.textContent = '사용 가능한 비밀번호입니다.';
        }
        statusElement.className = 'password-status error';
    }
    
    // 비밀번호 확인도 다시 검증
    validateConfirmPassword();
}

// 휴대폰 번호 형식 검증
function validatePhoneFormat() {
    const phoneNumber = document.getElementById('regPhoneNumber').value;
    const statusElement = document.getElementById('phoneStatus');
    
    if (!phoneNumber) {
        statusElement.textContent = '';
        statusElement.className = 'phone-status';
        return;
    }
    
    const phonePattern = /^010-\d{4}-\d{4}$/;
    if (phonePattern.test(phoneNumber)) {
        statusElement.textContent = '';
        statusElement.className = 'phone-status';
    } else {
        statusElement.textContent = '휴대폰 번호는 010-0000-0000 형태로 입력해주세요.';
        statusElement.className = 'phone-status error';
    }
}

// 이메일 형식 검증
function validateEmailFormat() {
    const email = document.getElementById('regEmail').value;
    const statusElement = document.getElementById('emailStatus');
    
    if (!email) {
        statusElement.textContent = '';
        statusElement.className = 'email-status';
        return;
    }
    
    if (isValidEmail(email)) {
        statusElement.textContent = '';
        statusElement.className = 'email-status';
    } else {
        statusElement.textContent = '올바른 이메일 형식이 아닙니다. (@ 포함)';
        statusElement.className = 'email-status error';
    }
}

// 비밀번호 확인 검증
function validateConfirmPassword() {
    const password = document.getElementById('regPassword').value;
    const confirmPassword = document.getElementById('regConfirmPassword').value;
    const statusElement = document.getElementById('confirmPasswordStatus');
    
    if (!confirmPassword) {
        statusElement.textContent = '';
        statusElement.className = 'confirm-password-status';
        return;
    }
    
    if (password === confirmPassword) {
        statusElement.textContent = '비밀번호가 일치합니다.';
        statusElement.className = 'confirm-password-status success';
    } else {
        statusElement.textContent = '비밀번호가 다릅니다.';
        statusElement.className = 'confirm-password-status error';
    }
}

// 페이지 이동 함수들
function goToRegister() {
    window.location.href = '/register.html';
}

function goToLogin() {
    window.location.href = '/index.html';
}

// 휴대폰 번호 입력 필터링 (숫자와 백스페이스, 삭제키만 허용)
function filterPhoneInput(event) {
    const allowedKeys = [
        'Backspace', 'Delete', 'Tab', 'Escape', 'Enter',
        'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown',
        'Home', 'End'
    ];
    
    // 허용된 키이거나 숫자키인 경우만 허용
    if (allowedKeys.includes(event.key) || 
        (event.key >= '0' && event.key <= '9') ||
        (event.ctrlKey && (event.key === 'a' || event.key === 'c' || event.key === 'v'))) {
        return;
    }
    
    // 그 외의 키는 차단
    event.preventDefault();
}

// 휴대폰 번호 자동 포맷팅 (000-0000-0000 형식)
function formatPhoneNumber(event) {
    let value = event.target.value;
    
    // 숫자만 추출
    const numbersOnly = value.replace(/\D/g, '');
    
    // 11자리를 초과하지 않도록 제한
    const limitedNumbers = numbersOnly.substring(0, 11);
    
    // 자동 포맷팅
    let formatted = '';
    
    if (limitedNumbers.length <= 3) {
        formatted = limitedNumbers;
    } else if (limitedNumbers.length <= 7) {
        formatted = limitedNumbers.substring(0, 3) + '-' + limitedNumbers.substring(3);
    } else {
        formatted = limitedNumbers.substring(0, 3) + '-' + 
                   limitedNumbers.substring(3, 7) + '-' + 
                   limitedNumbers.substring(7);
    }
    
    event.target.value = formatted;
}

// 알림 메시지 표시
function showAlert(alertId, message, type) {
    const alertElement = document.getElementById(alertId);
    if (alertElement) {
        alertElement.textContent = message;
        alertElement.className = `alert alert-${type} show`;
        
        // 3초 후 자동 숨김
        setTimeout(() => {
            alertElement.classList.remove('show');
        }, 3000);
    }
} 