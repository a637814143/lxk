<template>
  <section class="card">
    <div class="card__title">
      <h2>财务记录管理</h2>
      <div class="actions">
        <button class="outline" @click="loadTransactions">刷新</button>
        <button class="outline" @click="emit('request-wallet-refresh')">刷新钱包</button>
      </div>
    </div>

    <form class="form-grid" @submit.prevent="createTransaction">
      <label>企业ID<input v-model.number="transactionForm.companyId" type="number" min="1" required /></label>
      <label>金额（元）<input v-model.number="transactionForm.amount" type="number" min="0" step="0.01" required /></label>
      <label>币种<input v-model="transactionForm.currency" placeholder="默认 CNY" /></label>
      <label class="full">费用用途<input v-model="transactionForm.type" required maxlength="50" placeholder="例如：平台服务费" /></label>
      <label class="full">业务编号<input v-model="transactionForm.reference" maxlength="100" placeholder="可选的内部参考编号" /></label>
      <label class="full">备注<textarea v-model="transactionForm.notes" maxlength="255" placeholder="补充说明（可选）"></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">创建记录</button>
        <button class="outline" type="button" @click="resetTransactionForm">重置</button>
      </div>
    </form>

    <table v-if="transactions.length" class="table">
      <thead><tr><th>ID</th><th>企业ID</th><th>用途</th><th>金额</th><th>状态</th><th>更新时间</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="item in transactions" :key="item.id">
          <td>{{ item.id }}</td>
          <td>{{ item.companyId }}</td>
          <td>{{ item.type }}</td>
          <td>{{ formatMoney(item.amount) }} {{ item.currency || 'CNY' }}</td>
          <td>{{ item.status }}</td>
          <td>{{ formatDate(item.updatedAt || item.createdAt) }}</td>
          <td class="actions">
            <select v-model="item.status" @change="updateTransactionStatus(item)">
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
import { defineEmits, onMounted, reactive, ref } from 'vue';
import { get, patch, post } from '../../api/http';
import { useToast } from '../../ui/toast';

const emit = defineEmits(['request-wallet-refresh']);

const transactions = ref([]);
const toast = useToast();

const transactionForm = reactive({
  companyId: null,
  amount: null,
  currency: 'CNY',
  type: '',
  reference: '',
  notes: ''
});

onMounted(loadTransactions);

async function loadTransactions() {
  try {
    transactions.value = await get('/portal/admin/transactions');
  } catch (error) {
    toast.error(error.message ?? '加载财务记录失败');
  }
}

async function createTransaction() {
  if (!transactionForm.companyId || !transactionForm.amount || !transactionForm.type) {
    toast.error('请完整填写企业ID、金额和费用用途');
    return;
  }
  try {
    await post('/portal/admin/transactions', transactionForm);
    toast.success('财务记录已创建');
    resetTransactionForm();
    await loadTransactions();
    emit('request-wallet-refresh');
  } catch (error) {
    toast.error(error.message ?? '创建财务记录失败');
  }
}

async function updateTransactionStatus(item) {
  try {
    await patch(`/portal/admin/transactions/${item.id}`, { status: item.status, notes: item.notes });
    toast.success('交易状态已更新');
    await loadTransactions();
    emit('request-wallet-refresh');
  } catch (error) {
    toast.error(error.message ?? '更新交易状态失败');
  }
}

function resetTransactionForm() {
  transactionForm.companyId = null;
  transactionForm.amount = null;
  transactionForm.currency = 'CNY';
  transactionForm.type = '';
  transactionForm.reference = '';
  transactionForm.notes = '';
}

function formatMoney(value) {
  const amount = Number(value ?? 0);
  return amount.toFixed(2);
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString();
}
</script>

<style scoped>
.actions { display: flex; gap: 12px; }
</style>

