var API_BASE = '/auth';

// ──────────────────── CSRF helper ────────────────────

function getCsrfTokenFromCookie() {
    var match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]*)/);
    return match ? decodeURIComponent(match[1]) : '';
}

function authHeaders(extraHeaders) {
    var headers = extraHeaders || {};
    var csrf = getCsrfTokenFromCookie();
    if (csrf) {
        headers['X-XSRF-TOKEN'] = csrf;
    }
    return headers;
}

// ──────────────────── 注册处理 ────────────────────

var registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        clearErrors();

        var username = document.getElementById('username').value.trim();
        var password = document.getElementById('password').value;
        var confirmPassword = document.getElementById('confirmPassword').value;
        var name = document.getElementById('name').value.trim();
        var role = document.getElementById('role').value;

        var hasError = false;
        if (!username) { showError('usernameError', '用户名不能为空'); hasError = true; }
        if (!password) { showError('passwordError', '密码不能为空'); hasError = true; }
        else if (password.length < 6) { showError('passwordError', '密码长度至少6位'); hasError = true; }
        if (password !== confirmPassword) { showError('confirmError', '两次输入的密码不一致'); hasError = true; }
        if (!name) { showError('nameError', '真实姓名不能为空'); hasError = true; }
        if (hasError) return;

        try {
            var response = await fetch(API_BASE + '/register', {
                method: 'POST',
                headers: authHeaders({ 'Content-Type': 'application/json' }),
                body: JSON.stringify({ username: username, password: password, name: name, role: role })
            });
            var result = await response.json();
            if (result.code === 200) {
                alert('注册成功，请登录');
                window.location.href = '/login';
            } else {
                showGlobalError(result.message || '注册失败');
            }
        } catch (error) {
            showGlobalError('网络错误，请稍后重试');
        }
    });
}

// ──────────────────── 登录处理 ────────────────────

var loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        clearErrors();

        var username = document.getElementById('username').value.trim();
        var password = document.getElementById('password').value;

        if (!username) { showError('usernameError', '用户名不能为空'); return; }
        if (!password) { showError('passwordError', '密码不能为空'); return; }

        try {
            var response = await fetch(API_BASE + '/login', {
                method: 'POST',
                headers: authHeaders({ 'Content-Type': 'application/json' }),
                body: JSON.stringify({ username: username, password: password })
            });
            var result = await response.json();
            if (result.code === 200 && result.data) {
                // Store full user info (now returned by server)
                sessionStorage.setItem('user', JSON.stringify({
                    username: result.data.username,
                    name: result.data.name,
                    role: result.data.role
                }));

                // Redirect to saved URL (if set by auth-check.js) or dashboard
                var redirectUrl = sessionStorage.getItem('redirectUrl');
                sessionStorage.removeItem('redirectUrl');
                window.location.href = redirectUrl || '/index';
            } else {
                showGlobalError(result.message || '用户名或密码错误');
            }
        } catch (error) {
            showGlobalError('网络错误，请稍后重试');
        }
    });
}

// ──────────────────── 辅助函数 ────────────────────

function showError(elementId, message) {
    var el = document.getElementById(elementId);
    if (el) { el.textContent = message; el.style.display = 'block'; }
}

function clearErrors() {
    document.querySelectorAll('.error-msg').forEach(function (el) {
        el.textContent = '';
        el.style.display = 'none';
    });
    var globalError = document.getElementById('globalError');
    if (globalError) {
        globalError.textContent = '';
        globalError.classList.remove('show');
    }
}

function showGlobalError(message) {
    var globalError = document.getElementById('globalError');
    if (globalError) {
        globalError.textContent = message;
        globalError.classList.add('show');
    }
}
