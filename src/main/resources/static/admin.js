const token = localStorage.getItem('adminToken');
if (!token) window.location.replace('/');

const state = { students: [], filtered: [], editing: null, selected: new Set() };
const $ = (selector) => document.querySelector(selector);
const table = $('#studentTable');
const totalCount = $('#totalCount');
const summary = $('#tableSummary');
const searchInput = $('#searchInput');
const clearSearch = $('#clearSearch');
const modalBackdrop = $('#modalBackdrop');
const studentForm = $('#studentForm');
const formStatus = $('#formStatus');
const selectAll = $('#selectAll');
const selectionActions = $('#selectionActions');
const selectionSummary = $('#selectionSummary');
const batchDeleteButton = $('#batchDeleteButton');
const logoutButton = $('#logoutButton');

function apiHeaders() {
  return { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token };
}

function toast(message, error) {
  const el = $('#toast');
  el.textContent = message;
  el.className = error ? 'toast show error' : 'toast show';
  window.setTimeout(() => el.classList.remove('show'), 2800);
}

function formatDate(value) {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }).replaceAll('/', '.');
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (char) => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
  }[char]));
}

function scoreText(score) {
  return score === null || score === undefined || score === ''
    ? '<span class="no-score">未录入</span>'
    : Number(score).toFixed(2);
}

function syncSelectionUi() {
  const selectedCount = state.selected.size;
  selectionActions.hidden = selectedCount === 0;
  selectionSummary.textContent = '已选择 ' + selectedCount + ' 名学生';
  const visibleStudentNos = state.filtered.map((student) => student.studentNo);
  selectAll.checked = visibleStudentNos.length > 0 && visibleStudentNos.every((studentNo) => state.selected.has(studentNo));
  selectAll.indeterminate = visibleStudentNos.some((studentNo) => state.selected.has(studentNo)) && !selectAll.checked;
}

function renderRows() {
  if (!state.filtered.length) {
    table.innerHTML = '<tr><td colspan="6" class="empty-state">没有找到匹配的学生档案</td></tr>';
    summary.textContent = '共 0 条记录';
    syncSelectionUi();
    return;
  }

  table.innerHTML = state.filtered.map((student) => {
    const checked = state.selected.has(student.studentNo) ? ' checked' : '';
    return '<tr>' +
      '<td class="select-cell"><input class="row-checkbox" type="checkbox" data-select="' + student.studentNo + '"' + checked + ' aria-label="选择学生"></td>' +
      '<td><div class="student-cell"><span class="student-avatar">' + escapeHtml((student.name || '?').slice(0, 1)) +
      '</span><div><div class="student-name">' + escapeHtml(student.name) +
      '</div><div class="student-id">查询码 · ' + escapeHtml(student.publicId || '—') +
      '</div></div></div></td>' +
      '<td><b>' + escapeHtml(student.studentNo) + '</b></td>' +
      '<td><span class="score">' + scoreText(student.score) + '</span></td>' +
      '<td>' + formatDate(student.registerTime) + '</td>' +
      '<td><div class="row-actions"><button class="row-button" data-edit="' + student.studentNo + '">编辑</button><button class="row-button delete" data-delete="' + student.studentNo + '">删除</button></div></td>' +
      '</tr>';
  }).join('');
  summary.textContent = '显示 ' + state.filtered.length + ' 条记录';
  syncSelectionUi();
}

function applySearch() {
  const keyword = searchInput.value.trim();
  state.filtered = keyword ? state.students.filter((student) => student.studentNo.includes(keyword)) : state.students.slice();
  clearSearch.hidden = !keyword;
  renderRows();
}

async function request(url, options) {
  const response = await fetch(url, { ...(options || {}), headers: { ...apiHeaders(), ...((options || {}).headers || {}) } });
  const result = await response.json().catch(() => ({}));
  if (response.status === 401 || result.code === 401) {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminInfo');
    window.location.replace('/');
    throw new Error('登录已失效');
  }
  if (!response.ok || result.code !== 200) throw new Error(result.message || '请求失败');
  return result;
}

