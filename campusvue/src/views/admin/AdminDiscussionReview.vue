<template>
  <section class="card">
    <div class="card__title">
      <h2>讨论审核</h2>
      <button class="outline" type="button" @click="loadPendingDiscussions">刷新</button>
    </div>
    <ul class="list" v-if="pendingDiscussions.length">
      <li v-for="item in pendingDiscussions" :key="item.id" class="list__item">
        <div>
          <h3>{{ item.title }} <small class="muted">企业 #{{ item.companyId }}</small></h3>
          <p class="muted">提交时间：{{ formatDate(item.createdAt) }}</p>
          <p>{{ item.sanitizedContent || item.content }}</p>
          <p v-if="item.reviewComment" class="muted">备注：{{ item.reviewComment }}</p>
        </div>
        <div class="list__actions">
          <button class="primary" type="button" @click="handleDiscussionReview(item, 'approved')">通过</button>
          <button class="danger" type="button" @click="handleDiscussionReview(item, 'rejected')">驳回</button>
        </div>
      </li>
    </ul>
    <p v-else class="muted">暂无待审核的讨论内容</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const admin = inject('adminContext');
if (!admin) {
  throw new Error('adminContext not provided');
}

const { pendingDiscussions, loadPendingDiscussions, handleDiscussionReview, formatDate } = admin;
</script>
