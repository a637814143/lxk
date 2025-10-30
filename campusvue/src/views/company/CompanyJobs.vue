<template>
  <section class="card">
    <div class="card__title">
      <h2>职位管理</h2>
      <button class="outline" type="button" @click="loadJobs">刷新</button>
    </div>
    <form class="form-grid" @submit.prevent="createJob">
      <label class="full">职位名称<input v-model="jobForm.jobTitle" required /></label>
      <label>职位类型<input v-model="jobForm.jobType" /></label>
      <label>薪资范围<input v-model="jobForm.salaryRange" /></label>
      <label>工作地点<input v-model="jobForm.location" /></label>
      <label class="full">岗位要求<textarea v-model="jobForm.requirement"></textarea></label>
      <label class="full">职位描述<textarea v-model="jobForm.description"></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">{{ jobForm.jobId ? '更新职位' : '发布职位' }}</button>
        <button class="outline" type="button" @click="resetJobForm">重置</button>
      </div>
    </form>
    <table v-if="jobs.length" class="table">
      <thead>
        <tr><th>职位</th><th>类型</th><th>地点</th><th>状态</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="job in jobs" :key="job.id">
          <td>{{ job.jobTitle }}</td>
          <td>{{ job.jobType || '不限' }}</td>
          <td>{{ job.location || '不限' }}</td>
          <td><span class="status">{{ job.status }}</span></td>
          <td class="actions">
            <button class="outline" type="button" @click="prefillJob(job)">编辑</button>
            <select v-model="job.status" @change="changeJobStatus(job)">
              <option value="pending">待审核</option>
              <option value="approved">已发布</option>
              <option value="rejected">已拒绝</option>
              <option value="closed">已关闭</option>
            </select>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂未发布职位</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const company = inject('companyContext');
if (!company) {
  throw new Error('companyContext not provided');
}

const { jobForm, jobs, loadJobs, createJob, resetJobForm, prefillJob, changeJobStatus } = company;
</script>

<style scoped>
.status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 72px;
  padding: 4px 12px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 12px;
}
</style>
