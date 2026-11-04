<script setup>
import { state, undoToast } from '../store.js'
import { t } from '../i18n/index.js'
</script>

<template>
  <!-- 一条纸：圆角 12，右边一个能撤回的链接 -->
  <div v-if="state.toast" class="toast" role="status">
    <span class="msg">{{ state.toast.text }}</span>
    <button v-if="state.toast.undo" type="button" class="undo" @click="undoToast">
      {{ t('act.undo') }}
    </button>
  </div>
</template>

<style scoped>
.toast {
  position: absolute;
  left: var(--pad);
  right: var(--pad);
  bottom: calc(var(--nav-h) + 14px);
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  min-height: 42px;
  padding: 0 14px;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: 12px;
}

.msg {
  font-size: 12.5px;
  line-height: 1.35;
  color: var(--ink);
}

.undo {
  flex: none;
  padding: 6px 0 6px 6px;
  font-size: 12.5px;
  letter-spacing: 0.04em;
  color: var(--ink);
  text-decoration: underline;
  text-underline-offset: 3px;
  text-decoration-thickness: 1px;
}

.toast {
  animation: toast-in 200ms var(--ease) both;
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .toast {
    animation: none;
  }
}
</style>
