<template>
  <section class="card">
    <div class="card__title">
      <h2>职位浏览</h2>
      <button class="outline" type="button" @click="searchJobs">搜索</button>
    </div>
    <form class="filters" @submit.prevent="searchJobs">
      <input v-model="jobFilters.keyword" placeholder="关键字" />
      <input v-model="jobFilters.company" placeholder="企业名称" />
      <input v-model="jobFilters.jobType" placeholder="职位类别" />
      <input v-model="jobFilters.location" placeholder="工作地点" />
      <input v-model="jobFilters.salaryRange" placeholder="薪资范围" />
    </form>
    <div v-if="jobs.length" class="job-grid">
      <article v-for="job in jobs" :key="job.id" class="job-card">
        <header>
          <h3>{{ job.jobTitle }}</h3>
          <span class="tag">{{ job.jobType || '不限' }}</span>
        </header>
        <p class="muted">工作地点：{{ job.location || '不限' }}</p>
        <p class="muted">薪资范围：{{ job.salaryRange || '面议' }}</p>
        <p class="job-card__desc">{{ job.description || '暂无职位描述' }}</p>
        <footer>
          <button class="primary" type="button" :disabled="!selectedResumeId" @click="applyJob(job.id)">
            使用选中简历投递
          </button>
          <button class="outline" type="button" @click="loadCompanyDiscussions(job.companyId, job.companyName)">
            查看企业讨论
          </button>
        </footer>
      </article>
    </div>
    <p v-else class="muted">请调整筛选条件或稍后再试，目前没有符合条件的职位。</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const student = inject('studentContext');
if (!student) {
  throw new Error('studentContext not provided');
}

const { jobFilters, jobs, selectedResumeId, searchJobs, applyJob, loadCompanyDiscussions } = student;
</script>

<style scoped>
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.filters input {
  flex: 1 1 180px;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 8px 10px;
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
</style>
