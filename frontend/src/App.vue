<script setup>
import { onMounted, ref } from 'vue'

const devices = ref([])
const loading = ref(true)
const errorMessage = ref('')
const submitting = ref(false)
const formMessage = ref('')
const formError = ref('')
const editingId = ref(null)
const form = ref({
  code: '',
  name: '',
  model: '',
  responsiblePerson: '',
  status: 'IN_USE',
})

const statusLabels = {
  IN_USE: '使用中',
  MAINTENANCE: '维修中',
  RETIRED: '已停用',
}

function statusLabel(status) {
  return statusLabels[status] ?? status ?? '未知'
}

async function loadDevices() {
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await fetch('/api/devices')
    if (!response.ok) {
      throw new Error(`请求失败：HTTP ${response.status}`)
    }
    devices.value = await response.json()
  } catch (error) {
    errorMessage.value = `${error.message}。请确认 Spring Boot 后端已在 8080 端口运行。`
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.value = { code: '', name: '', model: '', responsiblePerson: '', status: 'IN_USE' }
  editingId.value = null
}

function startEdit(device) {
  editingId.value = device.id
  formMessage.value = ''
  formError.value = ''
  form.value = {
    code: device.code ?? '',
    name: device.name ?? '',
    model: device.model ?? '',
    responsiblePerson: device.responsiblePerson ?? '',
    status: device.status ?? 'IN_USE',
  }
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function saveDevice() {
  submitting.value = true
  formMessage.value = ''
  formError.value = ''

  try {
    const response = await fetch(editingId.value ? `/api/devices/${editingId.value}` : '/api/devices', {
      method: editingId.value ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form.value),
    })
    if (!response.ok) {
      const body = await response.json().catch(() => null)
      throw new Error(body?.message || `新增失败：HTTP ${response.status}`)
    }
    formMessage.value = editingId.value ? '设备已更新' : '设备已新增'
    resetForm()
    await loadDevices()
  } catch (error) {
    formError.value = error.message
  } finally {
    submitting.value = false
  }
}

async function deleteDevice(device) {
  if (!window.confirm(`确定删除设备“${device.name}”吗？`)) return

  formMessage.value = ''
  formError.value = ''
  try {
    const response = await fetch(`/api/devices/${device.id}`, { method: 'DELETE' })
    if (!response.ok) {
      const body = await response.json().catch(() => null)
      throw new Error(body?.message || `删除失败：HTTP ${response.status}`)
    }
    formMessage.value = '设备已删除'
    if (editingId.value === device.id) resetForm()
    await loadDevices()
  } catch (error) {
    formError.value = error.message
  }
}

onMounted(loadDevices)
</script>

<template>
  <main class="app-shell">
    <header class="page-header">
      <div>
        <p class="eyebrow">EQUIPMENT LEDGER / DAY 7</p>
        <h1>设备台账</h1>
        <p class="subtitle">集中查看设备基础信息与当前状态</p>
      </div>
      <button class="refresh-button" type="button" :disabled="loading" @click="loadDevices">
        {{ loading ? '加载中...' : '刷新列表' }}
      </button>
    </header>

    <section class="summary-bar" aria-label="设备概览">
      <span>设备总数</span>
      <strong>{{ devices.length }}</strong>
    </section>

    <section class="form-section">
      <div class="section-heading">
        <div>
        <h2>{{ editingId ? '编辑设备' : '新增设备' }}</h2>
          <p>{{ editingId ? '修改设备信息后保存。' : '填写编号和名称，其他信息可以稍后补充。' }}</p>
        </div>
      </div>
      <form class="device-form" @submit.prevent="saveDevice">
        <label>设备编号<input v-model.trim="form.code" required placeholder="例如 EQ-001" /></label>
        <label>设备名称<input v-model.trim="form.name" required placeholder="例如 数控车床" /></label>
        <label>型号<input v-model.trim="form.model" placeholder="例如 CK-6150" /></label>
        <label>负责人<input v-model.trim="form.responsiblePerson" placeholder="例如 张师傅" /></label>
        <label>状态
          <select v-model="form.status">
            <option value="IN_USE">使用中</option>
            <option value="MAINTENANCE">维修中</option>
            <option value="RETIRED">已停用</option>
          </select>
        </label>
        <div class="form-actions">
          <button class="submit-button" type="submit" :disabled="submitting">
            {{ submitting ? '保存中...' : editingId ? '保存修改' : '保存设备' }}
          </button>
          <button v-if="editingId" class="cancel-button" type="button" @click="resetForm">取消</button>
        </div>
      </form>
      <p v-if="formMessage" class="form-feedback success">{{ formMessage }}</p>
      <p v-if="formError" class="form-feedback failure" role="alert">{{ formError }}</p>
    </section>

    <p v-if="errorMessage" class="message error" role="alert">{{ errorMessage }}</p>
    <p v-else-if="loading" class="message">正在加载设备数据...</p>
    <p v-else-if="devices.length === 0" class="message">暂无设备记录</p>

    <section v-else class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>设备编号</th>
            <th>设备名称</th>
            <th>型号</th>
            <th>状态</th>
            <th>负责人</th>
            <th>车间 ID</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="device in devices" :key="device.id">
            <td class="code">{{ device.code }}</td>
            <td>{{ device.name }}</td>
            <td>{{ device.model || '未填写' }}</td>
            <td><span class="status" :data-status="device.status">{{ statusLabel(device.status) }}</span></td>
            <td>{{ device.responsiblePerson || '未指定' }}</td>
            <td>{{ device.workshopId ?? '未关联' }}</td>
            <td class="row-actions">
              <button type="button" class="link-button" @click="startEdit(device)">编辑</button>
              <button type="button" class="link-button danger" @click="deleteDevice(device)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </section>
  </main>
