/**
 * 회원가입 양식(Form) 유효성 검사 및 에러 메시지 제어 스크립트
 */

/**
 * [메인 검증 함수] 입력된 값들을 정규식으로 검사하고 통과 시 확인 모달을 띄움
 */
function validateAndShowModal() {
    let valid = true;

    // DOM 요소에서 입력값 가져오기 (양 끝 공백은 자동으로 제거)
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('passwordConfirm').value;
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();

    // ① 아이디 검증 : 4~20자의 영문 대소문자 및 숫자 조합
    const usernameRegex = /^[a-zA-Z0-9]{4,20}$/;
    if (!usernameRegex.test(username)) {
        showError('username', '아이디는 4~20자 영문/숫자만 가능합니다.');
        valid = false;
    } else {
        clearError('username');
    }

    // ② 패스워드 검증 : 최소 8자 이상, 영문+숫자+특수문자(!@#$%^&*) 조합 필수
    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$/;
    if (!passwordRegex.test(password)) {
        showError('password', '8자 이상, 영문+숫+특수문자를 모두 포함해야 합니다.');
        valid = false;
    } else {
        clearError('password');
    }

    // ③ 패스워드 재확인 : 위에서 입력한 패스워드와 정확히 일치하는지 대조
    if (password !== passwordConfirm) {
        showError('passwordConfirm', '패스워드가 일치하지 않습니다.');
        valid = false;
    } else {
        clearError('passwordConfirm');
    }

    // ④ 이메일 형식 검증 : 표준 이메일 포맷 (@ 및 도메인 존재 여부)
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showError('email', '올바른 이메일 형식이 아닙니다.');
        valid = false;
    } else {
        clearError('email');
    }

    // ⑤ 연락처 형식 검증 : 대한민국 핸드폰 번호 포맷 (010-XXXX-XXXX) 필수
    const phoneRegex = /^010-\d{4}-\d{4}$/;
    if (!phoneRegex.test(phone)) {
        showError('phone', '010-0000-0000 형식으로 입력해주세요.');
        valid = false;
    } else {
        clearError('phone');
    }

    // 모든 유효성 검사를 통과한 경우에만 최종 확인 모달창 호출
    if (valid) {
        showConfirmModal();
    }
}

/**
 * 입력 오류 발생 시 부트스트랩 경고 스타일을 적용하고 메시지를 출력하는 함수
 */
function showError(fieldId, message) {
    const field = document.getElementById(fieldId);
    if (!field) return;

    field.classList.remove('is-valid'); // [버그 수정] 기존의 성공 스타일 제거
    field.classList.add('is-invalid');  // 에러 스타일(빨간 테두리) 추가
    
    const msg = document.getElementById(fieldId + 'Msg');
    if (msg) {
        msg.textContent = message;      // 안내 메시지 주입
    }
}

/**
 * 유효성 검사 통과 시 에러 스타일을 걷어내고 성공 스타일을 적용하는 함수
 */
function clearError(fieldId) {
    const field = document.getElementById(fieldId);
    if (!field) return;

    field.classList.remove('is-invalid'); // 에러 스타일 제거
    field.classList.add('is-valid');     // 성공 스타일(초록 테두리) 추가
}

/**
 * [JS 로드 순서 연동] 페이지 로딩 시 URL 파라미터를 분석하여 백엔드에서 넘어온 중복 에러 처리
 */
document.addEventListener('DOMContentLoaded', function() {
    const params = new URLSearchParams(window.location.search);
    const error = params.get('error');

    // 백엔드(AuthResource.java)의 리다이렉트 에러 파라미터와 매핑
    if (error === 'duplicate_username') {
        showError('username', '이미 사용 중인 아이디입니다.');
    } else if (error === 'duplicate_email') {
        showError('email', '이미 사용 중인 이메일입니다.');
    }
});