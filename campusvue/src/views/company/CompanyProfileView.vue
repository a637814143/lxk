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
          <input
            ref="licenseFileInput"
            type="file"
            accept=".pdf,.jpg,.jpeg,.png"
            @change="handleLicenseFile"
          />
          <button class="outline" type="button" @click="uploadLicense" :disabled="!licenseFile">上传</button>
        </div>
        <small v-if="profileForm.licenseDocument">
          当前文件：<a :href="profileForm.licenseDocument" target="_blank" rel="noopener">点击查看</a>
        </small>
      </label>
      <div class="full actions">
        <button class="primary" type="submit">保存资料</button>
        <button class="outline" type="button" @click="loadProfile">重新加载</button>
      </div>
    </form>

    <div class="full activation">
      <h3>平台功能激活</h3>
      <p class="muted">
        当前状态：<strong>{{ profileForm.inviteActivated ? '已激活' : '未激活' }}</strong>
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
        管理员审核通过后，会向您发送企业邀请码。验证成功后，即可使用发布职位等平台功能。
      </small>
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
    toast.success('邀请码验证成功，平台功能已激活');
    await refreshWallet();
  } catch (error) {
    toast.error(error.message ?? '邀请码验证失败，请稍后再试');
  } finally {
    activating.value = false;
  }
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
.activation {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px dashed #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.activation-row {
  display: flex;
  gap: 12px;
  align-items: center;
}
.activation-row input[type='text'] {
  flex: 1;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #d1d5db;
}
</style>
