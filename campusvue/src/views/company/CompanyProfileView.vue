<template>
  <section class="card">
    <div class="card__title">
      <div>
        <h2>企业资料</h2>
        <p class="muted">当前钱包余额：￥{{ formatMoney(currentBalance) }}</p>
      </div>
      <button class="outline" @click="refreshWallet">刷新余额</button>
    </div>

    <form class="form-grid" @submit.prevent="saveProfile">
      <label>企业名称<input v-model="profileForm.companyName" required maxlength="100" /></label>
      <label>营业执照号<input v-model="profileForm.licenseNumber" maxlength="100" /></label>
      <label>行业类别<input v-model="profileForm.industry" maxlength="100" /></label>
      <label>企业地址<input v-model="profileForm.address" maxlength="255" /></label>
      <label>企业官网<input v-model="profileForm.website" maxlength="255" /></label>
      <label class="full">企业简介<textarea v-model="profileForm.description" maxlength="255"></textarea></label>
      <label class="full">Logo 链接<input v-model="profileForm.logo" maxlength="255" /></label>
      <label class="full file-input">
        营业执照文件
        <div class="file-row">
          <input ref="licenseFileInput" type="file" accept=".pdf,.jpg,.jpeg,.png" @change="handleLicenseFile" />
          <button class="outline" type="button" @click="uploadLicense" :disabled="!licenseFile">上传</button>
        </div>
        <small v-if="profileForm.licenseDocument">
          当前文件：
          <a :href="profileForm.licenseDocument" target="_blank" rel="noopener">点击查看</a>
        </small>
      </label>
      <div class="full actions">
        <button class="primary" type="submit">保存资料</button>
        <button class="outline" type="button" @click="loadProfile">重新加载</button>
      </div>
    </form>

    <p v-if="feedback.message" :class="['feedback', feedback.type]">{{ feedback.message }}</p>
  </section>
</template>

<script setup>
import { computed, inject, onMounted, reactive, ref } from 'vue';
import { get, put, upload } from '../../api/http';

const walletSummary = inject('walletSummary', ref(null));
const refreshWallet = inject('refreshWallet', () => Promise.resolve());

const profileForm = reactive({
  companyName: '',
  licenseNumber: '',
  industry: '',
  address: '',
  website: '',
  description: '',
  logo: '',
  licenseDocument: '',
  walletBalance: 0
});

const feedback = reactive({ message: '', type: 'info' });
const licenseFile = ref(null);
const licenseFileInput = ref(null);

const currentBalance = computed(() => {
  if (walletSummary?.value?.balance != null) {
    return walletSummary.value.balance;
  }
  return profileForm.walletBalance ?? 0;
});

onMounted(() => {
  loadProfile();
});

async function loadProfile() {
  try {
    const data = await get('/portal/company/profile');
    Object.assign(profileForm, data);
    await refreshWallet();
  } catch (error) {
    if (error.status !== 404) {
      showFeedback(error.message ?? '加载企业资料失败', 'error');
    }
  }
}

async function saveProfile() {
  try {
    const payload = { ...profileForm };
    delete payload.walletBalance;
    const data = await put('/portal/company/profile', payload);
    Object.assign(profileForm, data);
    showFeedback('企业资料已保存', 'success');
    await refreshWallet();
  } catch (error) {
    showFeedback(error.message ?? '保存失败，请稍后再试', 'error');
  }
}

function handleLicenseFile(event) {
  const [file] = event.target.files ?? [];
  licenseFile.value = file ?? null;
}

async function uploadLicense() {
  if (!licenseFile.value) {
    showFeedback('请先选择要上传的文件', 'error');
    return;
  }
  try {
    const formData = new FormData();
    formData.append('file', licenseFile.value);
    const data = await upload('/portal/company/profile/license', formData);
    Object.assign(profileForm, data);
    showFeedback('营业执照上传成功，等待管理员审核', 'success');
    await refreshWallet();
  } catch (error) {
    showFeedback(error.message ?? '上传失败，请稍后再试', 'error');
  } finally {
    licenseFile.value = null;
    if (licenseFileInput.value) {
      licenseFileInput.value.value = '';
    }
  }
}

function showFeedback(message, type = 'info') {
  feedback.message = message;
  feedback.type = type;
  if (message) {
    setTimeout(() => {
      feedback.message = '';
    }, 4000);
  }
}

function formatMoney(value) {
  const amount = Number(value ?? 0);
  return amount.toFixed(2);
}
</script>

<style scoped>
.file-input .file-row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 6px;
}

.file-input input[type='file'] {
  flex: 1;
  padding: 8px;
  border-radius: 10px;
  border: 1px solid #d1d5db;
}

.file-input small {
  display: block;
  margin-top: 6px;
  color: #64748b;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feedback {
  padding: 10px 14px;
  border-radius: 12px;
  text-align: center;
}

.feedback.success {
  background: #dcfce7;
  color: #15803d;
}

.feedback.error {
  background: #fee2e2;
  color: #b91c1c;
}
</style>
