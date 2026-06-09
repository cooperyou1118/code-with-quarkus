/**
 * 마이페이지(프로필) 정보 로드, 정보 수정 및 비밀번호 변경 제어 스크립트
 */

// [버그 수정] window.onload 대신 DOMContentLoaded 사용 (다른 스크립트와의 충돌 원천 차단)
document.addEventListener('DOMContentLoaded', function() {
    // 1. 서버에서 사용자 정보 요청 및 화면 표시
    fetch('/profile/info') 
        .then(res => res.json()) 
        .then(data => {
            const profileLink = document.getElementById('profileNavLink');
            if (profileLink) {
                profileLink.setAttribute('data-bs-title', '👏 ' + data.username);
                new bootstrap.Tooltip(profileLink);
            }

            document.getElementById('infoUsername').textContent = data.username; 
            document.getElementById('infoEmail').textContent = data.email;
            document.getElementById('infoPhone').textContent = data.phone;
            
            if (data.profileImage) { 
                document.getElementById('profileImg').src = '/uploads/profile/' + data.profileImage;
            }

            if (document.getElementById('updateEmail')) {
                document.getElementById('updateEmail').value = data.email;
            }
            if (document.getElementById('updatePhone')) {
                document.getElementById('updatePhone').value = data.phone;
            }
        });

    // 2. URL 파라미터 제어 영역 (에러 및 성공 메시지 처리)
    const params = new URLSearchParams(window.location.search);
    const error = params.get('error');
    const success = params.get('success');

    const msgEl = document.getElementById('updateMsg');

    // 개인정보 수정 성공/실패 메시지
    if (msgEl) {
        if (success === 'updated') {
            msgEl.className = 'alert alert-success';
            msgEl.textContent = '✅ 개인정보가 수정되었습니다.';
            msgEl.classList.remove('d-none');
        } else if (error === 'duplicate_email') {
            msgEl.className = 'alert alert-danger';
            msgEl.textContent = '❗ 이미 사용 중인 이메일입니다.';
            msgEl.classList.remove('d-none');
        }
    }
    
    // ==========================================
    // [★ 22페이지 핵심 구현 ★] 비밀번호 변경 성공 처리
    // ==========================================
    if (success === 'password_changed') {
        // ① Toast 출력
        if (typeof showToast === 'function') {
            showToast('✔️ 비밀번호가 변경 완료, 로그인 페이지로 이동합니다.', 'success');
        }
        
        // 상단 알림창 표기
        const pwMsgEl = document.getElementById('pwMsg');
        if (pwMsgEl) {
            pwMsgEl.className = 'alert alert-success';
            pwMsgEl.textContent = '🔒 비밀번호가 변경되었습니다. 잠시 후 이동합니다.';
            pwMsgEl.classList.remove('d-none');
        }

        // ② 3.5초(3500ms) 후 로그인 페이지로 자동 이동 (백엔드 로그아웃 기능 연동)
        setTimeout(function() {
            window.location.href = '/logout?next=login';
        }, 3500);
    }

    // 현재 비밀번호 오류 처리
    if (error === 'wrong_password') {
        if (typeof showToast === 'function') {
            showToast('❌ 현재 비밀번호가 일치하지 않습니다.', 'danger');
        }
        const pwMsgEl = document.getElementById('pwMsg');
        if (pwMsgEl) {
            pwMsgEl.className = 'alert alert-danger';
            pwMsgEl.textContent = '❌ 현재 비밀번호가 일치하지 않습니다.';
            pwMsgEl.classList.remove('d-none');
        }
    }

    // 파일 업로드 오류 메시지 제어
    if (error) {
        const messages = {
            'invalid_type': 'jpg, png, gif, webp 파일만 가능합니다.',
            'too_large': '파일 크기는 5MB 이하여야 합니다.',
            'upload_fail': '업로드 실패. 다시 시도해주세요.'
        };
        const msg = messages[error];
        const div = document.getElementById('uploadErrorMsg');
        if (msg && div) {
            div.textContent = msg;
            div.classList.remove('d-none');
        }
    }
}); // 👈 DOMContentLoaded 안전 종료

