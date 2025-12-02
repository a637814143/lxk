<template>
  <section class="card">
    <div class="card__title">
      <h2>数据备份</h2>
      <div class="actions">
        <button class="outline" @click="loadBackups">刷新</button>
      </div>
    </div>

    <div class="form-grid two-cols">
      <form class="form-card" @submit.prevent="createBackup">
        <h3>创建备份</h3>
        <label>备份标签
          <input v-model="backupForm.backupType" required maxlength="100" placeholder="例如：daily / before-release" />
        </label>
        <label class="full">备注
          <textarea v-model="backupForm.message" maxlength="255" placeholder="记录本次备份的背景（可选）"></textarea>
        </label>
        <div class="full actions">
          <button class="primary" type="submit">创建备份</button>
          <button class="outline" type="button" @click="resetForm">重置</button>
        </div>
      </form>

      <form class="form-card" @submit.prevent="restoreBackup">
        <h3>上传恢复</h3>
        <label>备份文件
          <input type="file" @change="onFileChange" required />
        </label>
        <label>标签（可选）
          <input v-model="restoreForm.backupType" maxlength="100" placeholder="restore / import / 手工上传" />
        </label>
        <label class="full">备注
          <textarea v-model="restoreForm.message" maxlength="255" placeholder="说明恢复/上传的背景（可选）"></textarea>
        </label>
        <div class="full actions">
          <button class="primary" type="submit">上传恢复</button>
          <button class="outline" type="button" @click="resetRestore">重置</button>
        </div>
      </form>
    </div>

    <table v-if="backups.length" class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>标签</th>
          <th>状态</th>
          <th>大小</th>
          <th>创建时间</th>
          <th>创建人</th>
          <th>备注</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in backups" :key="item.id">
          <td>{{ item.id }}</td>
          <td>{{ item.backupType || '-' }}</td>
          <td><span class="pill" :class="`pill--${(item.status || 'unknown').toLowerCase()}`">{{ translateStatus(item.status) }}</span></td>
          <td>{{ formatSize(item.sizeBytes) }}</td>
          <td>{{ formatDate(item.createdAt) }}</td>
          <td>{{ item.adminName || '-' }}</td>
          <td>{{ item.message || '-' }}</td>
          <td class="table-actions">
            <a v-if="item.downloadUrl" :href="item.downloadUrl" target="_blank" rel="noreferrer">下载</a>
            <button class="outline" type="button" @click="deleteBackup(item.id)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">尚未生成数据备份</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, post, upload, del as httpDelete } from '../../api/http';
import { useToast } from '../../ui/toast';

const backups = ref([]);
const backupForm = reactive({ backupType: '', message: '' });
const restoreForm = reactive({ file: null, backupType: 'restore', message: '' });
const toast = useToast();

onMounted(loadBackups);

async function loadBackups() {
  try {
    backups.value = await get('/portal/admin/backups');
  } catch (error) {
    toast.error(error.message ?? '加载备份信息失败');
  }
}

async function createBackup() {
  if (!backupForm.backupType) {
    toast.error('请输入备份标签');
    return;
  }
  try {
    await post('/portal/admin/backups', backupForm);
    toast.success('备份任务已创建');
    resetForm();
    await loadBackups();
  } catch (error) {
    toast.error(error.message ?? '创建备份失败');
  }
}

function resetForm() {
  backupForm.backupType = '';
  backupForm.message = '';
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString();
}

function formatSize(bytes) {
  const val = Number(bytes || 0);
  if (!val) return '-';
  if (val >= 1024 * 1024) return (val / (1024 * 1024)).toFixed(2) + ' MB';
  if (val >= 1024) return (val / 1024).toFixed(2) + ' KB';
  return val + ' B';
}

function translateStatus(status) {
  const s = (status || '').toLowerCase();
  if (s === 'completed') return '完成';
  if (s === 'failed') return '失败';
  if (s === 'restored') return '恢复/上传';
  return status || '-';
}

function onFileChange(event) {
  restoreForm.file = event.target.files?.[0] ?? null;
}

async function restoreBackup() {
  if (!restoreForm.file) {
    toast.error('请先选择备份文件');
    return;
  }
  try {
    const fd = new FormData();
    fd.append('file', restoreForm.file);
    if (restoreForm.backupType) fd.append('backupType', restoreForm.backupType);
    if (restoreForm.message) fd.append('message', restoreForm.message);
    await upload('/portal/admin/backups/restore', fd);
    toast.success('备份文件已上传');
    resetRestore();
    await loadBackups();
  } catch (error) {
    toast.error(error.message ?? '上传/恢复失败');
  }
}

function resetRestore() {
  restoreForm.file = null;
  restoreForm.backupType = 'restore';
  restoreForm.message = '';
}

async function deleteBackup(id) {
  if (!id) return;
  if (!confirm('确定要删除该备份吗？文件和记录将一并移除。')) return;
  try {
    await httpDelete(`/portal/admin/backups/${id}`);
    await loadBackups();
    toast.success('已删除备份');
  } catch (error) {
    toast.error(error.message ?? '删除备份失败');
  }
}
</script>

<style scoped>
.form-grid.two-cols {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
}

.form-card {
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px 14px;
  display: grid;
  gap: 10px;
}

.actions { display: flex; gap: 12px; }

.table-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid #e5e7eb;
  background: #f8fafc;
}

.pill--completed { background: #dcfce7; border-color: #bbf7d0; color: #166534; }
.pill--failed { background: #fee2e2; border-color: #fecdd3; color: #b91c1c; }
.pill--restored { background: #e0f2fe; border-color: #bae6fd; color: #0ea5e9; }
</style>
