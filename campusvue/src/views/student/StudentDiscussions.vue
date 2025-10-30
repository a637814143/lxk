<template>
  <section class="card">
    <div class="card__title">
      <h2>企业讨论区</h2>
      <button v-if="currentDiscussionCompany.id" class="outline" type="button" @click="resetDiscussion">
        返回说明
      </button>
    </div>
    <p class="muted" v-if="discussionLoading">正在加载企业讨论，请稍候…</p>
    <template v-else>
      <p class="muted" v-if="!currentDiscussionCompany.id">
        点击职位卡片中的“查看企业讨论”即可查看企业与管理员审核通过的交流内容。
      </p>
      <div v-else>
        <h3>{{ currentDiscussionCompany.name }} 的公开讨论</h3>
        <p v-if="discussionFeedback.message" :class="['feedback', discussionFeedback.type]">
          {{ discussionFeedback.message }}
        </p>
        <ul class="list" v-if="discussions.length">
          <li v-for="post in discussions" :key="post.id" class="list__item">
            <div>
              <h3>{{ post.title }}</h3>
              <p class="muted">发布时间：{{ formatDate(post.createdAt) }}</p>
              <p>{{ post.sanitizedContent }}</p>
              <p v-if="post.reviewComment" class="muted">审核备注：{{ post.reviewComment }}</p>
            </div>
          </li>
        </ul>
        <p v-else class="muted">暂无公开讨论，欢迎稍后再来查看。</p>
      </div>
    </template>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const student = inject('studentContext');
if (!student) {
  throw new Error('studentContext not provided');
}

const {
  currentDiscussionCompany,
  discussionLoading,
  discussionFeedback,
  discussions,
  resetDiscussion,
  formatDate
} = student;
</script>
