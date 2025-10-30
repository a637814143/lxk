<template>
  <section class="card">
    <h2>基本资料</h2>
    <form class="form-grid" @submit.prevent="saveProfile">
      <label>姓名<input v-model="profileForm.name" required /></label>
      <label>性别<input v-model="profileForm.gender" placeholder="男/女/其他" /></label>
      <label>学校<input v-model="profileForm.school" /></label>
      <label>专业<input v-model="profileForm.major" /></label>
      <label>年级<input v-model="profileForm.grade" /></label>
      <label>学历<input v-model="profileForm.education" /></label>
      <label class="full">头像地址<input v-model="profileForm.avatar" /></label>
      <div class="full actions">
        <button class="primary" type="submit">保存资料</button>
        <span v-if="profileSaved" class="hint">资料已更新</span>
      </div>
    </form>
  </section>
</template>

<script setup>
import { inject } from 'vue';

const student = inject('studentContext');
if (!student) {
  throw new Error('studentContext not provided');
}

const { profileForm, saveProfile, profileSaved } = student;
</script>

<style scoped>
.hint {
  color: #16a34a;
}
</style>
