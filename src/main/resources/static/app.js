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
const passphraseBackdrop = document.querySelector('#passphraseBackdrop');
const passphraseForm = document.querySelector('#passphraseForm');
const accessPassphraseInput = document.querySelector('#accessPassphrase');
const passphraseError = document.querySelector('#passphraseError');
const passphraseCancel = document.querySelector('#passphraseCancel');
const passphraseConfirm = document.querySelector('#passphraseConfirm');

let mode = 'login';
let pendingCredentials = null;

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
  document.querySelectorAll('.field-error').forEach((item) => { item.textContent = ''; });
}

function showStatus(message, kind) {
  statusMessage.textContent = message;
  statusMessage.className = kind ? 'status-message ' + kind : 'status-message';
}

function setFieldError(name, message) {
  const target = document.querySelector('[data-error-for="' + name + '"]');
  if (target) target.textContent = message;
}

function validate() {
  clearFeedback();
  let valid = true;
  const username = usernameInput.value.trim();
  const password = passwordInput.value;
  if (!username) {
    setFieldError('username', '请输入管理员姓名');
    valid = false;
  }
  if (!password.trim()) {
    setFieldError('password', '请输入密码');
    valid = false;
  } else if (mode === 'register' && password.length < 6) {
    setFieldError('password', '密码至少需要 6 位');
    valid = false;
  }
  return valid;
}

function openPassphraseDialog() {
  passphraseError.textContent = '';
  accessPassphraseInput.value = '';
  passphraseBackdrop.hidden = false;
  window.setTimeout(() => accessPassphraseInput.focus(), 50);
}

function closePassphraseDialog() {
  passphraseBackdrop.hidden = true;
  pendingCredentials = null;
  submitButton.disabled = false;
  submitText.textContent = mode === 'register' ? '注册并登录' : '登录';
}

async function performAuth(passphrase) {
  const credentials = pendingCredentials;
  if (!credentials) return;

  passphraseConfirm.disabled = true;
  passphraseConfirm.textContent = '验证中…';
  passphraseError.textContent = '';

  try {
    const passphraseHeaders = { 'X-Access-Passphrase': passphrase };
    const verifyResponse = await fetch('/api/admin/verify-passphrase', {
      method: 'POST',
      headers: passphraseHeaders
    });
    const verifyResult = await verifyResponse.json().catch(() => ({}));
    if (!verifyResponse.ok || verifyResult.code !== 200) {
      throw new Error('暗号不正确，请重新输入');
    }

    const response = await fetch('/api/admin/' + credentials.mode, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'X-Access-Passphrase': passphrase },
      body: JSON.stringify({ username: credentials.username, password: credentials.password })
    });
    const result = await response.json().catch(() => ({}));
    if (!response.ok || result.code !== 200 || !result.data || !result.data.token) {
      throw new Error(result.message || '操作失败，请稍后重试');
    }

    localStorage.setItem('adminToken', result.data.token);
    localStorage.setItem('adminInfo', JSON.stringify(result.data.admin || {}));
    if ((credentials.mode === 'login' && document.querySelector('#remember').checked) || credentials.mode === 'register') {
      localStorage.setItem('rememberAdmin', credentials.username);
    }
    passphraseBackdrop.hidden = true;
    showStatus(credentials.mode === 'register' ? '注册成功，正在进入管理后台…' : '登录成功，正在进入管理后台…', 'success');
    window.setTimeout(() => { window.location.href = '/admin.html'; }, 450);
  } catch (error) {
    passphraseError.textContent = error.message || '验证失败，请稍后重试';
    accessPassphraseInput.select();
  } finally {
    passphraseConfirm.disabled = false;
    passphraseConfirm.textContent = '验证并继续';
    submitButton.disabled = false;
    submitText.textContent = mode === 'register' ? '注册并登录' : '登录';
  }
}

function submitAuth(event) {
  event.preventDefault();
  if (!validate()) return;
  pendingCredentials = { mode, username: usernameInput.value.trim(), password: passwordInput.value };
  submitButton.disabled = true;
  openPassphraseDialog();
}

passwordToggle.addEventListener('click', () => {
  const visible = passwordInput.type === 'text';
  passwordInput.type = visible ? 'password' : 'text';
  passwordToggle.setAttribute('aria-pressed', String(!visible));
  passwordToggle.setAttribute('aria-label', visible ? '显示密码' : '隐藏密码');
});

passphraseForm.addEventListener('submit', (event) => {
  event.preventDefault();
  if (!accessPassphraseInput.value.trim()) {
    passphraseError.textContent = '请输入暗号';
    return;
  }
  performAuth(accessPassphraseInput.value);
});
passphraseCancel.addEventListener('click', closePassphraseDialog);
registerButton.addEventListener('click', () => setMode(mode === 'login' ? 'register' : 'login'));
backToLogin.addEventListener('click', () => setMode('login'));
forgotButton.addEventListener('click', () => showStatus('请联系系统管理员重置密码。'));
form.addEventListener('submit', submitAuth);

const rememberedAdmin = localStorage.getItem('rememberAdmin');
if (rememberedAdmin) {
  usernameInput.value = rememberedAdmin;
  document.querySelector('#remember').checked = true;
}
