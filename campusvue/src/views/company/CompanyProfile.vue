<template>
  <section class="card">
    <h2>企业资料</h2>
    <form class="form-grid" @submit.prevent="saveProfile">
      <label>企业名称<input v-model="profileForm.companyName" required /></label>
      <label>营业执照号<input v-model="profileForm.licenseNumber" /></label>
      <label>行业类别<input v-model="profileForm.industry" /></label>
      <label>企业地址<input v-model="profileForm.address" /></label>
      <label>企业官网<input v-model="profileForm.website" /></label>
      <label class="full">企业简介<textarea v-model="profileForm.description"></textarea></label>
      <label class="full">Logo 链接<input v-model="profileForm.logo" /></label>
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
      </div>
    </form>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const company = inject('companyContext');
if (!company) {
  throw new Error('companyContext not provided');
}

const { profileForm, saveProfile, handleLicenseFile, uploadLicense, licenseFile, licenseFileInput } = company;
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
</style>