</template>

<style>
:root {
  color: #1b2838;
  background: #f4f6f8;
  font-family: Inter, "Segoe UI", "Microsoft YaHei", sans-serif;
  font-synthesis: none;
}

* { box-sizing: border-box; }
body { margin: 0; min-width: 320px; }
button { font: inherit; }

.app-shell { max-width: 1180px; margin: 0 auto; padding: 48px 28px; }
.page-header { display: flex; justify-content: space-between; align-items: end; gap: 24px; margin-bottom: 28px; }
.eyebrow { margin: 0 0 10px; color: #39756f; font-size: 12px; font-weight: 700; letter-spacing: 1px; }
h1 { margin: 0; color: #152538; font-size: 36px; line-height: 1.1; }
.subtitle { margin: 10px 0 0; color: #667487; }
.refresh-button { border: 1px solid #39756f; border-radius: 6px; padding: 10px 16px; color: #fff; background: #39756f; cursor: pointer; }
.refresh-button:disabled { cursor: wait; opacity: .65; }
.summary-bar { display: flex; align-items: baseline; gap: 12px; padding: 18px 20px; border: 1px solid #dce3e8; border-radius: 6px; background: #fff; }
.summary-bar span { color: #667487; }
.summary-bar strong { color: #152538; font-size: 28px; }
.form-section { margin-top: 18px; padding: 22px; border: 1px solid #dce3e8; border-radius: 6px; background: #fff; }
.section-heading h2 { margin: 0; color: #152538; font-size: 20px; }
.section-heading p { margin: 6px 0 0; color: #667487; font-size: 14px; }
.device-form { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)) auto; gap: 14px; align-items: end; margin-top: 20px; }
.device-form label { display: grid; gap: 7px; color: #526275; font-size: 13px; font-weight: 600; }
.device-form input, .device-form select { width: 100%; min-width: 0; border: 1px solid #cbd5dc; border-radius: 4px; padding: 10px; color: #1b2838; background: #fff; font: inherit; }
.device-form input:focus, .device-form select:focus { outline: 2px solid #b9ddd7; border-color: #39756f; }
.form-actions { display: flex; gap: 8px; }
.submit-button, .cancel-button { border: 1px solid #152538; border-radius: 4px; padding: 10px 14px; cursor: pointer; white-space: nowrap; }
.submit-button { color: #fff; background: #152538; }
.cancel-button { color: #526275; border-color: #cbd5dc; background: #fff; }
.submit-button:disabled { cursor: wait; opacity: .65; }
.form-feedback { margin: 14px 0 0; font-size: 14px; }
.form-feedback.success { color: #27635d; }
.form-feedback.failure { color: #a03c3c; }
.table-wrap { margin-top: 18px; overflow-x: auto; border: 1px solid #dce3e8; border-radius: 6px; background: #fff; }
table { width: 100%; border-collapse: collapse; text-align: left; }
th, td { padding: 15px 18px; border-bottom: 1px solid #edf0f2; white-space: nowrap; }
th { color: #667487; background: #f8fafb; font-size: 13px; font-weight: 600; }
tbody tr:last-child td { border-bottom: 0; }
.code { color: #39756f; font-weight: 700; }
.status { display: inline-block; padding: 4px 8px; border-radius: 4px; color: #27635d; background: #e4f3ef; font-size: 13px; }
.status[data-status="MAINTENANCE"] { color: #8b5d14; background: #fff1d5; }
.status[data-status="RETIRED"] { color: #6b7280; background: #eef0f2; }
.row-actions { display: flex; gap: 12px; }
.link-button { border: 0; padding: 0; color: #39756f; background: transparent; cursor: pointer; }
.link-button.danger { color: #a03c3c; }
.message { margin: 18px 0 0; padding: 28px; border: 1px dashed #cbd5dc; border-radius: 6px; color: #667487; background: #fff; text-align: center; }
.message.error { border-color: #e6b8b8; color: #a03c3c; background: #fff8f8; }

@media (max-width: 640px) {
  .app-shell { padding: 28px 16px; }
  .page-header { align-items: start; flex-direction: column; }
  h1 { font-size: 30px; }
  .refresh-button { width: 100%; }
  .device-form { grid-template-columns: 1fr; }
  .form-actions { flex-direction: column; }
  .submit-button, .cancel-button { width: 100%; }
}
</style>
