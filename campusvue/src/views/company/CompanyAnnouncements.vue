<template>
  <section class="card">
    <div class="card__title">
      <h2>平台公告</h2>
      <button class="outline" type="button" @click="loadAnnouncements">刷新</button>
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
import { inject } from 'vue';

const company = inject('companyContext');
if (!company) {
  throw new Error('companyContext not provided');
}

const { announcements, loadAnnouncements, formatDate } = company;
</script>
