<template>
  <section class="profile-section">
    <header class="profile-header">
      <div>
        <p class="eyebrow">企业资料</p>
        <h2>完善企业基本信息</h2>
        <p class="muted">补充认证信息，方便管理员审核并解锁全部功能。</p>
      </div>
      <div class="balance chip">
        <span>当前余额</span>
        <strong>￥{{ formatMoney(currentBalance) }}</strong>
        <button class="outline" type="button" @click="refreshWallet">刷新</button>
      </div>
    </header>

    <div class="profile-layout">
      <section class="profile-card">
        <header class="card-head">
          <div>
            <p class="eyebrow small">企业信息</p>
            <h3>基础资料</h3>
          </div>
          <button class="outline" type="button" @click="loadProfile">重新加载</button>
        </header>

        <form class="field-grid" @submit.prevent="saveProfile">
          <label class="field">
            <span>企业名称</span>
            <input v-model="profileForm.companyName" required maxlength="100" placeholder="请输入营业执照上的名称" />
          </label>
          <label class="field">
            <span>统一社会信用代码</span>
            <input v-model="profileForm.licenseNumber" maxlength="100" placeholder="用于管理员审核" />
          </label>
          <label class="field">
            <span>所属行业</span>
            <input v-model="profileForm.industry" maxlength="100" placeholder="如：互联网 / 教育 / 制造业" />
          </label>
          <label class="field">
            <span>企业地址</span>
            <input v-model="profileForm.address" maxlength="255" placeholder="省市区 + 详细地址" />
          </label>
          <label class="field">
            <span>企业官网</span>
            <input v-model="profileForm.website" maxlength="255" placeholder="https://example.com" />
          </label>
          <label class="field full">
            <span>企业简介</span>
            <textarea
              v-model="profileForm.description"
              maxlength="255"
              placeholder="简要介绍公司业务、规模与亮点"
            ></textarea>
          </label>
          <label class="field full">
            <span>Logo 链接</span>
            <input v-model="profileForm.logo" maxlength="255" placeholder="可填写在线 Logo 图片地址" />
          </label>

          <div class="field full upload-area">
            <div>
              <p class="label">营业执照文件</p>
              <p class="muted">支持 PDF / JPG / PNG，上传后用于管理员审核。</p>
              <p class="muted" v-if="profileForm.licenseDocument">
                当前文件：
                <a :href="profileForm.licenseDocument" target="_blank" rel="noopener">点击查看</a>
              </p>
            </div>
            <div class="upload-actions">
              <label class="upload-input">
                <input
                  ref="licenseFileInput"
                  type="file"
                  accept=".pdf,.jpg,.jpeg,.png"
                  @change="handleLicenseFile"
                />
                <span>{{ licenseFile?.name || '选择文件' }}</span>
              </label>
              <button class="outline" type="button" @click="uploadLicense" :disabled="!licenseFile">上传</button>
            </div>
          </div>

          <div class="full actions">
            <button class="primary" type="submit">保存资料</button>
            <button class="outline" type="button" @click="loadProfile">取消修改</button>
          </div>
        </form>
      </section>

      <aside class="aside">
        <div class="aside-card">
          <p class="label">功能激活</p>
          <p class="muted">
            当前状态：
            <span class="status" :class="profileForm.inviteActivated ? 'success' : 'warning'">
              {{ profileForm.inviteActivated ? '已激活' : '待激活' }}
            </span>
          </p>
          <div class="activation-row">
            <input
              v-model="inviteCode"
              type="text"
              maxlength="64"
              placeholder="请输入管理员提供的邀请码"
              :disabled="activating || profileForm.inviteActivated"
            />
            <button
              class="outline"
              type="button"
              @click="submitInvite"
              :disabled="activating || !inviteCode || profileForm.inviteActivated"
            >
              {{ activating ? '验证中...' : '提交邀请码' }}
            </button>
          </div>
          <small class="muted">
            管理员审核通过后会下发邀请码。激活成功后，即可发布职位、查看投递与参与讨论。
          </small>
        </div>

        <div class="aside-card tips">
          <p class="label">填写提示</p>
          <ul>
            <li>确保企业名称、信用代码与执照文件一致，避免审核被驳回。</li>
            <li>简介突出核心业务、规模、所在地、技术/产品亮点。</li>
            <li>Logo 与官网链接有助于提升企业可信度。</li>
          </ul>
        </div>
      </aside>
    </div>
  </section>
</template>

<script setup>
import { computed, inject, onMounted, reactive, ref } from 'vue';
import { get, post, put, upload } from '../../api/http';
import { useToast } from '../../ui/toast';

const walletSummary = inject('walletSummary', ref(null));
const refreshWallet = inject('refreshWallet', () => Promise.resolve());
const toast = useToast();

