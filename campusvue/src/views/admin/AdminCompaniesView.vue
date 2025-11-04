<template>
  <section class="card">
    <div class="card__title">
      <h2>企业审核</h2>
      <button class="outline" @click="loadPendingCompanies">刷新</button>
    </div>

    <table v-if="pendingCompanies.length" class="table">
      <thead><tr><th>企业名称</th><th>行业</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="company in pendingCompanies" :key="company.id">
          <td>{{ company.companyName }}</td>
          <td>{{ company.industry || '未填写' }}</td>
          <td>{{ company.auditStatus }}</td>
          <td class="actions">
            <button class="primary" @click="review(company.id, 'approved')">通过</button>
            <button class="outline" @click="openRejectDialog(company)">驳回</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无待审核企业</p>

    <section v-if="rejectDialog.visible" class="card">
      <h3>驳回原因</h3>
      <form class="form-grid" @submit.prevent="submitReject">
        <label class="full">备注<textarea v-model="rejectDialog.reason" maxlength="255" placeholder="请输入驳回原因"></textarea></label>
        <div class="full actions">
          <button class="primary" type="submit">确认驳回</button>
          <button class="outline" type="button" @click="closeRejectDialog">取消</button>
        </div>
      </form>
    </section>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, patch } from '../../api/http';

const pendingCompanies = ref([]);
const feedback = reactive({ message: '', type: 'info' });

const rejectDialog = reactive({
  visible: false,
  companyId: null,
  reason: ''
});

onMounted(() => {
  loadPendingCompanies();
});

async function loadPendingCompanies() {
  try {
    pendingCompanies.value = await get('/portal/admin/companies/pending');
  } catch (error) {
    showFeedback(error.message ?? '加载待审核企业失败', 'error');
  }
}

async function review(companyId, status, reason = '') {
  try {
    await patch(`/portal/admin/companies/${companyId}/review`, { status, reason });
    showFeedback('审核结果已提交', 'success');
    await loadPendingCompanies();
  } catch (error) {
    showFeedback(error.message ?? '提交审核失败', 'error');
  }
}

function openRejectDialog(company) {
  rejectDialog.visible = true;
  rejectDialog.companyId = company.id;
  rejectDialog.reason = '';
}

function closeRejectDialog() {
  rejectDialog.visible = false;
  rejectDialog.companyId = null;
  rejectDialog.reason = '';
}

async function submitReject() {
  if (!rejectDialog.companyId) {
    return;
  }
  await review(rejectDialog.companyId, 'rejected', rejectDialog.reason);
  closeRejectDialog();
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
