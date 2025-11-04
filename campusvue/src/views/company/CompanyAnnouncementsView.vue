<template>
  <section class="card">
    <div class="card__title">
      <h2>平台公告</h2>
      <button class="outline" @click="loadAnnouncements">刷新</button>
    </div>

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
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { get } from '../../api/http';

const announcements = ref([]);

onMounted(() => {
  loadAnnouncements();
});

async function loadAnnouncements() {
  try {
    announcements.value = await get('/portal/company/announcements');
  } catch (error) {
    console.warn('加载公告失败', error);
  }
}

function formatDate(value) {
  if (!value) {
    return '-';
  }
  return new Date(value).toLocaleString();
}
</script>
