<template>
  <section class="card">
    <div class="card__title">
      <h2>财务往来</h2>
      <div class="actions">
        <span class="muted">钱包余额：￥{{ formatMoney(walletBalance) }}</span>
        <button class="outline" @click="refreshWallet">刷新余额</button>
        <button class="outline" @click="loadTransactions">刷新记录</button>
      </div>
    </div>

    <div class="finance-grid">
      <form class="form-grid" @submit.prevent="purchaseSubscription">
        <h3 class="form-title">购买季度服务</h3>
        <label>季度数量<input v-model.number="subscriptionForm.quarters" type="number" min="1" max="12" required /></label>
        <label>每季度价格（元）<input v-model.number="subscriptionForm.quarterPrice" type="number" min="0" step="0.01" /></label>
        <label class="full">备注<textarea v-model="subscriptionForm.note" maxlength="255" placeholder="填写使用场景或说明（可选）"></textarea></label>
        <label class="full">参考编号<input v-model="subscriptionForm.reference" maxlength="100" placeholder="内部参考编号（可选）" /></label>
        <div class="full actions">
          <button class="primary" type="submit">确认扣款</button>
        </div>
        <p class="muted">预计扣款：￥{{ formatMoney(subscriptionTotal) }}，扣款成功后金额将自动转入系统管理员账户。</p>
      </form>
    </div>

    <table v-if="transactions.length" class="table">
      <thead>
        <tr><th>用途</th><th>金额</th><th>币种</th><th>状态</th><th>更新时间</th><th>备注</th></tr>
      </thead>
      <tbody>
        <tr v-for="item in transactions" :key="item.id">
          <td>{{ item.type }}</td>
          <td>{{ formatMoney(item.amount) }}</td>
          <td>{{ item.currency || 'CNY' }}</td>
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
import { computed, inject, onMounted, reactive, ref } from 'vue';
import { get, post } from '../../api/http';
import { useToast } from '../../ui/toast';

const walletSummary = inject('walletSummary', ref(null));
const refreshWallet = inject('refreshWallet', () => Promise.resolve());
const toast = useToast();

const transactions = ref([]);
const walletBalance = computed(() => Number(walletSummary.value?.balance ?? 0));
const subscriptionForm = reactive({ quarters: 1, quarterPrice: 1999, note: '', reference: '' });

const subscriptionTotal = computed(() => Number(subscriptionForm.quarters || 0) * Number(subscriptionForm.quarterPrice || 0));

onMounted(loadTransactions);

async function loadTransactions() {
  try {
    transactions.value = await get('/portal/company/transactions');
  } catch (error) {
    toast.error(error.message ?? '加载财务记录失败');
  }
}

async function purchaseSubscription() {
  if (!subscriptionForm.quarters || subscriptionForm.quarters <= 0) {
    toast.error('请选择正确的季度数量');
    return;
  }
  try {
    await post('/portal/company/wallet/subscription', {
      quarters: subscriptionForm.quarters,
      quarterPrice: subscriptionForm.quarterPrice,
      note: subscriptionForm.note,
      reference: subscriptionForm.reference
    });
    toast.success('扣款成功，已开通季度服务');
    await refreshWallet();
    await loadTransactions();
  } catch (error) {
    toast.error(error.message ?? '扣款失败，请检查余额');
  }
}

function formatMoney(value) { const amount = Number(value ?? 0); return amount.toFixed(2); }
function formatDate(value) { if (!value) return '-'; return new Date(value).toLocaleString(); }
</script>

<style scoped>
.finance-grid { display: grid; gap: 24px; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); }
.form-title { margin: 0; color: #1e293b; }
.actions { display: flex; gap: 12px; align-items: center; }
</style>
