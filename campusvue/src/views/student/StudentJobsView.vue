<template>
  <section class="section">
    <header class="section__header">
      <div>
        <h2>职位浏览</h2>
        <p class="muted">按条件筛选职位并使用选定简历进行投递</p>
      </div>
      <button class="outline" type="button" @click="searchJobs" :disabled="loading">
        {{ loading ? '搜索中…' : '搜索' }}
      </button>
    </header>

    <div class="filters">
      <input v-model="jobFilters.keyword" placeholder="关键字" />
      <input v-model="jobFilters.company" placeholder="企业名称" />
      <input v-model="jobFilters.jobType" placeholder="职位类别" />
      <input v-model="jobFilters.location" placeholder="工作地点" />
      <input v-model="jobFilters.salaryRange" placeholder="薪资范围" />
      <select v-model="resumeSelection" @change="handleResumeChange">
        <option :value="null">请选择投递简历</option>
        <option v-for="resume in resumes" :key="resume.id" :value="resume.id">
          {{ resume.title }} (#{{ resume.id }})
        </option>
      </select>
    </div>

    <div v-if="jobs.length" class="job-grid">
      <article v-for="job in jobs" :key="job.id" class="job-card">
        <header>
          <h3>{{ job.jobTitle }}</h3>
          <span class="tag">{{ job.jobType || '不限' }}</span>
        </header>
        <p class="muted">企业：{{ job.companyName || `企业 #${job.companyId}` }}</p>
        <p class="muted">工作地点：{{ job.location || '不限' }}</p>
        <p class="muted">薪资范围：{{ job.salaryRange || '面议' }}</p>
        <p class="job-card__desc">{{ job.description || '暂无职位描述' }}</p>
        <footer>
          <button class="primary" :disabled="!selectedResumeId" @click="applyJob(job.id)">
            用选中简历投递
          </button>
          <button class="outline" type="button" @click="openDiscussions(job)">
            查看企业讨论
          </button>
        </footer>
      </article>
    </div>
    <p v-else class="muted">请调整筛选条件或稍后再试，目前没有符合条件的职位。</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { computed, inject, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { get, post } from '../../api/http';

const router = useRouter();

const selectedResumeId = inject('selectedResumeId', ref(null));
const setSelectedResumeId = inject('setSelectedResumeId', (id) => {
  selectedResumeId.value = id;
});

const resumes = ref([]);
const jobs = ref([]);
const loading = ref(false);
const feedback = reactive({ message: '', type: 'info' });

const jobFilters = reactive({
  keyword: '',
  company: '',
  jobType: '',
  location: '',
  salaryRange: ''
});

const resumeSelection = computed({
  get: () => selectedResumeId.value ?? null,
  set: value => setSelectedResumeId(value ? Number(value) : null)
});

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
  }
}

function buildQuery(params) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value) {
      search.append(key, value);
    }
  });
  const query = search.toString();
  return query ? `?${query}` : '';
}

async function loadResumes() {
  try {
    const data = await get('/portal/student/resumes');
    resumes.value = data;
    if (!selectedResumeId.value && data.length) {
      setSelectedResumeId(data[0].id);
    }
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

async function searchJobs() {
  try {
    loading.value = true;
    const query = buildQuery(jobFilters);
    jobs.value = await get(`/portal/student/jobs${query}`);
  } catch (error) {
    showFeedback(error.message, 'error');
  } finally {
    loading.value = false;
  }
}

async function applyJob(jobId) {
  if (!selectedResumeId.value) {
    showFeedback('请先选择要投递的简历', 'error');
    return;
  }
  try {
    await post('/portal/student/applications', {
      jobId,
      resumeId: selectedResumeId.value
    });
    showFeedback('投递成功，稍后可在“我的投递”中查看进度', 'success');
  } catch (error) {
    showFeedback(error.message, 'error');
  }
}

function openDiscussions(job) {
  router.push({
    name: 'student-discussions',
    query: {
      companyId: job.companyId,
      companyName: job.companyName ?? ''
    }
  });
}

function handleResumeChange(event) {
  const value = event.target.value;
  resumeSelection.value = value ? Number(value) : null;
}

watch(() => selectedResumeId.value, () => {
  // Keep select element in sync when selection is changed elsewhere
  if (!selectedResumeId.value) {
    resumeSelection.value = null;
  }
});

onMounted(async () => {
  await loadResumes();
  await searchJobs();
});
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
  align-items: center;
}

.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.filters input,
.filters select {
  flex: 1 1 180px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 8px 10px;
  font-size: 14px;
}

.job-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.job-card {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #fff;
}

.job-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.job-card__desc {
  line-height: 1.5;
  color: #475569;
}

.tag {
  background: #e0f2fe;
  color: #0369a1;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 12px;
}

.primary {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border: none;
  color: #fff;
  padding: 10px 18px;
  border-radius: 10px;
  cursor: pointer;
}

.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.feedback.success {
  background: #dcfce7;
  color: #15803d;
}

.feedback.error {
  background: #fee2e2;
  color: #b91c1c;
}
</style>
