<template>
  <section class="review-section">
    <header class="section-head">
      <div>
        <p class="eyebrow">企业管理</p>
        <h2>企业列表</h2>
        <p class="muted">查看认证状态、服务状态与余额，支持审核</p>
      </div>
      <div class="head-actions">
        <select v-model="filters.status">
          <option value="all">全部状态</option>
          <option value="pending">待审核</option>
          <option value="approved">已通过</option>
          <option value="rejected">已驳回</option>
        </select>
        <button class="outline" @click="loadCompanies">刷新</button>
      </div>
    </header>

    <div v-if="companies.length" class="table-wrapper">
      <table class="table">
        <thead>
          <tr>
            <th>企业名称</th>
            <th>行业</th>
            <th>审核状态</th>
            <th>服务状态</th>
            <th>余额</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="company in companies" :key="company.id">
            <td>{{ company.companyName || '未填写' }}</td>
            <td>{{ company.industry || '未填写' }}</td>
            <td>
              <span class="status-pill" :class="statusClass(company.auditStatus)">
                {{ formatAuditStatus(company.auditStatus) }}
              </span>
            </td>
            <td>
              <div class="service-col">
                <span class="status-pill service" :class="serviceStatusClass(company.serviceStatus)">
                  {{ formatServiceStatus(company.serviceStatus) }}
                </span>
                <small class="muted">
                  {{ formatRemaining(company) }}
                </small>
              </div>
            </td>
            <td>{{ company.walletBalance ?? '—' }}</td>
            <td class="actions">
              <button class="outline" @click.stop="openDetail(company.id)">详情</button>
              <button
                class="primary"
                @click.stop="review(company.id, 'approved')"
                :disabled="company.auditStatus === 'approved'"
              >
                通过
              </button>
              <button
                class="outline"
                @click.stop="openRejectDialog(company)"
                :disabled="company.auditStatus === 'rejected'"
              >
                驳回
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-else class="muted">暂无符合条件的企业</p>

    <section v-if="rejectDialog.visible" class="card reject-card">
      <h3>驳回原因</h3>
      <form class="form-grid" @submit.prevent="submitReject">
        <label class="full">
          备注
          <textarea
            v-model="rejectDialog.reason"
            maxlength="255"
            placeholder="请输入驳回原因（可选）"
          ></textarea>
        </label>
        <div class="full actions">
          <button class="primary" type="submit">确认驳回</button>
          <button class="outline" type="button" @click="closeRejectDialog">取消</button>
        </div>
      </form>
    </section>

    <div v-if="detailDialog.visible" class="modal-backdrop" @click.self="closeDetail">
      <div class="modal">
        <header class="modal__header">
          <div>
            <h3>企业详情</h3>
            <p class="muted">企业 ID：{{ detailDialog.company?.id ?? '—' }}</p>
          </div>
          <button class="outline" type="button" @click="closeDetail">关闭</button>
        </header>
        <div class="modal__body" v-if="!detailDialog.loading && detailDialog.company">
          <div class="detail-grid">
            <div>
              <strong>企业名称：</strong>{{ detailDialog.company.companyName || '—' }}
            </div>
            <div><strong>统一社会信用代码：</strong>{{ detailDialog.company.licenseNumber || '—' }}</div>
            <div><strong>行业：</strong>{{ detailDialog.company.industry || '—' }}</div>
            <div><strong>地址：</strong>{{ detailDialog.company.address || '—' }}</div>
            <div>
              <strong>官网：</strong>
              <span v-if="detailDialog.company.website">
                <a :href="detailDialog.company.website" target="_blank" rel="noopener">{{ detailDialog.company.website }}</a>
              </span>
              <span v-else>—</span>
            </div>
            <div class="full">
              <strong>公司简介：</strong>
              <p class="pre">{{ detailDialog.company.description || '—' }}</p>
            </div>
            <div>
              <strong>审核状态：</strong>
              <span class="status-pill" :class="statusClass(detailDialog.company.auditStatus)">
                {{ formatAuditStatus(detailDialog.company.auditStatus) }}
              </span>
            </div>
            <div><strong>审核备注：</strong>{{ detailDialog.company.auditReason || '—' }}</div>
            <div><strong>邀请码已激活：</strong>{{ detailDialog.company.inviteActivated ? '是' : '否' }}</div>
            <div><strong>钱包余额：</strong>{{ detailDialog.company.walletBalance ?? '—' }}</div>
            <div>
              <strong>服务状态：</strong>
              <span class="status-pill service" :class="serviceStatusClass(detailDialog.company.serviceStatus)">
                {{ formatServiceStatus(detailDialog.company.serviceStatus) }}
              </span>
            </div>
            <div><strong>剩余时间：</strong>{{ formatRemaining(detailDialog.company) }}</div>
            <div><strong>服务到期：</strong>{{ formatDate(activeUntil(detailDialog.company)) || '—' }}</div>
            <div>
              <strong>Logo：</strong>
              <template v-if="detailDialog.company.logo">
                <a :href="detailDialog.company.logo" target="_blank" rel="noopener">查看</a>
              </template>
              <span v-else>—</span>
            </div>
            <div>
              <strong>执照/证明：</strong>
              <template v-if="detailDialog.company.licenseDocument">
                <a :href="detailDialog.company.licenseDocument" target="_blank" rel="noopener">查看</a>
              </template>
              <span v-else>—</span>
            </div>
          </div>
        </div>
        <div class="modal__body" v-else>正在加载企业详情...</div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue';
