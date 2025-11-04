<template>
  <section class="card">
    <div class="card__title">
      <h2>简历投递</h2>
      <button class="outline" @click="reload">刷新</button>
    </div>

    <table v-if="applications.length" class="table">
      <thead>
        <tr><th>职位</th><th>学生ID</th><th>状态</th><th>备注</th><th>更新时间</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="app in applications" :key="app.id">
          <td>{{ resolveJobTitle(app.jobId) }}</td>
          <td>{{ app.studentId }}</td>
          <td>{{ app.status }}</td>
          <td>
            <input
              v-model="app.decisionNote"
              maxlength="500"
              placeholder="备注（可选）"
              @change="updateStatus(app)"
            />
          </td>
          <td>{{ formatDate(app.updateTime) }}</td>
          <td class="actions">
            <select v-model="app.status" @change="updateStatus(app)">
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

    <section v-if="messageDialog.visible" class="card">
      <h3>向学生发送消息</h3>
      <form class="form-grid" @submit.prevent="sendMessage">
        <label>标题<input v-model="messageDialog.form.title" required maxlength="100" /></label>
        <label class="full">内容<textarea v-model="messageDialog.form.content" required maxlength="255"></textarea></label>
        <div class="full actions">
          <button class="primary" type="submit">发送</button>
          <button class="outline" type="button" @click="closeMessageDialog">取消</button>
        </div>
      </form>
    </section>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { get, patch } from '../../api/http';

const applications = ref([]);
const jobs = ref([]);
const feedback = reactive({ message: '', type: 'info' });

const messageDialog = reactive({
  visible: false,
  applicationId: null,
  status: '',
  form: {
    title: '',
    content: ''
  }
});

const jobMap = computed(() => {
  const map = new Map();
  jobs.value.forEach(job => {
    map.set(job.id, job.jobTitle);
  });
  return map;
});

onMounted(() => {
  reload();
});

async function reload() {
  await Promise.all([loadJobs(), loadApplications()]);
}

async function loadJobs() {
  try {
    jobs.value = await get('/portal/company/jobs');
  } catch (error) {
    console.warn('加载职位失败', error);
  }
}

async function loadApplications() {
  try {
    const items = await get('/portal/company/applications');
    applications.value = items.map(normalizeApplication);
  } catch (error) {
    showFeedback(error.message ?? '加载投递记录失败', 'error');
  }
}

function normalizeApplication(item) {
  return {
    id: item.id,
    jobId: item.jobId,
    studentId: item.studentId,
    status: item.status,
    decisionNote: item.decisionNote || '',
    updateTime: item.updateTime
  };
}

async function updateStatus(app) {
  try {
    const updated = await patch(`/portal/company/applications/${app.id}`, {
      status: app.status,
      decisionNote: app.decisionNote
    });
    applyApplicationUpdate(app, updated);
    showFeedback('投递状态已更新', 'success');
  } catch (error) {
    showFeedback(error.message ?? '更新状态失败', 'error');
  }
}

function applyApplicationUpdate(target, updated) {
  Object.assign(target, normalizeApplication(updated));
}

function openMessageDialog(app) {
  messageDialog.visible = true;
  messageDialog.applicationId = app.id;
  messageDialog.status = app.status;
  messageDialog.form.title = `关于职位 #${app.jobId}`;
  messageDialog.form.content = '';
}

function closeMessageDialog() {
  messageDialog.visible = false;
  messageDialog.applicationId = null;
  messageDialog.status = '';
  messageDialog.form.title = '';
  messageDialog.form.content = '';
}

async function sendMessage() {
  if (!messageDialog.form.title || !messageDialog.form.content) {
    showFeedback('请填写消息标题与内容', 'error');
    return;
  }
  try {
    const payload = {
      status: messageDialog.status,
      decisionNote: findApplication(messageDialog.applicationId)?.decisionNote,
      messageTitle: messageDialog.form.title,
      messageContent: messageDialog.form.content
    };
    const updated = await patch(`/portal/company/applications/${messageDialog.applicationId}`, payload);
    const target = findApplication(messageDialog.applicationId);
    if (target) {
      applyApplicationUpdate(target, updated);
    }
    showFeedback('消息发送成功', 'success');
    closeMessageDialog();
  } catch (error) {
    showFeedback(error.message ?? '消息发送失败', 'error');
  }
}

function findApplication(id) {
  return applications.value.find(item => item.id === id) || null;
}

function resolveJobTitle(jobId) {
  return jobMap.value.get(jobId) || `职位 #${jobId}`;
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
.table input {
  min-width: 160px;
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
