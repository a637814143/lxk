<template>
  <section class="card">
    <div class="card__title">
      <h2>财务管理</h2>
      <button class="outline" @click="resetForm">重置</button>
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

    <p class="muted">创建后可在交易列表查看并进一步操作。</p>
  </section>
</template>

<script setup>
import { reactive } from 'vue';
import { post } from '../../api/http';
import { useToast } from '../../ui/toast';

const toast = useToast();

const transactionForm = reactive({
  companyId: null,
  amount: null,
  currency: '',
  type: '',
  reference: '',
  notes: ''
});

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
</script>

<style scoped>
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
</style>
