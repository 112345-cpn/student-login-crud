const form = document.querySelector('#authForm');
const usernameInput = document.querySelector('#username');
const passwordInput = document.querySelector('#password');
const submitButton = document.querySelector('#submitButton');
const submitText = document.querySelector('#submitText');
const statusMessage = document.querySelector('#statusMessage');
const formTitle = document.querySelector('#formTitle');
const formSubtitle = document.querySelector('#formSubtitle');
const loginOptions = document.querySelector('#loginOptions');
const registerTip = document.querySelector('#registerTip');
const bottomPrompt = document.querySelector('#bottomPrompt');
const registerButton = document.querySelector('#registerButton');
const backToLogin = document.querySelector('#backToLogin');
const forgotButton = document.querySelector('#forgotButton');
const passwordToggle = document.querySelector('.password-toggle');

let mode = 'login';

function setMode(nextMode) {
  mode = nextMode;
  const isRegister = mode === 'register';
  formTitle.textContent = isRegister ? '创建管理员账户' : '欢迎回来';
  formSubtitle.textContent = isRegister ? '注册完成后将自动登录管理后台。' : '登录你的管理账户，开始今天的工作。';
  submitText.textContent = isRegister ? '注册并登录' : '登录';
  loginOptions.hidden = isRegister;
  registerTip.hidden = !isRegister;
  bottomPrompt.textContent = isRegister ? '已经有管理员账户？' : '还没有管理员账户？';
  registerButton.innerHTML = isRegister ? '返回登录 <span>↗</span>' : '立即注册 <span>↗</span>';
  passwordInput.autocomplete = isRegister ? 'new-password' : 'current-password';
  clearFeedback();
  usernameInput.focus();
}

function clearFeedback() {
  statusMessage.textContent = '';
  statusMessage.className = 'status-message';
  document.querySelectorAll('.field-error').forEach((item) => item.textContent = '');
}

function showStatus(message, kind = 'error') {
  statusMessage.textContent = message;
  statusMessage.className = `status-message ${kind}`;
}

function setFieldError(name, message) {
  const target = document.querySelector(`[data-error-for="${name}"]`);
  if (target) target.textContent = message;
}

function validate() {
  clearFeedback();
  let valid = true;
  const username = usernameInput.value.trim();
  const password = passwordInput.value;
  if (!username) { setFieldError('username', '请输入管理员姓名'); valid = false; }
  if (!password.trim()) { setFieldError('password', '请输入密码'); valid = false; }
  else if (mode === 'register' && password.length < 6) { setFieldError('password', '密码至少需要 6 位'); valid = false; }
  return valid;
}

async function submitAuth(event) {
  event.preventDefault();
  if (!validate()) return;

  submitButton.disabled = true;
  submitText.textContent = mode === 'register' ? '创建中…' : '登录中…';
  showStatus('正在验证账户信息…', '');

  try {
    const response = await fetch(`/api/admin/${mode}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: usernameInput.value.trim(), password: passwordInput.value })
    });
    const result = await response.json().catch(() => ({}));
    if (!response.ok || result.code !== 200 || !result.data?.token) {
      throw new Error(result.message || (response.status === 409 ? '该管理员姓名已存在' : '操作失败，请稍后重试'));
    }

    localStorage.setItem('adminToken', result.data.token);
    localStorage.setItem('adminInfo', JSON.stringify(result.data.admin || {}));
    if (mode === 'login' && document.querySelector('#remember').checked) {
      localStorage.setItem('rememberAdmin', usernameInput.value.trim());
    } else if (mode === 'register') {
      localStorage.setItem('rememberAdmin', usernameInput.value.trim());
    }
    showStatus(mode === 'register' ? '注册成功，正在进入管理后台…' : '登录成功，正在进入管理后台…', 'success');
    window.setTimeout(() => { window.location.href = '/admin.html'; }, 500);
  } catch (error) {
    showStatus(error.message || '网络异常，请检查服务是否启动');
  } finally {
    submitButton.disabled = false;
    if (document.body.contains(submitText)) submitText.textContent = mode === 'register' ? '注册并登录' : '登录';
  }
}

passwordToggle.addEventListener('click', () => {
  const visible = passwordInput.type === 'text';
  passwordInput.type = visible ? 'password' : 'text';
  passwordToggle.setAttribute('aria-pressed', String(!visible));
  passwordToggle.setAttribute('aria-label', visible ? '显示密码' : '隐藏密码');
});
registerButton.addEventListener('click', () => setMode(mode === 'login' ? 'register' : 'login'));
backToLogin.addEventListener('click', () => setMode('login'));
forgotButton.addEventListener('click', () => showStatus('请联系系统管理员重置密码。'));
form.addEventListener('submit', submitAuth);

const rememberedAdmin = localStorage.getItem('rememberAdmin');
if (rememberedAdmin) {
  usernameInput.value = rememberedAdmin;
  document.querySelector('#remember').checked = true;
}
