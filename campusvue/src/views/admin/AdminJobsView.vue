<template>
  <section class="card">
    <div class="card__title">
      <h2>职位审核</h2>
      <button class="outline" @click="loadPendingJobs">刷新</button>
    </div>

    <table v-if="pendingJobs.length" class="table">
      <thead><tr><th>职位名称</th><th>企业ID</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="job in pendingJobs" :key="job.id">
          <td>{{ job.jobTitle }}</td>
          <td>{{ job.companyId }}</td>
          <td>{{ job.status }}</td>
          <td class="actions">
            <button class="primary" @click="review(job.id, 'approved')">通过</button>
            <button class="outline" @click="openReviewDialog(job)">驳回</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无待审核职位</p>

    <section v-if="reviewDialog.visible" class="card">
      <h3>职位审核</h3>
      <form class="form-grid" @submit.prevent="submitReview">
        <label class="full">审核备注<textarea v-model="reviewDialog.reason" maxlength="255" placeholder="请输入审核备注（可选）"></textarea></label>
        <div class="full actions">
          <button class="primary" type="submit">确认驳回</button>
          <button class="outline" type="button" @click="closeReviewDialog">取消</button>
        </div>
      </form>
    </section>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, patch } from '../../api/http';

const pendingJobs = ref([]);
const feedback = reactive({ message: '', type: 'info' });

const reviewDialog = reactive({
  visible: false,
  jobId: null,
  reason: ''
});

onMounted(() => {
  loadPendingJobs();
});

async function loadPendingJobs() {
  try {
    pendingJobs.value = await get('/portal/admin/jobs/pending');
  } catch (error) {
    showFeedback(error.message ?? '加载待审核职位失败', 'error');
  }
}

async function review(jobId, status, reason = '') {
  try {
    await patch(`/portal/admin/jobs/${jobId}/review`, { status, reason });
    showFeedback('职位审核结果已提交', 'success');
    await loadPendingJobs();
  } catch (error) {
    showFeedback(error.message ?? '提交审核失败', 'error');
  }
}

function openReviewDialog(job) {
  reviewDialog.visible = true;
  reviewDialog.jobId = job.id;
  reviewDialog.reason = '';
}

function closeReviewDialog() {
  reviewDialog.visible = false;
  reviewDialog.jobId = null;
  reviewDialog.reason = '';
}

async function submitReview() {
  if (!reviewDialog.jobId) {
    return;
  }
  await review(reviewDialog.jobId, 'rejected', reviewDialog.reason);
  closeReviewDialog();
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
