<template>
  <section class="section">
    <header class="section__header">
      <div>
        <h2>消息中心</h2>
        <p class="muted">查看并及时处理来自企业或系统的通知</p>
      </div>
      <button class="outline" type="button" @click="loadMessages" :disabled="loading">
        {{ loading ? '刷新中' : '刷新' }}
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
  </section>
</template>

<script setup>
import { inject, onMounted, ref } from 'vue';
import { get, post } from '../../api/http';
import { useToast } from '../../ui/toast';

const messages = ref([]);
const loading = ref(false);
const marking = ref(false);
const toast = useToast();
const refreshUnreadCount = inject('refreshUnreadCount', () => {});

async function loadMessages() {
  try {
    loading.value = true;
    messages.value = await get('/portal/student/messages');
  } catch (error) {
    toast.error(error.message);
  } finally {
    loading.value = false;
  }
}

async function markMessageRead(messageId) {
  try {
    marking.value = true;
    const updated = await post(`/portal/student/messages/${messageId}/read`);
    messages.value = messages.value.map(msg => (msg.id === updated.id ? updated : msg));
    try { refreshUnreadCount(); } catch {}
  } catch (error) {
    toast.error(error.message);
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
