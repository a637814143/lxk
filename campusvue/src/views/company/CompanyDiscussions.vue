<template>
  <section class="card">
    <div class="card__title">
      <h2>企业讨论区</h2>
      <button class="outline" type="button" @click="loadDiscussions">刷新</button>
    </div>
    <form class="form-grid" @submit.prevent="createDiscussion">
      <label class="full">讨论主题<input v-model="discussionForm.title" required /></label>
      <label class="full">讨论内容<textarea v-model="discussionForm.content" required></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">提交审核</button>
        <button class="outline" type="button" @click="resetDiscussionForm">清空</button>
      </div>
    </form>
    <ul class="list" v-if="discussions.length">
      <li v-for="item in discussions" :key="item.id" class="list__item">
        <div>
          <h3>{{ item.title }}</h3>
          <p class="muted">状态：{{ translateDiscussionStatus(item.status) }} · 提交时间：{{ formatDate(item.createdAt) }}</p>
          <p>{{ item.sanitizedContent || item.content }}</p>
          <p v-if="item.reviewComment" class="muted">审核备注：{{ item.reviewComment }}</p>
        </div>
      </li>
    </ul>
    <p v-else class="muted">暂无讨论内容，欢迎提交与学生交流的话题。</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const company = inject('companyContext');
if (!company) {
  throw new Error('companyContext not provided');
}

const {
  discussions,
  discussionForm,
  loadDiscussions,
  createDiscussion,
  resetDiscussionForm,
  translateDiscussionStatus,
  formatDate
} = company;
</script>
