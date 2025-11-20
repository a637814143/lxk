<template>
  <div class="comment-node" :style="{ marginLeft: `${level * 16}px` }">
    <p class="comment-meta">
      {{ comment.authorUsername || '用户' }} · {{ formatDate(comment.createdAt) }}
    </p>
    <p>{{ comment.sanitizedContent || comment.content }}</p>
    <button class="comment-reply" type="button" @click="$emit('reply', comment)">
      回复
    </button>
    <ul v-if="comment.replies && comment.replies.length" class="comments__list">
      <li v-for="child in comment.replies" :key="child.id" class="comments__item">
        <CommentNode
          :comment="child"
          :level="level + 1"
          :format-date="formatDate"
          @reply="$emit('reply', $event)"
        />
      </li>
    </ul>
  </div>
</template>

<script setup>
import CommentNode from './StudentDiscussionsViewNode.vue';

defineProps({
  comment: {
    type: Object,
    required: true
  },
  level: {
    type: Number,
    default: 0
  },
  formatDate: {
    type: Function,
    required: true
  }
});
</script>

<style scoped>
.comment-node {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-meta {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
}

.comment-reply {
  margin-top: 4px;
  padding: 0;
  border: none;
  background: transparent;
  color: #2563eb;
  font-size: 12px;
  cursor: pointer;
  align-self: flex-start;
}

.comments__list {
  list-style: none;
  margin: 6px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.comments__item {
  padding: 6px 8px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background: #fff;
}
</style>

