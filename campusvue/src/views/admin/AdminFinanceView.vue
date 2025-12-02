<template>
  <section class="card">
    <div class="card__title">
      <div>
        <h2>财务管理</h2>
        <p class="muted">企业扣款自动生成管理员收入记录，支持人工补记</p>
      </div>
      <div class="head-actions">
        <button class="outline" @click="loadTransactions">刷新记录</button>
        <button class="outline" @click="resetForm">重置表单</button>
      </div>
    </div>

    <form class="form-grid" @submit.prevent="createTransaction">
      <label>企业ID<input v-model.number="transactionForm.companyId" type="number" min="1" required /></label>
      <label>金额（元）<input v-model.number="transactionForm.amount" type="number" min="0" step="0.01" required /></label>
      <label>币种<input v-model="transactionForm.currency" placeholder="默认 CNY" /></label>
      <label class="full">费用用途<input v-model="transactionForm.type" required maxlength="50" placeholder="例如：平台服务费" /></label>
      <label class="full">业务编号<input v-model="transactionForm.reference" maxlength="100" placeholder="可选的内部参考编号" /></label>
      <label class="full">备注<textarea v-model="transactionForm.notes" maxlength="255"></textarea></label>
      <div class="full actions">
        <button class="primary" type="submit">创建交易</button>
      </div>
    </form>

    <div class="table-wrapper" v-if="transactions.length">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>企业</th>
            <th>金额</th>
            <th>类型</th>
            <th>状态</th>
            <th>备注</th>
            <th>创建时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tx in transactions" :key="tx.id">
            <td>{{ tx.id }}</td>
            <td>
              <div class="company-col">
                <strong>{{ tx.companyName || `#${tx.companyId}` || '—' }}</strong>
                <small class="muted">{{ tx.reference || '无编号' }}</small>
              </div>
            </td>
            <td>
              <span :class="tx.amount >= 0 ? 'text-success' : 'text-danger'">
                {{ tx.amount }} {{ tx.currency || 'CNY' }}
              </span>
              <small class="muted" v-if="isAuto(tx)">自动生成</small>
            </td>
            <td>{{ tx.type }}</td>
            <td>
              <span class="status-pill" :class="statusClass(tx.status)">{{ formatStatus(tx.status) }}</span>
              <small class="muted" v-if="tx.adminName">由 {{ tx.adminName }}</small>
            </td>
            <td>{{ tx.notes || '—' }}</td>
            <td>{{ formatDate(tx.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else class="muted">暂无交易记录</p>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { get, post } from '../../api/http';
import { useToast } from '../../ui/toast';

const toast = useToast();
const transactions = ref([]);

const transactionForm = reactive({
  companyId: null,
  amount: null,
  currency: '',
  type: '',
  reference: '',
  notes: ''
});

onMounted(() => {
  loadTransactions();
});

async function loadTransactions() {
  try {
    transactions.value = await get('/portal/admin/transactions');
  } catch (error) {
    toast.error(error.message ?? '加载交易记录失败');
  }
}

async function createTransaction() {
  try {
    await post('/portal/admin/transactions', {
      companyId: transactionForm.companyId,
      amount: transactionForm.amount,
      currency: transactionForm.currency || 'CNY',
      type: transactionForm.type,
      reference: transactionForm.reference,
      notes: transactionForm.notes
    });
    toast.success('交易已创建');
    resetForm();
    await loadTransactions();
  } catch (error) {
    toast.error(error.message ?? '创建交易失败');
  }
}

function resetForm() {
  transactionForm.companyId = null;
  transactionForm.amount = null;
  transactionForm.currency = '';
  transactionForm.type = '';
  transactionForm.reference = '';
  transactionForm.notes = '';
}

function statusClass(status) {
  const s = (status || '').toLowerCase();
  return {
    success: s === 'completed',
    warning: s === 'pending',
    danger: s === 'cancelled'
  };
}

function formatStatus(status) {
  if (!status) return '—';
  const s = status.toLowerCase();
  if (s === 'completed') return '已完成';
  if (s === 'pending') return '待处理';
  if (s === 'cancelled') return '已取消';
  return status;
}

function formatDate(value) {
  if (!value) return '—';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString('zh-CN', { hour12: false });
}

function isAuto(tx) {
  return (tx.reference && tx.reference.includes('mirror')) || (tx.notes && tx.notes.includes('自动生成'));
}
</script>

<style scoped>
.head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 10px;
}

.form-grid input,
.form-grid textarea {
  border-radius: 10px;
  border: 1px solid #d1d5db;
  padding: 10px 12px;
  font-size: 14px;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.form-grid input:focus,
.form-grid textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.25);
  outline: none;
}

.actions {
  display: flex;
  gap: 12px;
}

.table-wrapper {
  margin-top: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.company-col {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.6);
  background: #f1f5f9;
  color: #0f172a;
  font-weight: 700;
  font-size: 12px;
}

.status-pill.success {
  background: #dcfce7;
  border-color: rgba(34, 197, 94, 0.4);
  color: #15803d;
}

.status-pill.warning {
  background: #fef3c7;
  border-color: rgba(251, 191, 36, 0.4);
  color: #b45309;
}

.status-pill.danger {
  background: #fee2e2;
  border-color: rgba(248, 113, 113, 0.4);
  color: #b91c1c;
}

.text-success {
  color: #15803d;
  font-weight: 700;
}

.text-danger {
  color: #b91c1c;
  font-weight: 700;
}
</style>
