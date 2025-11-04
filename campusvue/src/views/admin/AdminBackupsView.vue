<template>
  <section class="card">
    <div class="card__title">
      <h2>数据备份</h2>
      <button class="outline" @click="loadBackups">刷新</button>
    </div>

    <form class="form-grid" @submit.prevent="createBackup">
      <label>备份名称<input v-model="backupForm.name" required maxlength="100" /></label>
      <label class="full">备注<textarea v-model="backupForm.description" maxlength="255" placeholder="记录本次备份的背景（可选）"></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">创建备份</button>
        <button class="outline" type="button" @click="resetForm">重置</button>
      </div>
    </form>

    <table v-if="backups.length" class="table">
      <thead><tr><th>ID</th><th>名称</th><th>创建时间</th><th>创建人</th><th>备注</th></tr></thead>
      <tbody>
        <tr v-for="item in backups" :key="item.id">
          <td>{{ item.id }}</td>
          <td>{{ item.name }}</td>
          <td>{{ formatDate(item.createdAt) }}</td>
          <td>{{ item.adminName || '-' }}</td>
          <td>{{ item.description || '-' }}</td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">尚未生成数据备份</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, post } from '../../api/http';

const backups = ref([]);
const feedback = reactive({ message: '', type: 'info' });

const backupForm = reactive({
  name: '',
  description: ''
});

onMounted(() => {
  loadBackups();
});

async function loadBackups() {
  try {
    backups.value = await get('/portal/admin/backups');
  } catch (error) {
    showFeedback(error.message ?? '加载备份信息失败', 'error');
  }
}

async function createBackup() {
  if (!backupForm.name) {
    showFeedback('请输入备份名称', 'error');
    return;
  }
  try {
    await post('/portal/admin/backups', backupForm);
    showFeedback('备份任务已创建', 'success');
    resetForm();
    await loadBackups();
  } catch (error) {
    showFeedback(error.message ?? '创建备份失败', 'error');
  }
}

function resetForm() {
  backupForm.name = '';
  backupForm.description = '';
}

function formatDate(value) {
  if (!value) {
    return '-';
  }
  return new Date(value).toLocaleString();
}

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
  }
}
</script>

<style scoped>
.feedback {
  padding: 10px 14px;
  border-radius: 12px;
  text-align: center;
}

.feedback.success {
  background: #dcfce7;
  color: #15803d;
}

.feedback.error {
  background: #fee2e2;
  color: #b91c1c;
}
</style>
