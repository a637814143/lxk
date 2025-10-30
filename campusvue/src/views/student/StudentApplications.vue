<template>
  <section class="card">
    <h2>我的投递</h2>
    <table v-if="applications.length" class="table">
      <thead>
        <tr><th>职位</th><th>企业</th><th>状态</th><th>投递时间</th></tr>
      </thead>
      <tbody>
        <tr v-for="app in applications" :key="app.id">
          <td>{{ resolveJobTitle(app.jobId) }}</td>
          <td>{{ resolveCompanyName(app.companyId) }}</td>
          <td>{{ app.status }}</td>
          <td>{{ formatDate(app.applyTime) }}</td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无投递记录</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const student = inject('studentContext');
if (!student) {
  throw new Error('studentContext not provided');
}

const { applications, resolveJobTitle, resolveCompanyName, formatDate } = student;
</script>