import { get, post } from '../../api/http';
import { useToast } from '../../ui/toast';

const companies = ref([]);
const toast = useToast();

const filters = reactive({
  status: 'all'
});

const rejectDialog = reactive({
  visible: false,
  companyId: null,
  reason: ''
});

const detailDialog = reactive({
  visible: false,
  loading: false,
  company: null
});

onMounted(() => {
  loadCompanies();
});

watch(
  () => filters.status,
  () => loadCompanies()
);

async function loadCompanies() {
  try {
    const query = filters.status && filters.status !== 'all' ? `?status=${filters.status}` : '';
    companies.value = await get(`/portal/admin/companies${query}`);
  } catch (error) {
    toast.error(error.message ?? '加载企业列表失败');
  }
}

async function review(companyId, status, reason = '') {
  try {
    await post(`/portal/admin/companies/${companyId}/review`, { status, reason });
    toast.success('审核结果已提交');
    await loadCompanies();
    if (detailDialog.visible) {
      await openDetail(companyId);
    }
  } catch (error) {
    toast.error(error.message ?? '提交审核失败');
  }
}

function openRejectDialog(company) {
  rejectDialog.visible = true;
  rejectDialog.companyId = company.id;
  rejectDialog.reason = '';
}

function closeRejectDialog() {
  rejectDialog.visible = false;
  rejectDialog.companyId = null;
  rejectDialog.reason = '';
}

async function submitReject() {
  if (!rejectDialog.companyId) return;
  await review(rejectDialog.companyId, 'rejected', rejectDialog.reason);
  closeRejectDialog();
}

async function openDetail(companyId) {
  detailDialog.visible = true;
  detailDialog.loading = true;
  detailDialog.company = null;
  try {
    detailDialog.company = await get(`/portal/admin/companies/${companyId}`);
  } catch (error) {
    toast.error(error.message ?? '加载企业详情失败');
  } finally {
    detailDialog.loading = false;
  }
}

function closeDetail() {
  detailDialog.visible = false;
  detailDialog.loading = false;
  detailDialog.company = null;
}

function formatAuditStatus(status) {
  if (status === 'approved') return '已通过';
  if (status === 'rejected') return '已驳回';
  return '待审核';
}

function statusClass(status) {
  return {
    success: status === 'approved',
    danger: status === 'rejected',
    warning: status !== 'approved' && status !== 'rejected'
  };
}

function formatServiceStatus(status) {
  if (status === 'active') return '订阅中';
  if (status === 'trial') return '试用中';
  return '已过期';
}

function serviceStatusClass(status) {
  return {
    'service-active': status === 'active',
    'service-trial': status === 'trial',
    'service-expired': status !== 'active' && status !== 'trial'
  };
}

function formatRemaining(company) {
  const seconds = company?.serviceRemainingSeconds ?? 0;
  if (!seconds) return '已到期';
  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  if (days > 0) return `${days}天${hours}小时`;
  if (hours > 0) return `${hours}小时${minutes}分`;
  return `${Math.max(minutes, 1)}分钟`;
}

function activeUntil(company) {
  if (!company) return null;
  const { serviceStatus, subscriptionExpiresAt, trialEndsAt } = company;
  if (serviceStatus === 'active' && subscriptionExpiresAt) return subscriptionExpiresAt;
  if (serviceStatus === 'trial' && trialEndsAt) return trialEndsAt;
  return subscriptionExpiresAt || trialEndsAt || null;
}

function formatDate(value) {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString('zh-CN', { hour12: false });
}
</script>

<style scoped>
.review-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.eyebrow {
  margin: 0 0 4px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #2563eb;
  font-weight: 700;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.head-actions select {
  height: 36px;
  border-radius: 8px;
  border: 1px solid var(--border-subtle);
  padding: 0 12px;
  background: #fff;
}

.table-wrapper {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid var(--border-subtle);
  border-radius: 14px;
  box-shadow: var(--shadow-soft);
  overflow: hidden;
}

.actions {
  display: flex;
  gap: 8px;
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
  background: var(--color-success-soft);
  border-color: rgba(22, 163, 74, 0.3);
  color: var(--color-success);
}

.status-pill.warning {
  background: #fef3c7;
  border-color: rgba(251, 191, 36, 0.4);
  color: #b45309;
}

.status-pill.danger {
  background: var(--color-danger-soft);
  border-color: rgba(185, 28, 28, 0.3);
  color: var(--color-danger);
}

.status-pill.service {
  border-color: transparent;
}

.status-pill.service.service-active {
  background: rgba(37, 99, 235, 0.12);
  color: #1d4ed8;
}

.status-pill.service.service-trial {
  background: rgba(251, 191, 36, 0.2);
  color: #b45309;
}

.status-pill.service.service-expired {
  background: rgba(148, 163, 184, 0.3);
  color: #475569;
}

.service-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reject-card {
  background: rgba(255, 255, 255, 0.96);
  border-radius: 14px;
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-soft);
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  border-radius: 12px;
  width: min(900px, 94vw);
  max-height: 82vh;
  overflow: auto;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.18);
}

.modal__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.modal__body {
  padding: 16px 20px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px 18px;
}

.pre {
  white-space: pre-wrap;
  color: #475569;
  line-height: 1.6;
}

.table tbody tr:hover {
  background: #f8fafc;
}

textarea {
  width: 100%;
  border-radius: 10px;
  border: 1px solid var(--border-subtle);
  padding: 10px 12px;
  min-height: 120px;
  resize: vertical;
}
</style>
