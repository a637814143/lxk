<template>
  <section class="section">
    <header class="section__header">
      <div>
        <h2>消息中心</h2>
        <p class="muted">查看并及时处理来自企业或系统的通知</p>
      </div>
      <button class="outline" type="button" @click="loadMessages" :disabled="loading">
        {{ loading ? '刷新中…' : '刷新' }}
      </button>
    </header>

    <ul class="list" v-if="messages.length">
      <li v-for="message in messages" :key="message.id" class="list__item">
        <div>
          <h3>{{ message.title }}</h3>
          <p class="muted">{{ formatDate(message.sendTime) }}</p>
          <p>{{ message.content }}</p>
        </div>
        <button
          v-if="!message.isRead"
          class="outline"
          :disabled="marking"
          @click="markMessageRead(message.id)"
        >
          标记已读
        </button>
      </li>
    </ul>
    <p v-else class="muted">暂无消息</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, post } from '../../api/http';

const messages = ref([]);
const loading = ref(false);
const marking = ref(false);
const feedback = reactive({ message: '', type: 'info' });

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
  }
}

async function loadMessages() {
  try {
    loading.value = true;
    messages.value = await get('/portal/student/messages');
  } catch (error) {
    showFeedback(error.message, 'error');
  } finally {
    loading.value = false;
  }
}

async function markMessageRead(messageId) {
  try {
    marking.value = true;
    const updated = await post(`/portal/student/messages/${messageId}/read`);
    messages.value = messages.value.map(msg => (msg.id === updated.id ? updated : msg));
  } catch (error) {
    showFeedback(error.message, 'error');
  } finally {
    marking.value = false;
  }
}

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

onMounted(loadMessages);
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

.list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.list__item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
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
