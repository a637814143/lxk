<template>
  <section class="card">
    <h2>消息中心</h2>
    <ul class="list" v-if="messages.length">
      <li v-for="message in messages" :key="message.id" class="list__item">
        <div>
          <h3>{{ message.title }}</h3>
          <p class="muted">{{ formatDate(message.sendTime) }}</p>
          <p>{{ message.content }}</p>
        </div>
        <button v-if="!message.isRead" class="outline" type="button" @click="markMessageRead(message.id)">标记已读</button>
      </li>
    </ul>
    <p v-else class="muted">暂无消息</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const student = inject('studentContext');
if (!student) {
  throw new Error('studentContext not provided');
}

const { messages, markMessageRead, formatDate } = student;
</script>
