<template>
  <section class="card">
    <div class="card__title">
      <h2>公告管理</h2>
      <button class="outline" @click="loadAnnouncements">刷新</button>
    </div>

    <form class="form-grid" @submit.prevent="saveAnnouncement">
      <label>标题<input v-model="announcementForm.title" required maxlength="100" /></label>
      <label>目标
        <select v-model="announcementForm.target" required>
          <option value="all">全部用户</option>
          <option value="student">学生</option>
          <option value="company">企业</option>
        </select>
      </label>
      <label class="full">内容<textarea v-model="announcementForm.content" required maxlength="255"></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">{{ announcementForm.id ? '更新公告' : '发布公告' }}</button>
        <button class="outline" type="button" @click="resetForm">取消编辑</button>
      </div>
    </form>

    <ul class="list" v-if="announcements.length">
      <li v-for="item in announcements" :key="item.id" class="list__item">
        <div>
          <h3>{{ item.title }} <small class="muted">({{ item.target }})</small></h3>
          <p class="muted">发布时间：{{ formatDate(item.publishTime) }}</p>
          <p>{{ item.content }}</p>
        </div>
        <div class="list__actions">
          <button class="outline" @click="editAnnouncement(item)">编辑</button>
          <button class="outline" @click="deleteAnnouncement(item.id)">删除</button>
        </div>
      </li>
    </ul>
    <p v-else class="muted">暂无公告</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { del, get, post, put } from '../../api/http';

const announcements = ref([]);
const feedback = reactive({ message: '', type: 'info' });

const announcementForm = reactive({
  id: null,
  title: '',
  target: 'all',
  content: ''
});

onMounted(() => {
  loadAnnouncements();
});

async function loadAnnouncements() {
  try {
    announcements.value = await get('/portal/admin/announcements');
  } catch (error) {
    showFeedback(error.message ?? '加载公告失败', 'error');
  }
}

async function saveAnnouncement() {
  try {
    const payload = {
      title: announcementForm.title,
      target: announcementForm.target,
      content: announcementForm.content
    };
    if (announcementForm.id) {
      await put(`/portal/admin/announcements/${announcementForm.id}`, payload);
      showFeedback('公告已更新', 'success');
    } else {
      await post('/portal/admin/announcements', payload);
      showFeedback('公告已发布', 'success');
    }
    resetForm();
    await loadAnnouncements();
  } catch (error) {
    showFeedback(error.message ?? '保存公告失败', 'error');
  }
}

function editAnnouncement(item) {
  announcementForm.id = item.id;
  announcementForm.title = item.title;
  announcementForm.target = item.target || 'all';
  announcementForm.content = item.content;
}

async function deleteAnnouncement(id) {
  if (!confirm('确定删除该公告吗？')) {
    return;
  }
  try {
    await del(`/portal/admin/announcements/${id}`);
    showFeedback('公告已删除', 'success');
    await loadAnnouncements();
  } catch (error) {
    showFeedback(error.message ?? '删除公告失败', 'error');
  }
}

function resetForm() {
  announcementForm.id = null;
  announcementForm.title = '';
  announcementForm.target = 'all';
  announcementForm.content = '';
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
.list__actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
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
