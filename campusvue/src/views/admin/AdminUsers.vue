<template>
  <section class="card">
    <div class="card__title">
      <h2>用户管理</h2>
      <button class="outline" type="button" @click="loadUsers">刷新</button>
    </div>
    <table v-if="users.length" class="table">
      <thead>
        <tr><th>ID</th><th>用户名</th><th>角色</th><th>状态</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.username }}</td>
          <td>{{ user.role }}</td>
          <td>{{ user.status === 1 ? '启用' : '禁用' }}</td>
          <td class="actions">
            <button class="outline" type="button" @click="toggleUserStatus(user)">
              {{ user.status === 1 ? '禁用账号' : '恢复账号' }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无用户数据</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const admin = inject('adminContext');
if (!admin) {
  throw new Error('adminContext not provided');
}

const { users, loadUsers, toggleUserStatus } = admin;
</script>
