<template>
  <section class="card">
    <h2>平台公告</h2>
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

const student = inject('studentContext');
if (!student) {
  throw new Error('studentContext not provided');
}

const { announcements, formatDate } = student;
</script>
