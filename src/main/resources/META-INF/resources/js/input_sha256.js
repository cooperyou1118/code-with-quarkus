/**
 * 비밀번호 암호화(해싱) 및 회원가입 폼 전송 제어 스크립트
 */

// SHA-256 해시 함수 (브라우저 내장 Web Crypto API 활용)
async function hashPassword(password) {
    const encoder = new TextEncoder();
    const data = encoder.encode(password);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    const hashArray = Array.from(new Uint8Array(hashBuffer));
    return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

// 폼 유효성 통과 후 확인 모달 출력 및 해시 생성
async function showConfirmModal() {
    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const password = document.getElementById('password').value;

    // 모달 내부에 사용자가 입력한 정보 텍스트 바인딩
    document.getElementById('confirmUsername').textContent = username;
    document.getElementById('confirmEmail').textContent = email;
    document.getElementById('confirmPhone').textContent = phone;

    // 패스워드 평문을 SHA-256으로 해싱하여 hidden 필드에 장전
    const hashed = await hashPassword(password);
    document.getElementById('hashedPassword').value = hashed;

    // 디버깅용: F12 개발자 콘솔에서 해시값 정상 변환 여부 확인
    console.log('=== 생성된 보안 해시값 ===\n', hashed);

    // [버그 수정] new bootstrap.Modal() 대신 getOrCreateInstance() 사용
    // (모달을 여러 번 열고 닫아도 화면이 먹통이 되지 않도록 싱글톤 패턴 적용)
    const modalElement = document.getElementById('confirmModal');
    const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
    modal.show();
}

// 모달 내 '가입하기' 최종 버튼 클릭 시 작동하는 폼 전송 함수
function submitRegister() {
    const modalElement = document.getElementById('confirmModal');
    const modal = bootstrap.Modal.getInstance(modalElement);
    
    // 1. 확인 모달 부드럽게 닫기
    if (modal) {
        modal.hide();
    }

    // 2. [보안 수정] 폼 전송 직전, 평문 비밀번호가 담긴 원본 input 태그 비활성화
    // (서버로 평문 password가 날아가는 것을 방지하고 hashedPassword만 전송되도록 유도)
    const rawPasswordInput = document.getElementById('password');
    if (rawPasswordInput) {
        rawPasswordInput.removeAttribute('name'); 
    }

    // 3. 안전하게 가공된 폼 데이터를 백엔드(POST /register_check)로 전송
    document.getElementById('registerForm').submit();
}