<template>
  <section class="card finance-card">
    <header class="card__title finance-header">
      <div>
        <h2>财务往来</h2>
        <p class="muted">查看钱包余额、充值记录与扣款记录</p>
      </div>
      <div class="actions">
        <div class="wallet-badge">
          <span class="wallet-label">钱包余额</span>
          <span class="wallet-amount">￥{{ formatMoney(walletBalance) }}</span>
        </div>
        <div class="service-badge" :class="serviceStatusClass">
          <div class="service-row">
            <span class="service-label">服务状态</span>
            <span class="service-chip">{{ serviceStatusText }}</span>
          </div>
          <div class="service-row">
            <span class="service-label">剩余时长</span>
            <span class="service-remaining">{{ remainingText }}</span>
          </div>
          <p class="service-deadline" v-if="deadlineText">到期：{{ deadlineText }}</p>
          <p class="muted service-hint">{{ statusHint }}</p>
        </div>
        <button class="outline" @click="refreshWallet">刷新余额</button>
        <button class="outline" @click="loadTransactions">刷新记录</button>
      </div>
    </header>

    <div class="finance-grid">
      <form class="form-grid form-card" @submit.prevent="purchaseSubscription">
        <h3 class="form-title">购买季度服务</h3>
        <label>
          季度数量
          <input
            v-model.number="subscriptionForm.quarters"
            type="number"
            min="1"
            max="12"
            required
          />
        </label>
        <label>
          每季度价格（元）
          <input
            v-model.number="subscriptionForm.quarterPrice"
            type="number"
            min="0"
            step="0.01"
          />
        </label>
        <label class="full">
          备注
          <textarea
            v-model="subscriptionForm.note"
            maxlength="255"
            placeholder="填写使用场景或说明（可选）"
          ></textarea>
        </label>
        <label class="full">
          参考编号
          <input
            v-model="subscriptionForm.reference"
            maxlength="100"
            placeholder="内部参考编号（可选）"
          />
        </label>
        <div class="full actions">
          <button class="primary" type="submit">确认扣款</button>
        </div>
        <p class="muted tip">
          预计扣款：￥{{ formatMoney(subscriptionTotal) }}，扣款成功后金额将自动转入系统管理员账户。
        </p>
      </form>

      <form class="form-grid form-card" @submit.prevent="rechargeWalletSubmit">
        <h3 class="form-title">余额充值</h3>
        <label>
          充值金额（元）
          <input
            v-model.number="rechargeForm.amount"
            type="number"
            min="0.01"
            step="0.01"
            required
          />
        </label>
        <label>
          币种
          <input v-model="rechargeForm.currency" maxlength="10" placeholder="默认 CNY，可留空" />
        </label>
        <label class="full">
          参考编号
          <input
            v-model="rechargeForm.reference"
            maxlength="100"
            placeholder="内部参考编号（可选）"
          />
        </label>
        <label class="full">
          备注
          <textarea
            v-model="rechargeForm.note"
            maxlength="255"
            placeholder="例如：测试充值，不做真实扣款等"
          ></textarea>
        </label>
        <div class="full actions">
          <button class="outline" type="submit">确认充值</button>
        </div>
        <p class="muted tip">仅用于演示 / 测试环境，不会发起真实支付。</p>
      </form>
    </div>

    <section class="transactions-section">
      <h3 class="transactions-title">资金流水</h3>
      <table v-if="transactions.length" class="table finance-table">
        <thead>
          <tr>
            <th>用途</th>
            <th>金额</th>
            <th>币种</th>
            <th>状态</th>
            <th>更新时间</th>
            <th>备注</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in transactions" :key="item.id">
            <td>{{ item.type }}</td>
            <td>
              <span
                :class="[
                  'amount',
                  Number(item.amount) >= 0 ? 'amount--in' : 'amount--out'
                ]"
              >
                {{ formatMoney(item.amount) }}
              </span>
            </td>
            <td>{{ item.currency || 'CNY' }}</td>
            <td>
              <span
                :class="[
                  'status-badge',
                  'status-badge--' + (String(item.status || '').toLowerCase())
                ]"
              >
                {{ formatStatus(item.status) }}
              </span>
            </td>
            <td>{{ formatDate(item.updatedAt || item.createdAt) }}</td>
            <td>{{ item.notes || '-' }}</td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted no-data">暂无财务记录</p>
    </section>
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
const serviceStatus = computed(() => walletSummary.value?.serviceStatus || 'trial');
const remainingSeconds = computed(() => Number(walletSummary.value?.remainingSeconds ?? 0));
const activeDeadline = computed(() => {
  if (!walletSummary.value) return null;
  if (serviceStatus.value === 'active' && walletSummary.value.subscriptionExpiresAt) {
    return walletSummary.value.subscriptionExpiresAt;
  }
  if (serviceStatus.value === 'trial' && walletSummary.value.trialEndsAt) {
    return walletSummary.value.trialEndsAt;
  }
  return walletSummary.value.subscriptionExpiresAt || walletSummary.value.trialEndsAt || null;
});
const remainingText = computed(() => {
  if (!walletSummary.value) return '加载中...';
  return formatRemaining(remainingSeconds.value);
});
const deadlineText = computed(() => (activeDeadline.value ? formatDate(activeDeadline.value) : ''));
const serviceStatusText = computed(() => {
  if (!walletSummary.value) return '加载中';
  if (serviceStatus.value === 'active') return '订阅中';
  if (serviceStatus.value === 'trial') return '试用中';
  return '已到期';
});
const serviceStatusClass = computed(() => ({
  ...(walletSummary.value
    ? {
        'service-badge--active': serviceStatus.value === 'active',
        'service-badge--trial': serviceStatus.value === 'trial',
        'service-badge--expired': serviceStatus.value === 'expired'
      }
    : {})
}));
const statusHint = computed(() => {
  if (!walletSummary.value) return '正在读取服务状态...';
  if (serviceStatus.value === 'trial') return '未购买前自动开启24小时试用。';
  if (serviceStatus.value === 'active') return '季度服务已开通，过期前可续订。';
  return '服务已到期，请购买季度服务后继续使用。';
});
const subscriptionForm = reactive({ quarters: 1, quarterPrice: 1999, note: '', reference: '' });
const rechargeForm = reactive({ amount: null, currency: 'CNY', reference: '', note: '' });

