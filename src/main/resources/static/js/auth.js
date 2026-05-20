const API_BASE = '/auth';

// 注册处理
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearErrors();

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const name = document.getElementById('name').value.trim();
        const role = document.getElementById('role').value;

        let hasError = false;
        if (!username) { showError('usernameError', '用户名不能为空'); hasError = true; }
        if (!password) { showError('passwordError', '密码不能为空'); hasError = true; }
        else if (password.length < 6) { showError('passwordError', '密码长度至少6位'); hasError = true; }
        if (password !== confirmPassword) { showError('confirmError', '两次输入的密码不一致'); hasError = true; }
        if (!name) { showError('nameError', '真实姓名不能为空'); hasError = true; }
        if (hasError) return;

        try {
            const response = await fetch(`${API_BASE}/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password, name, role })
            });
            const result = await response.json();
            if (result.code === 200) {
                alert('注册成功，请登录');
                window.location.href = '/login';   // 注意：没有 .html
            } else {
                showGlobalError(result.message || '注册失败');
            }
        } catch (error) {
            showGlobalError('网络错误，请稍后重试');
        }
    });
}

// 登录处理
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearErrors();

        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        if (!username) { showError('usernameError', '用户名不能为空'); return; }
        if (!password) { showError('passwordError', '密码不能为空'); return; }

        try {
            const response = await fetch(`${API_BASE}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            const result = await response.json();
            if (result.code === 200) {
                const user = { username, name: username };
                sessionStorage.setItem('user', JSON.stringify(user));
                window.location.href = '/index';   // 注意：没有 .html
            } else {
                showGlobalError(result.message || '用户名或密码错误');
            }
        } catch (error) {
            showGlobalError('网络错误，请稍后重试');
        }
    });
}

// 辅助函数（与之前相同）
function showError(elementId, message) {
    const el = document.getElementById(elementId);
    if (el) { el.textContent = message; el.style.display = 'block'; }
}
function clearErrors() {
    document.querySelectorAll('.error-msg').forEach(el => {
        el.textContent = '';
        el.style.display = 'none';
    });
    const globalError = document.getElementById('globalError');
    if (globalError) {
        globalError.textContent = '';
        globalError.classList.remove('show');
    }
}
function showGlobalError(message) {
    const globalError = document.getElementById('globalError');
    if (globalError) {
        globalError.textContent = message;
        globalError.classList.add('show');
    }
}