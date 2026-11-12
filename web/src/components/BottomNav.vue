<script setup>
import { state, setTab } from '../store.js'
import { t } from '../i18n/index.js'

const tabs = [{ id: 'day' }, { id: 'plan' }, { id: 'find' }, { id: 'me' }]
</script>

<template>
  <nav class="nav" role="tablist">
    <button
      v-for="tab in tabs"
      :key="tab.id"
      type="button"
      role="tab"
      class="tab"
      :class="{ on: state.tab === tab.id }"
      :aria-selected="state.tab === tab.id"
      @click="setTab(tab.id)"
    >
      <span class="label">{{ t('nav.' + tab.id) }}</span>
      <span class="dot" aria-hidden="true" />
    </button>
  </nav>
</template>

<style scoped>
/* 56 高，1px 上边框，与页面同色；选中只有一个朱红小点 */
.nav {
  flex: none;
  display: flex;
  height: var(--nav-h);
  border-top: 1px solid var(--line);
  background: var(--paper);
  z-index: 20;
}

.tab {
  flex: 1 1 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  color: var(--ink-2);
  font-size: 15px;
  letter-spacing: 0.02em;
  transition: color 180ms var(--ease);
}

.tab.on {
  color: var(--ink);
}

.dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: transparent;
  transition: background 180ms var(--ease);
}

.tab.on .dot {
  background: var(--red);
}
</style>
