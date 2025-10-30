<template>
  <section class="card">
    <div class="card__title">
      <h2>职位审核</h2>
      <button class="outline" type="button" @click="loadPendingJobs">刷新</button>
    </div>
    <table v-if="pendingJobs.length" class="table">
      <thead>
        <tr><th>职位名称</th><th>企业ID</th><th>状态</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="job in pendingJobs" :key="job.id">
          <td>{{ job.jobTitle }}</td>
          <td>{{ job.companyId }}</td>
          <td>{{ job.status }}</td>
          <td class="actions">
            <button class="primary" type="button" @click="reviewJob(job.id, 'approved')">通过</button>
            <button class="danger" type="button" @click="reviewJob(job.id, 'rejected')">驳回</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无待审核职位</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const admin = inject('adminContext');
if (!admin) {
  throw new Error('adminContext not provided');
}

const { pendingJobs, loadPendingJobs, reviewJob } = admin;
</script>
