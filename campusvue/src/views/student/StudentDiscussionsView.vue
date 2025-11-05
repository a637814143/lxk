<template>
  <section class="section">
    <header class="section__header">
      <div>
        <h2>企业讨论区</h2>
        <p class="muted">
          查看企业的公开讨论记录，可通过职位列表的“查看企业讨论”快速跳转
        </p>
      </div>
      <div class="actions">
        <input v-model="manualCompanyId" placeholder="输入企业ID" />
        <button class="outline" type="button" @click="loadByManualId" :disabled="loading">
          加载讨论
        </button>
        <button class="outline" type="button" @click="goBackToJobs">返回职位</button>
      </div>
    </header>

    <div v-if="currentCompany.id" class="current-company">
      <h3>{{ currentCompany.name }}</h3>
      <p class="muted">企业 ID：{{ currentCompany.id }}</p>
    </div>

    <p class="muted" v-if="loading">正在加载企业讨论，请稍候…</p>
    <template v-else>
      <p class="muted" v-if="!currentCompany.id">
        尚未选择企业。可在职位列表中点击“查看企业讨论”或在上方输入企业 ID。
      </p>
      <div v-else>
        <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
        <ul class="list" v-if="discussions.length">
          <li v-for="post in discussions" :key="post.id" class="list__item">
            <div>
              <h3>{{ post.title }}</h3>
              <p class="muted">发布时间：{{ formatDate(post.createdAt) }}</p>
              <p>{{ post.sanitizedContent }}</p>
              <p v-if="post.reviewComment" class="muted">审核备注：{{ post.reviewComment }}</p>
            </div>
          </li>
        </ul>
        <p v-else class="muted">暂无公开讨论，欢迎稍后再来查看。</p>
      </div>
    </template>
  </section>
</template>

<script setup>
import { reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { get } from '../../api/http';

const route = useRoute();
const router = useRouter();

const currentCompany = reactive({ id: null, name: '' });
const discussions = ref([]);
const loading = ref(false);
const feedback = reactive({ message: '', type: 'info' });
const manualCompanyId = ref('');

async function loadDiscussions(companyId, companyName = '') {
  if (!companyId) {
    currentCompany.id = null;
    currentCompany.name = '';
    discussions.value = [];
    feedback.message = '';
    feedback.type = 'info';
    manualCompanyId.value = '';
    return;
  }

  currentCompany.id = companyId;
  currentCompany.name = companyName || `企业 #${companyId}`;
  manualCompanyId.value = String(companyId);
  loading.value = true;
  feedback.message = '';
  try {
    const data = await get(`/public/discussions/company/${companyId}`);
    discussions.value = data;
    if (!data.length) {
      feedback.message = '暂无公开讨论记录';
      feedback.type = 'info';
    }
  } catch (error) {
    discussions.value = [];
    feedback.message = error.message ?? '加载讨论失败';
    feedback.type = 'error';
  } finally {
    loading.value = false;
  }
}

function loadByManualId() {
  const parsed = Number(manualCompanyId.value);
  if (!parsed) {
    feedback.message = '请输入有效的企业 ID';
    feedback.type = 'error';
    return;
  }
  router.replace({
    name: 'student-discussions',
    query: { companyId: parsed }
  });
  loadDiscussions(parsed, '');
}

function goBackToJobs() {
  router.push({ name: 'student-jobs' });
}

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

watch(
  () => route.query,
  query => {
    const companyId = query.companyId ? Number(query.companyId) : null;
    const companyName = query.companyName ?? '';
    loadDiscussions(companyId, companyName);
  },
  { immediate: true, deep: true }
);
</script>

<style scoped>
.section {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.actions input {
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 8px 10px;
  min-width: 160px;
}

.current-company {
  background: #eff6ff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list__item {
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.outline {
  background: transparent;
  border: 1px solid #2563eb;
  color: #2563eb;
  padding: 8px 16px;
  border-radius: 10px;
  cursor: pointer;
}

.muted {
  color: #6b7280;
  margin: 0;
}

.feedback {
  text-align: center;
  padding: 12px;
  border-radius: 12px;
}

.feedback.error {
  background: #fee2e2;
  color: #b91c1c;
}

.feedback.info {
  background: #e0f2fe;
  color: #0369a1;
}
</style>
