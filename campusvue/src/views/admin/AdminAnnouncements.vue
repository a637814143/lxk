<template>
  <section class="card">
    <div class="card__title">
      <h2>公告管理</h2>
      <button class="outline" type="button" @click="loadAnnouncements">刷新</button>
    </div>
    <form class="form-grid" @submit.prevent="saveAnnouncement">
      <label>标题<input v-model="announcementForm.title" required /></label>
      <label>目标
        <select v-model="announcementForm.target" required>
          <option value="all">全部用户</option>
          <option value="student">学生</option>
          <option value="company">企业</option>
        </select>
      </label>
      <label class="full">内容<textarea v-model="announcementForm.content" required></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">{{ announcementForm.id ? '更新公告' : '发布公告' }}</button>
        <button class="outline" type="button" @click="resetAnnouncementForm">取消编辑</button>
      </div>
    </form>
    <ul class="list" v-if="announcements.length">
      <li v-for="item in announcements" :key="item.id" class="list__item">
        <div>
          <h3>{{ item.title }} <small class="muted">({{ item.target }})</small></h3>
          <p class="muted">发布时间：{{ formatDate(item.publishTime) }}</p>
          <p>{{ item.content }}</p>
        </div>
        <div class="list__actions">
          <button class="outline" type="button" @click="editAnnouncement(item)">编辑</button>
          <button class="danger" type="button" @click="deleteAnnouncement(item.id)">删除</button>
        </div>
      </li>
    </ul>
    <p v-else class="muted">暂无公告</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const admin = inject('adminContext');
if (!admin) {
  throw new Error('adminContext not provided');
}

const {
  announcementForm,
  announcements,
  loadAnnouncements,
  saveAnnouncement,
  resetAnnouncementForm,
  editAnnouncement,
  deleteAnnouncement,
  formatDate
} = admin;
</script>
