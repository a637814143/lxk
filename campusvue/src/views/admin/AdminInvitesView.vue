<template>
  <section class="card">
    <div class="card__title">
      <h2>企业邀请码管理</h2>
      <button class="outline" @click="loadInvites">刷新</button>
    </div>

    <form class="form-grid" @submit.prevent="createInvite">
      <label class="full">备注<textarea v-model="inviteForm.note" maxlength="255" placeholder="记录此邀请码的使用场景（可选）"></textarea></label>
      <label class="full">企业名称提示<input v-model="inviteForm.companyNameHint" maxlength="100" placeholder="预期的企业名称（可选）" /></label>
      <div class="full actions">
        <button class="primary" type="submit">生成邀请码</button>
        <button class="outline" type="button" @click="resetForm">重置</button>
      </div>
    </form>

    <table v-if="invites.length" class="table">
      <thead><tr><th>邀请码</th><th>状态</th><th>企业提示</th><th>创建时间</th><th>使用时间</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="item in invites" :key="item.id">
          <td>{{ item.code }}</td>
          <td>{{ item.status }}</td>
          <td>{{ item.companyNameHint || '-' }}</td>
          <td>{{ formatDate(item.createdTime) }}</td>
          <td>{{ formatDate(item.usedTime) }}</td>
          <td class="actions">
            <template v-if="item.status === 'active'">
              <button class="outline" @click="updateStatus(item.id, 'revoked')">撤销</button>
            </template>
            <template v-else-if="item.status === 'revoked'">
              <button class="outline" @click="updateStatus(item.id, 'active')">重新启用</button>
            </template>
            <span v-else class="muted">已使用</span>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">尚未生成邀请码</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, patch, post } from '../../api/http';

const invites = ref([]);
const feedback = reactive({ message: '', type: 'info' });

const inviteForm = reactive({
  note: '',
  companyNameHint: ''
});

onMounted(() => {
  loadInvites();
});

async function loadInvites() {
  try {
    invites.value = await get('/portal/admin/invites');
  } catch (error) {
    showFeedback(error.message ?? '加载邀请码失败', 'error');
  }
}

async function createInvite() {
  try {
    await post('/portal/admin/invites', inviteForm);
    showFeedback('邀请码已生成', 'success');
    resetForm();
    await loadInvites();
  } catch (error) {
    showFeedback(error.message ?? '生成邀请码失败', 'error');
  }
}

async function updateStatus(id, status) {
  try {
    await patch(`/portal/admin/invites/${id}`, { status });
    showFeedback('邀请码状态已更新', 'success');
    await loadInvites();
  } catch (error) {
    showFeedback(error.message ?? '更新邀请码状态失败', 'error');
  }
}

function resetForm() {
  inviteForm.note = '';
  inviteForm.companyNameHint = '';
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
