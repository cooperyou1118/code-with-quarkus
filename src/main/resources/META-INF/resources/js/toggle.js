
// 페이지 로드 시 초기 테마 설정
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('themeToggleBtn');
    if (!btn) return;

    // 저장된 테마 불러오기 (없으면 'dark' 기본)
    applyTheme(localStorage.getItem('theme') || 'dark');

    // 버튼 클릭 이벤트 (여기서만 테마를 전환합니다)
    btn.addEventListener('click', () => {
        const isCurrentlyLight = document.body.classList.contains('light-mode');
        const nextTheme = isCurrentlyLight ? 'dark' : 'light';
        applyTheme(nextTheme);
        localStorage.setItem('theme', nextTheme);
    });
});

// 테마 적용 함수 (클래스 조작)
function applyTheme(theme) {
    const body = document.body;
    const btn = document.getElementById('themeToggleBtn');
    const navbar = document.querySelector('.navbar');

    if (theme === 'light') {
        body.classList.add('light-mode');
        if (btn) btn.textContent = '☀️ LIGHT';
        if (navbar) {
            navbar.classList.remove('navbar-dark', 'bg-dark');
            navbar.classList.add('navbar-light', 'bg-light');
        }
    } else {
        body.classList.remove('light-mode');
        if (btn) btn.textContent = '🌙 DARK';
        if (navbar) {
            navbar.classList.remove('navbar-light', 'bg-light');
            navbar.classList.add('navbar-dark', 'bg-dark');
        }
    }
}