// ==========================================
// 3. 비밀번호 유효성 검사 및 SHA-256 해시 처리 함수
// ==========================================
async function validateAndChangePassword() {
    let valid = true;
    const currentPwInput = document.getElementById('currentPwInput');
    const newPwInput = document.getElementById('newPwInput');
    
    const currentPw = currentPwInput.value;
    const newPw = newPwInput.value;
    const newPwConfirm = document.getElementById('newPwConfirm').value;

    // ① 현재 비밀번호 빈 값 체크
    if (!currentPw) {
        showFieldError('currentPwInput', 'currentPwMsg', '현재 비밀번호를 입력해주세요.');
        valid = false;
    } else {
        clearFieldError('currentPwInput');
    }

    // ② 새 비밀번호 정규식 검사
    const pwRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*()]).{8,}$/;
    if (!pwRegex.test(newPw)) {
        showFieldError('newPwInput', 'newPwMsg', '8자 이상, 영문+숫자+특수문자를 포함해야 합니다.');
        valid = false;
    } else {
        clearFieldError('newPwInput');
    }

    // ③ 새 비밀번호 확인 일치
    if (newPw !== newPwConfirm) {
        showFieldError('newPwConfirm', 'newPwConfirmMsg', '새 비밀번호가 일치하지 않습니다.');
        valid = false;
    } else {
        clearFieldError('newPwConfirm');
    }

    // 유효성 검사 실패 시 전송 중단
    if (!valid) return;

    // ④ 현재/새 비밀번호 SHA-256 해시 생성
    const hashedCurrent = await hashSHA256(currentPw);
    const hashedNew = await hashSHA256(newPw);

    document.getElementById('currentPassword').value = hashedCurrent;
    document.getElementById('newPassword').value = hashedNew;

    // F12 콘솔 확인용
    console.log('현재 PW 해시 :', hashedCurrent);
    console.log('새 PW 해시 :', hashedNew);

    // [보안 방어] 서버로 평문 비밀번호가 날아가지 않도록 name 속성 제거
    if (currentPwInput) currentPwInput.removeAttribute('name');
    if (newPwInput) newPwInput.removeAttribute('name');
    const confirmInput = document.getElementById('newPwConfirm');
    if (confirmInput) confirmInput.removeAttribute('name');

    // ⑤ 폼 서브밋
    document.getElementById('pwForm').submit();
}

// SHA-256 비동기 해시 암호화 함수 
async function hashSHA256(message) {
    const msgBuffer = new TextEncoder().encode(message);
    const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

// 개인정보 수정 및 유효성 검증 보조 함수들
function validateAndUpdate() {
    let valid = true;
    const email = document.getElementById('updateEmail').value.trim();
    const phone = document.getElementById('updatePhone').value.trim();

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showFieldError('updateEmail', 'updateEmailMsg', '올바른 이메일 형식이 아닙니다.');
        valid = false;
    } else {
        clearFieldError('updateEmail');
    }

    const phoneRegex = /^010-\d{4}-\d{4}$/;
    if (!phoneRegex.test(phone)) {
        showFieldError('updatePhone', 'updatePhoneMsg', '010-0000-0000 형식으로 입력해주세요.');
        valid = false;
    } else {
        clearFieldError('updatePhone');
    }

    if (valid) document.getElementById('updateForm').submit();
}

// [버그 수정] 부트스트랩 클래스 상호 삭제 로직 추가
function showFieldError(fieldId, msgId, message) {
    const field = document.getElementById(fieldId);
    if (field) {
        field.classList.remove('is-valid'); // 기존 성공 테두리 제거
        field.classList.add('is-invalid');
    }
    const msg = document.getElementById(msgId);
    if (msg) msg.textContent = message;
}

function clearFieldError(fieldId) {
    const field = document.getElementById(fieldId);
    if (field) {
        field.classList.remove('is-invalid'); // 기존 에러 테두리 제거
        field.classList.add('is-valid');
    }
}