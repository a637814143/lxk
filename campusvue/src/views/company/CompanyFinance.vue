<template>
  <section class="card">
    <div class="card__title">
      <h2>财务往来</h2>
      <button class="outline" type="button" @click="loadTransactions">刷新</button>
    </div>
    <div class="billing-summary">
      <span>钱包余额：￥{{ formatCurrency(walletBalance.value) }}</span>
      <span>季度单价：￥{{ formatCurrency(quarterFee.value) }}</span>
      <span>本次扣款：￥{{ formatCurrency(calculatedAmount.value) }}</span>
    </div>
    <form class="form-grid" @submit.prevent="submitTransaction">
      <label>
        使用时长
        <select v-model.number="transactionForm.durationQuarters">
          <option v-for="option in durationOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </label>
      <label>
        费用用途
        <input v-model="transactionForm.type" placeholder="季度服务费" />
      </label>
      <label class="full">业务编号<input v-model="transactionForm.reference" placeholder="可选的内部参考编号" /></label>
      <label class="full">备注<textarea v-model="transactionForm.notes" placeholder="补充说明（可选）"></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">立即扣款</button>
        <button class="outline" type="button" @click="resetTransactionForm">清空</button>
      </div>
    </form>
    <table v-if="transactions.length" class="table">
      <thead>
        <tr><th>用途</th><th>金额</th><th>币种</th><th>服务时长</th><th>状态</th><th>更新时间</th><th>备注</th></tr>
      </thead>
      <tbody>
        <tr v-for="item in transactions" :key="item.id">
          <td>{{ item.type }}</td>
          <td>{{ Number(item.amount ?? 0).toFixed(2) }}</td>
          <td>{{ item.currency || 'CNY' }}</td>
          <td>{{ formatDuration(item.durationMonths) }}</td>
          <td>{{ item.status }}</td>
          <td>{{ formatDate(item.updatedAt || item.createdAt) }}</td>
          <td>{{ item.notes || '-' }}</td>
        </tr>
      </tbody>
    </table>
    <p v-else class="muted">暂无财务记录</p>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const company = inject('companyContext');
if (!company) {
  throw new Error('companyContext not provided');
}

const {
  walletBalance,
  quarterFee,
  durationOptions,
  transactionForm,
  transactions,
  calculatedAmount,
  loadTransactions,
  submitTransaction,
  resetTransactionForm,
  formatDuration,
  formatDate,
  formatCurrency
} = company;
</script>