const subscriptionTotal = computed(
  () => Number(subscriptionForm.quarters || 0) * Number(subscriptionForm.quarterPrice || 0)
);

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

async function rechargeWalletSubmit() {
  if (!rechargeForm.amount || Number(rechargeForm.amount) <= 0) {
    toast.error('请输入正确的充值金额');
    return;
  }
  try {
    await post('/portal/company/wallet/recharge', {
      amount: rechargeForm.amount,
      currency: rechargeForm.currency || 'CNY',
      reference: rechargeForm.reference,
      note: rechargeForm.note
    });
    toast.success('充值成功，已更新钱包余额（虚拟充值）');
    rechargeForm.amount = null;
    await refreshWallet();
    await loadTransactions();
  } catch (error) {
    toast.error(error.message ?? '充值失败');
  }
}

function formatRemaining(seconds) {
  const totalSeconds = Math.max(0, Math.floor(Number(seconds ?? 0)));
  if (!totalSeconds) return '已到期';
  const days = Math.floor(totalSeconds / 86400);
  const hours = Math.floor((totalSeconds % 86400) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  if (days > 0) {
    return `${days}天${hours > 0 ? `${hours}小时` : ''}`.trim();
  }
  if (hours > 0) {
    return `${hours}小时${minutes > 0 ? `${minutes}分钟` : ''}`.trim();
  }
  return `${Math.max(1, minutes)}分钟`;
}

function formatMoney(value) {
  const amount = Number(value ?? 0);
  return amount.toFixed(2);
}

function formatDate(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString();
}

function formatStatus(value) {
  if (!value) return '-';
  const s = String(value).toLowerCase();
  if (s === 'completed') return '已完成';
  if (s === 'pending') return '待处理';
  if (s === 'cancelled') return '已取消';
  return value;
}
</script>

<style scoped>
.finance-card {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.finance-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.finance-header h2 {
  margin: 0;
  font-size: 22px;
}

.finance-header .muted {
  margin-top: 4px;
  font-size: 13px;
}

.wallet-badge {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 8px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, #eff6ff, #dbeafe);
  border: 1px solid #bfdbfe;
}

.wallet-label {
  font-size: 12px;
  color: #2563eb;
}

.wallet-amount {
  font-size: 18px;
  font-weight: 600;
  color: #1d4ed8;
}

.service-badge {
  display: grid;
  grid-template-columns: 1fr;
  gap: 6px;
  padding: 10px 14px;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: linear-gradient(135deg, #eef2ff, #ecfeff);
  min-width: 240px;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.06);
}

.service-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.service-label {
  font-size: 12px;
  color: #475569;
}

.service-chip {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  border: 1px solid #bae6fd;
  background: #e0f2fe;
  color: #0ea5e9;
}

.service-remaining {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.service-deadline {
  margin: 0;
  font-size: 12px;
  color: #475569;
}

.service-hint {
  margin: 0;
  font-size: 12px;
}

.service-badge--active {
  border-color: #22c55e;
  background: linear-gradient(135deg, #ecfdf3, #f0fdf4);
}

.service-badge--active .service-chip {
  background: #dcfce7;
  border-color: #bbf7d0;
  color: #15803d;
}

.service-badge--trial {
  border-color: #facc15;
  background: linear-gradient(135deg, #fffbeb, #fef9c3);
}

.service-badge--trial .service-chip {
  background: #fef3c7;
  border-color: #fde68a;
  color: #b45309;
}

.service-badge--expired {
  border-color: #e2e8f0;
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
}

.service-badge--expired .service-chip {
  background: #e2e8f0;
  border-color: #cbd5e1;
  color: #475569;
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.finance-grid {
  display: grid;
  gap: 20px;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  align-items: flex-start;
}

.form-card {
  background: #f9fafb;
  border-radius: 16px;
  padding: 16px 18px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.form-title {
  margin: 0 0 8px;
  color: #111827;
  font-size: 16px;
}

.form-card label {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #4b5563;
}

.form-card input,
.form-card textarea {
  border-radius: 10px;
  border: 1px solid #d1d5db;
  padding: 10px 12px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.form-card input:focus,
.form-card textarea:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.25);
}

.form-card textarea {
  min-height: 72px;
  resize: vertical;
}

.form-card .actions {
  justify-content: flex-start;
}

.tip {
  margin: 4px 0 0;
  font-size: 12px;
}

.transactions-section {
  margin-top: 8px;
}

.transactions-title {
  margin: 0 0 8px;
  font-size: 16px;
  color: #111827;
}

.finance-table {
  margin-top: 4px;
  font-size: 13px;
}

.finance-table th {
  background: #f9fafb;
  font-weight: 500;
}

.finance-table td,
.finance-table th {
  padding: 8px 10px;
}

.amount {
  font-variant-numeric: tabular-nums;
}

.amount--in {
  color: #16a34a;
}

.amount--out {
  color: #dc2626;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
}

.status-badge--completed {
  background: #dcfce7;
  color: #166534;
}

.status-badge--pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge--cancelled {
  background: #fee2e2;
  color: #b91c1c;
}

.no-data {
  margin-top: 8px;
}

@media (max-width: 768px) {
  .finance-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .actions {
    flex-wrap: wrap;
  }
}
</style>
