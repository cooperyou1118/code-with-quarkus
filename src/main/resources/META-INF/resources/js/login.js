/**
 * 로그인 폼 유효성 검사 및 해시 암호화 전송 스크립트
 */

function validateAndLogin() {
    let valid = true;

    // ① HTML input 엘리먼트에서 값 가져오기 (공백 제거)
    const username = document.getElementById('usernameInput').value.trim();
    const password = document.getElementById('passwordInput').value;

    // ② 아이디 유효성 검사 (4~20자 영문/숫자만 허용)
    const usernameRegex = /^[a-zA-Z0-9]{4,20}$/;
    if (!usernameRegex.test(username)) {
        showError('usernameInput', 'usernameMsg', '아이디는 4~20자 영문/숫자만 가능합니다.');
        valid = false;
    } else {
        clearError('usernameInput');
    }

    // ③ 패스워드 유효성 검사 (8자 이상, 영문 + 숫자 + 특수문자 포함)
    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$/;
    if (!passwordRegex.test(password)) {
        showError('passwordInput', 'passwordMsg', '8자 이상, 영문+숫자+특수문자를 포함해야 합니다.');
        valid = false;
    } else {
        clearError('passwordInput');
    }

    // ④ 두 항목 모두 통과 시 암호화 및 로그인 폼 전송 실행
    if (valid) {
        submitLogin();
    }
}

// 비밀번호를 암호화하여 서버로 전송하는 함수
async function submitLogin() {
    const passwordInputEle = document.getElementById('passwordInput');
    const password = passwordInputEle.value;
    
    // 외부 스크립트(input_sha256.js)에서 불러온 hashPassword 함수 사용
    const hashed = await hashPassword(password); 
  
    // 1. 해시된 값을 진짜 전송될 hidden 필드(id="password")에 삽입
    document.getElementById('password').value = hashed;

    // 2. [보안 방어] 서버로 평문 비밀번호가 날아가지 않도록 name 속성 제거
    if (passwordInputEle) {
        passwordInputEle.removeAttribute('name');
    }

    // 3. 폼 전송
    document.getElementById('loginForm').submit();
}

// ==========================================
// UI 상태 변경 함수 (에러/성공 스타일 적용)
// ==========================================

// 에러 메시지를 보여주는 함수
function showError(fieldId, msgId, message) {
    const field = document.getElementById(fieldId);
    if (field) {
        field.classList.remove('is-valid'); // [버그 수정] 기존 성공 테두리 제거
        field.classList.add('is-invalid');  // Bootstrap 빨간 테두리 추가
    }
  
    const msg = document.getElementById(msgId);
    if (msg) {
        msg.textContent = message; // 에러 메시지 텍스트 삽입
    }
}

// 에러를 지워주고 성공 상태를 보여주는 함수
function clearError(fieldId) {
    const field = document.getElementById(fieldId);
    if (field) {
        field.classList.remove('is-invalid'); // 에러 테두리 제거
        field.classList.add('is-valid');      // Bootstrap 초록 테두리 추가
    }
}

// ==========================================
// [12주차 과제] 서버 로그인 에러 처리 (URL 파라미터 감지)
// ==========================================

// [성능 최적화] 'load' 대신 'DOMContentLoaded'를 사용하여 더 빠르게 화면 렌더링
document.addEventListener('DOMContentLoaded', function() {
    // 1. URL에서 파라미터(?error=1)를 가져옵니다.
    const params = new URLSearchParams(window.location.search);
    const error = params.get('error');
    
    // 2. error 값이 '1'이면 (로그인 실패 시) 에러 메시지를 출력합니다.
    if (error === '1') {
        showError('passwordInput', 'passwordMsg', '아이디 또는 패스워드가 올바르지 않습니다.');
    }
});