const profileForm = reactive({
  companyName: '',
  licenseNumber: '',
  industry: '',
  address: '',
  website: '',
  description: '',
  logo: '',
  licenseDocument: '',
  walletBalance: 0,
  inviteActivated: false
});
const licenseFile = ref(null);
const licenseFileInput = ref(null);
const inviteCode = ref('');
const activating = ref(false);

const currentBalance = computed(() => walletSummary?.value?.balance ?? profileForm.walletBalance ?? 0);

onMounted(loadProfile);

async function loadProfile() {
  try {
    const data = await get('/portal/company/profile');
    Object.assign(profileForm, data);
    await refreshWallet();
  } catch (error) {
    if (error.status !== 404) {
      toast.error(error.message ?? '加载企业资料失败');
    }
  }
}

async function saveProfile() {
  try {
    const payload = { ...profileForm };
    delete payload.walletBalance;
    delete payload.inviteActivated;
    const data = await put('/portal/company/profile', payload);
    Object.assign(profileForm, data);
    toast.success('企业资料已保存');
    await refreshWallet();
  } catch (error) {
    toast.error(error.message ?? '保存失败，请稍后再试');
  }
}

function handleLicenseFile(event) {
  const [file] = event.target.files ?? [];
  licenseFile.value = file ?? null;
}

async function uploadLicense() {
  if (!licenseFile.value) {
    toast.error('请先选择要上传的文件');
    return;
  }
  try {
    const formData = new FormData();
    formData.append('file', licenseFile.value);
    const data = await upload('/portal/company/profile/license', formData);
    Object.assign(profileForm, data);
    toast.success('营业执照上传成功，等待管理员审核');
    await refreshWallet();
  } catch (error) {
    toast.error(error.message ?? '上传失败，请稍后再试');
  } finally {
    licenseFile.value = null;
    if (licenseFileInput.value) {
      licenseFileInput.value.value = '';
    }
  }
}

function formatMoney(value) {
  const amount = Number(value ?? 0);
  return amount.toFixed(2);
}

async function submitInvite() {
  if (!inviteCode.value) {
    toast.error('请输入邀请码');
    return;
  }
  try {
    activating.value = true;
    const data = await post('/portal/company/invite/activate', {
      inviteCode: inviteCode.value.trim()
    });
    Object.assign(profileForm, data);
    inviteCode.value = '';
    toast.success('邀请码验证成功，功能已激活');
    await refreshWallet();
  } catch (error) {
    toast.error(error.message ?? '邀请码验证失败，请稍后再试');
  } finally {
    activating.value = false;
  }
}
</script>

<style scoped>
.profile-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.profile-header h2 {
  margin: 6px 0;
}

.eyebrow {
  margin: 0 0 4px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #2563eb;
  font-weight: 700;
}

.eyebrow.small {
  font-size: 12px;
}

.balance {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: var(--color-primary-soft);
  border: 1px solid var(--color-primary-border);
  border-radius: 999px;
  padding: 8px 12px;
  color: var(--color-primary-strong);
}

.profile-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  align-items: start;
}

.profile-card {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid var(--border-subtle);
  border-radius: 18px;
  padding: 18px 18px 22px;
  box-shadow: var(--shadow-soft);
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.field-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-weight: 600;
}

.field span {
  font-size: 14px;
  color: var(--color-text-main);
}

.field.full {
  grid-column: 1 / -1;
}

.field input,
.field textarea {
  padding: 12px 13px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.7);
  background: #fff;
}

.field textarea {
  min-height: 120px;
}

.upload-area {
  border: 1px dashed var(--border-subtle);
  border-radius: 14px;
  padding: 12px 14px;
  background: #f8fafc;
}

.upload-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  flex-wrap: wrap;
}

.upload-input {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid rgba(148, 163, 184, 0.8);
  background: #fff;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.upload-input input {
  position: absolute;
  opacity: 0;
  width: 100%;
  height: 100%;
  inset: 0;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.aside {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.aside-card {
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid var(--border-subtle);
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: var(--shadow-soft);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.aside-card .label {
  margin: 0;
  font-weight: 700;
}

.status {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  border: 1px solid rgba(148, 163, 184, 0.7);
  background: #f1f5f9;
  color: #0f172a;
}

.status.success {
  background: var(--color-success-soft);
  border-color: rgba(22, 163, 74, 0.3);
  color: var(--color-success);
}

.status.warning {
  background: #fef3c7;
  border-color: rgba(251, 191, 36, 0.4);
  color: #b45309;
}

.activation-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.activation-row input[type='text'] {
  flex: 1;
  min-width: 220px;
  padding: 12px 13px;
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.7);
}

.tips ul {
  margin: 0;
  padding-left: 16px;
  color: #475569;
  line-height: 1.5;
}

@media (max-width: 960px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-header {
    flex-direction: column;
  }
}
</style>
