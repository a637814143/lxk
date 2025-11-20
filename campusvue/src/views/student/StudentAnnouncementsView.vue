<template>
  <section class="section">
    <header class="section__header">
      <div>
        <h2>平台公告</h2>
        <p class="muted">了解校园招聘平台的最新通知</p>
      </div>
      <button class="outline" type="button" @click="loadAnnouncements" :disabled="loading">
        {{ loading ? '加载中' : '刷新' }}
      </button>
    </header>

    <ul class="list" v-if="announcements.length">
      <li v-for="item in announcements" :key="item.id" class="list__item">
        <div>
          <h3>{{ item.title }}</h3>
          <p class="muted">发布时间：{{ formatDate(item.publishTime) }}</p>
          <p>{{ item.content }}</p>
        </div>
      </li>
    </ul>
    <p v-else class="muted">暂无公告</p>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get } from '../../api/http';

const announcements = ref([]);
const loading = ref(false);
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

async function loadAnnouncements() {
  try {
    loading.value = true;
    announcements.value = await get('/portal/student/announcements');
  } catch (error) {
    showFeedback(error.message, 'error');
  } finally {
    loading.value = false;
  }
}

function formatDate(value) {
  if (!value) return '';
  return new Date(value).toLocaleString();
}

onMounted(loadAnnouncements);
</script>
