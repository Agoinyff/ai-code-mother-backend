<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  disabled?: boolean
  placeholder?: string
}

interface Emits {
  (e: 'send', message: string): void
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  placeholder: '输入消息，继续优化你的网站...',
})

const emit = defineEmits<Emits>()

const inputValue = ref('')

function handleSend() {
  const message = inputValue.value.trim()
  if (message && !props.disabled) {
    emit('send', message)
    inputValue.value = ''
  }
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}
</script>

<template>
  <div class="chat-input-container">
    <div class="input-wrapper">
      <textarea
        v-model="inputValue"
        :placeholder="placeholder"
        :disabled="disabled"
        class="chat-textarea"
        rows="1"
        @keydown="handleKeydown"
      ></textarea>
      <button 
        class="send-btn" 
        :disabled="disabled || !inputValue.trim()"
        @click="handleSend"
      >
        <span class="send-icon">➤</span>
      </button>
    </div>
    <p class="input-hint">按 Enter 发送，Shift + Enter 换行</p>
  </div>
</template>

<style scoped>
.chat-input-container {
  padding: var(--spacing-md);
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: var(--spacing-sm);
  background: var(--bg-glass);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--spacing-sm);
  transition: all var(--transition-fast);
}

.input-wrapper:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
}

.chat-textarea {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-primary);
  font-size: 0.9375rem;
  padding: var(--spacing-sm);
  resize: none;
  min-height: 24px;
  max-height: 120px;
  line-height: 1.5;
}

.chat-textarea::placeholder {
  color: var(--text-muted);
}

.chat-textarea:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-btn {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  background: var(--gradient-primary);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: var(--shadow-glow);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.send-icon {
  color: white;
  font-size: 1rem;
}

.input-hint {
  margin-top: var(--spacing-xs);
  font-size: 0.75rem;
  color: var(--text-muted);
  text-align: center;
}
</style>
