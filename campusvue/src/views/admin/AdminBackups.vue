<template>
  <section class="card">
    <div class="card__title">
      <h2>数据备份</h2>
      <button class="outline" type="button" @click="loadBackups">刷新</button>
    </div>
    <form class="form-grid" @submit.prevent="triggerBackup">
      <label>备份类型<input v-model="backupForm.backupType" placeholder="例如 daily/system" /></label>
      <label class="full">备注<textarea v-model="backupForm.message" placeholder="可选备注"></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">立即备份</button>
        <button class="outline" type="button" @click="backupForm.message = ''">清空备注</button>
      </div>
    </form>
    <table v-if="backups.length" class="table">
      <thead><tr><th>ID</th><th>类型</th><th>状态</th><th>创建时间</th><th>文件</th></tr></thead>
      <tbody>
        <tr v-for="item in backups" :key="item.id">
          <td>{{ item.id }}</td>
          <td>{{ item.backupType || 'system' }}</td>
          <td>{{ item.status }}</td>
          <td>{{ formatDate(item.createdAt) }}</td>
          <td>
            <a v-if="item.downloadUrl" :href="item.downloadUrl" target="_blank" rel="noopener">下载</a>
            <span v-else class="muted">生成中</span>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无备份记录</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const admin = inject('adminContext');
if (!admin) {
  throw new Error('adminContext not provided');
}

const { backupForm, backups, loadBackups, triggerBackup, formatDate } = admin;
</script>
