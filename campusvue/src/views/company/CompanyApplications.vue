<template>
  <section class="card">
    <div class="card__title">
      <h2>简历投递</h2>
      <button class="outline" type="button" @click="loadApplications">刷新</button>
    </div>
    <table v-if="applications.length" class="table">
      <thead>
        <tr><th>职位</th><th>学生ID</th><th>状态</th><th>更新时间</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="app in applications" :key="app.id">
          <td>{{ resolveJobTitle(app.jobId) }}</td>
          <td>{{ app.studentId }}</td>
          <td>{{ app.status }}</td>
          <td>{{ formatDate(app.updateTime) }}</td>
          <td class="actions">
            <select v-model="app.status" @change="updateApplicationStatus(app)">
              <option value="待查看">待查看</option>
              <option value="已查看">已查看</option>
              <option value="面试中">面试中</option>
              <option value="录用">录用</option>
              <option value="拒绝">拒绝</option>
            </select>
            <button class="outline" type="button" @click="openMessageDialog(app)">发送消息</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无投递</p>
  </section>
  <section v-if="messageDialog.visible" class="card">
    <h2>向学生发送消息</h2>
    <form class="form-grid" @submit.prevent="sendMessage">
      <label>标题<input v-model="messageDialog.form.title" required /></label>
      <label class="full">内容<textarea v-model="messageDialog.form.content" required></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">发送</button>
        <button class="outline" type="button" @click="closeMessageDialog">取消</button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const company = inject('companyContext');
if (!company) {
  throw new Error('companyContext not provided');
}

const {
  applications,
  messageDialog,
  loadApplications,
  updateApplicationStatus,
  openMessageDialog,
  closeMessageDialog,
  sendMessage,
  resolveJobTitle,
  formatDate
} = company;
</script>