async function loadStudents() {
  table.innerHTML = '<tr><td colspan="6" class="empty-state">正在加载学生档案…</td></tr>';
  try {
    const result = await request('/api/students');
    state.students = Array.isArray(result.data) ? result.data : [];
  } catch (error) {
    state.students = [];
    toast('加载失败：' + error.message, true);
  }
  const allStudentNos = new Set(state.students.map((student) => student.studentNo));
  state.selected.forEach((studentNo) => { if (!allStudentNos.has(studentNo)) state.selected.delete(studentNo); });
  totalCount.textContent = state.students.length;
  applySearch();
}

function openModal(student) {
  const isEditing = Boolean(student);
  state.editing = student || null;
  $('#modalEyebrow').textContent = isEditing ? 'EDIT RECORD' : 'NEW RECORD';
  $('#modalTitle').textContent = isEditing ? '编辑学生' : '新增学生';
  $('#modalSubtitle').textContent = isEditing ? '修改学生信息后保存；登记时间保持不变。' : '填写学生基本信息，保存后将自动生成查询码。';
  $('#editingNo').value = isEditing ? student.studentNo : '';
  $('#studentNo').value = isEditing ? student.studentNo : '2600';
  $('#studentNo').readOnly = isEditing;
  $('#studentName').value = isEditing ? student.name : '';
  $('#studentScore').value = isEditing && student.score !== null && student.score !== undefined ? student.score : '';
  formStatus.textContent = '';
  document.querySelectorAll('[data-error]').forEach((el) => { el.textContent = ''; });
  modalBackdrop.hidden = false;
  (isEditing ? $('#studentName') : $('#studentNo')).focus();
}

function closeModal() { modalBackdrop.hidden = true; state.editing = null; }

function validateForm() {
  document.querySelectorAll('[data-error]').forEach((el) => { el.textContent = ''; });
  let ok = true;
  const no = $('#studentNo').value.trim();
  const name = $('#studentName').value.trim();
  const score = $('#studentScore').value;
  if (!/^2600\d{6}$/.test(no)) { $('[data-error="studentNo"]').textContent = '请填写 2600 后的 6 位数字'; ok = false; }
  if (!name) { $('[data-error="studentName"]').textContent = '请输入学生姓名'; ok = false; }
  if (score !== '' && Number(score) < 0) { $('[data-error="studentScore"]').textContent = '成绩不能为负数'; ok = false; }
  return ok;
}

async function saveStudent(event) {
  event.preventDefault();
  if (!validateForm()) return;
  const editingNo = $('#editingNo').value;
  const isEditing = Boolean(editingNo);
  const payload = { studentNo: $('#studentNo').value.trim(), name: $('#studentName').value.trim(), score: $('#studentScore').value === '' ? null : Number($('#studentScore').value) };
  $('#saveButton').disabled = true;
  formStatus.textContent = '正在保存…';
  try {
    await request(isEditing ? '/api/students/' + encodeURIComponent(editingNo) : '/api/students', { method: isEditing ? 'PUT' : 'POST', body: JSON.stringify(payload) });
    toast(isEditing ? '学生档案已更新，可继续修改后再次保存。' : '学生档案已创建，可继续新增下一位学生。');
    if (!isEditing) { $('#editingNo').value = ''; $('#studentNo').value = '2600'; $('#studentName').value = ''; $('#studentScore').value = ''; $('#studentNo').focus(); }
    formStatus.textContent = '保存成功，点击取消可关闭窗口。';
    await loadStudents();
  } catch (error) { formStatus.textContent = error.message; } finally { $('#saveButton').disabled = false; }
}

