<template>
  <section class="card">
    <div class="card__title">
      <h2>企业审核</h2>
      <button class="outline" type="button" @click="loadPendingCompanies">刷新</button>
    </div>
    <table v-if="pendingCompanies.length" class="table">
      <thead>
        <tr><th>企业名称</th><th>行业</th><th>状态</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="company in pendingCompanies" :key="company.id">
          <td>{{ company.companyName }}</td>
          <td>{{ company.industry || '未填写' }}</td>
          <td>{{ company.auditStatus }}</td>
          <td class="actions">
            <button class="primary" type="button" @click="reviewCompany(company.id, 'approved')">通过</button>
            <button class="danger" type="button" @click="reviewCompany(company.id, 'rejected')">驳回</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无待审核企业</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const admin = inject('adminContext');
if (!admin) {
  throw new Error('adminContext not provided');
}

const { pendingCompanies, loadPendingCompanies, reviewCompany } = admin;
</script>
