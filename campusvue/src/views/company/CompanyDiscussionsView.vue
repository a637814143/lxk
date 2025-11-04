<template>
  <section class="card">
    <div class="card__title">
      <h2>企业讨论区</h2>
      <button class="outline" @click="loadDiscussions">刷新</button>
    </div>

    <form class="form-grid" @submit.prevent="createDiscussion">
      <label class="full">讨论主题<input v-model="discussionForm.title" required maxlength="100" /></label>
      <label class="full">讨论内容<textarea v-model="discussionForm.content" required maxlength="1000"></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">提交审核</button>
        <button class="outline" type="button" @click="resetDiscussionForm">清空</button>
      </div>
    </form>

    <ul class="list" v-if="discussions.length">
      <li v-for="item in discussions" :key="item.id" class="list__item">
        <div>
          <h3>{{ item.title }}</h3>
          <p class="muted">状态：{{ translateStatus(item.status) }} · 提交时间：{{ formatDate(item.createdAt) }}</p>
          <p>{{ item.sanitizedContent || item.content }}</p>
          <p v-if="item.reviewComment" class="muted">审核备注：{{ item.reviewComment }}</p>
        </div>
      </li>
    </ul>
    <p v-else class="muted">暂无讨论内容，欢迎提交与学生交流的话题。</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, post } from '../../api/http';

const discussions = ref([]);
const discussionForm = reactive({
  title: '',
  content: ''
});
const feedback = reactive({ message: '', type: 'info' });

onMounted(() => {
  loadDiscussions();
});

async function loadDiscussions() {
  try {
    discussions.value = await get('/portal/company/discussions');
  } catch (error) {
    showFeedback(error.message ?? '加载讨论内容失败', 'error');
  }
}

async function createDiscussion() {
  if (!discussionForm.title || !discussionForm.content) {
    showFeedback('请填写讨论主题和内容', 'error');
    return;
  }
  try {
    const created = await post('/portal/company/discussions', discussionForm);
    discussions.value.unshift(created);
    showFeedback('讨论已提交审核', 'success');
    resetDiscussionForm();
  } catch (error) {
    showFeedback(error.message ?? '提交讨论失败', 'error');
  }
}

function resetDiscussionForm() {
  discussionForm.title = '';
  discussionForm.content = '';
}

function translateStatus(status) {
  switch ((status || '').toLowerCase()) {
    case 'approved':
      return '已发布';
    case 'rejected':
      return '已驳回';
    case 'pending':
      return '待审核';
    default:
      return status || '-';
  }
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