async function deleteStudent(studentNo) {
  if (!window.confirm('确定删除学号为 ' + studentNo + ' 的学生吗？此操作不可撤销。')) return;
  try { await request('/api/students/' + encodeURIComponent(studentNo), { method: 'DELETE' }); toast('学生档案已删除'); await loadStudents(); } catch (error) { toast('删除失败：' + error.message, true); }
}

async function batchDeleteStudents() {
  const studentNos = Array.from(state.selected);
  if (!studentNos.length) return;
  if (!window.confirm('确定删除选中的 ' + studentNos.length + ' 名学生吗？此操作不可撤销。')) return;
  batchDeleteButton.disabled = true;
  try {
    const results = await Promise.allSettled(studentNos.map((studentNo) => request('/api/students/' + encodeURIComponent(studentNo), { method: 'DELETE' })));
    const failed = results.filter((result) => result.status === 'rejected');
    const deletedCount = studentNos.length - failed.length;
    state.selected.clear();
    if (failed.length) throw new Error('成功删除 ' + deletedCount + ' 名，另有 ' + failed.length + ' 名删除失败');
    toast('已删除 ' + deletedCount + ' 名学生');
    await loadStudents();
  } catch (error) { toast('批量删除失败：' + error.message, true); await loadStudents(); } finally { batchDeleteButton.disabled = false; }
}

async function queryStudent() {
  const no = searchInput.value.trim();
  if (!no) { applySearch(); return; }
  try { const result = await request('/api/students/' + encodeURIComponent(no)); state.filtered = [result.data]; clearSearch.hidden = false; renderRows(); } catch (error) { if (error.message !== '登录已失效') toast('查询失败：' + error.message, true); }
}

async function logout() {
  if (!window.confirm('确定退出当前登录吗？')) return;
  logoutButton.disabled = true;
  try { await fetch('/api/admin/logout', { method: 'POST', headers: { Authorization: 'Bearer ' + token } }); } finally { localStorage.removeItem('adminToken'); localStorage.removeItem('adminInfo'); window.location.replace('/'); }
}

table.addEventListener('click', (event) => {
  const edit = event.target.closest('[data-edit]');
  const del = event.target.closest('[data-delete]');
  if (edit) openModal(state.students.find((student) => student.studentNo === edit.dataset.edit));
  if (del) deleteStudent(del.dataset.delete);
});
table.addEventListener('change', (event) => {
  const checkbox = event.target.closest('[data-select]');
  if (!checkbox) return;
  if (checkbox.checked) state.selected.add(checkbox.dataset.select); else state.selected.delete(checkbox.dataset.select);
  syncSelectionUi();
});
selectAll.addEventListener('change', () => { state.filtered.forEach((student) => { if (selectAll.checked) state.selected.add(student.studentNo); else state.selected.delete(student.studentNo); }); renderRows(); });
batchDeleteButton.addEventListener('click', batchDeleteStudents);
$('#addButton').addEventListener('click', () => openModal());
$('#refreshButton').addEventListener('click', loadStudents);
$('#searchButton').addEventListener('click', queryStudent);
searchInput.addEventListener('keydown', (event) => { if (event.key === 'Enter') queryStudent(); });
clearSearch.addEventListener('click', () => { searchInput.value = ''; applySearch(); });
$('#cancelButton').addEventListener('click', closeModal);
studentForm.addEventListener('submit', saveStudent);
$('#studentNo').addEventListener('input', () => { const input = $('#studentNo'); const digits = input.value.replace(/\D/g, ''); const suffix = digits.startsWith('2600') ? digits.slice(4) : digits; input.value = ('2600' + suffix).slice(0, 10); });
$('#studentNo').addEventListener('keydown', (event) => { const input = $('#studentNo'); if ((event.key === 'Backspace' || event.key === 'Delete') && input.selectionStart <= 4 && input.selectionEnd <= 4) event.preventDefault(); });
logoutButton.addEventListener('click', logout);
const info = JSON.parse(localStorage.getItem('adminInfo') || '{}');
$('#avatar').textContent = (info.username || '管').slice(0, 1);
loadStudents();
