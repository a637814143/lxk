<template>
  <section class="card">
    <div class="card__title">
      <h2>用户管理</h2>
      <button class="outline" @click="loadUsers">刷新</button>
    </div>

    <table v-if="users.length" class="table">
      <thead><tr><th>ID</th><th>用户名</th><th>角色</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.username }}</td>
          <td>{{ user.role }}</td>
          <td>{{ user.status === 1 ? '启用' : '禁用' }}</td>
          <td class="actions">
            <button class="outline" @click="toggleUserStatus(user)">
              {{ user.status === 1 ? '禁用账号' : '恢复账号' }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无用户数据</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, patch } from '../../api/http';

const users = ref([]);
const feedback = reactive({ message: '', type: 'info' });

onMounted(() => {
  loadUsers();
});

async function loadUsers() {
  try {
    users.value = await get('/portal/admin/users');
  } catch (error) {
    showFeedback(error.message ?? '加载用户失败', 'error');
  }
}

async function toggleUserStatus(user) {
  try {
    const nextStatus = user.status === 1 ? 0 : 1;
    const updated = await patch(`/portal/admin/users/${user.id}/status`, { status: nextStatus });
    Object.assign(user, updated);
    showFeedback('用户状态已更新', 'success');
  } catch (error) {
    showFeedback(error.message ?? '更新用户状态失败', 'error');
  }
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
.actions {
  display: flex;
  gap: 12px;
}

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
