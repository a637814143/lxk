<template>
  <section class="card">
    <div class="card__title">
      <h2>财务记录管理</h2>
      <button class="outline" type="button" @click="loadTransactions">刷新</button>
    </div>
    <div class="billing-summary">
      <span>季度单价：￥{{ formatCurrency(quarterFee.value) }}</span>
      <span>当前选择：{{ transactionForm.durationQuarters }} 个季度</span>
      <span>预计扣款：￥{{ formatCurrency(calculatedAmount.value) }}</span>
    </div>
    <form class="form-grid" @submit.prevent="createTransaction">
      <label>
        企业ID
        <input v-model="transactionForm.companyId" type="number" min="1" required />
      </label>
      <label>
        使用时长
        <select v-model.number="transactionForm.durationQuarters">
          <option v-for="option in durationOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </label>
      <label class="full">
        费用用途
        <input v-model="transactionForm.type" placeholder="季度服务费" />
      </label>
      <label class="full">
        业务编号
        <input v-model="transactionForm.reference" placeholder="可选的内部参考编号" />
      </label>
      <label class="full">
        备注
        <textarea v-model="transactionForm.notes" placeholder="补充说明（可选）"></textarea>
      </label>
      <div class="full actions">
        <button class="primary" type="submit">创建记录</button>
        <button class="outline" type="button" @click="resetTransactionForm">重置</button>
      </div>
    </form>
    <table v-if="transactions.length" class="table">
      <thead>
        <tr><th>ID</th><th>企业ID</th><th>用途</th><th>金额</th><th>服务时长</th><th>状态</th><th>更新时间</th><th>操作</th></tr>
      </thead>
      <tbody>
        <tr v-for="item in transactions" :key="item.id">
          <td>{{ item.id }}</td>
          <td>{{ item.companyId }}</td>
          <td>{{ item.type }}</td>
          <td>{{ Number(item.amount ?? 0).toFixed(2) }} {{ item.currency || 'CNY' }}</td>
          <td>{{ formatDuration(item.durationMonths) }}</td>
          <td>{{ item.status }}</td>
          <td>{{ formatDate(item.updatedAt || item.createdAt) }}</td>
          <td class="actions">
            <select v-model="item.status" @change="updateTransactionStatus(item, item.status)">
              <option value="pending">待处理</option>
              <option value="completed">已完成</option>
              <option value="cancelled">已取消</option>
            </select>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无财务记录</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const admin = inject('adminContext');
if (!admin) {
  throw new Error('adminContext not provided');
}

const {
  quarterFee,
  durationOptions,
  transactionForm,
  transactions,
  calculatedAmount,
  loadTransactions,
  createTransaction,
  resetTransactionForm,
  updateTransactionStatus,
  formatDuration,
  formatDate,
  formatCurrency
} = admin;
</script>
