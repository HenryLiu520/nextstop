<script setup>
/* 底栏：深绿一条，四个页签，第三个是查找。图标手写 SVG，不引图标库。 */
import { state, setTab } from '../store.js'
import { t } from '../i18n/index.js'

const tabs = [
  { id: 'day', icon: 'day' },
  { id: 'plan', icon: 'plan' },
  { id: 'find', icon: 'find' },
  { id: 'me', icon: 'me' },
]
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
      <svg class="ico" viewBox="0 0 24 24" aria-hidden="true">
        <template v-if="tab.icon === 'day'">
          <rect x="4" y="5" width="16" height="15" rx="2" />
          <path d="M4 10h16M9 3v4M15 3v4" />
        </template>
        <template v-else-if="tab.icon === 'plan'">
          <rect x="4" y="4" width="7" height="7" rx="1" />
          <rect x="13" y="4" width="7" height="7" rx="1" />
          <rect x="4" y="13" width="7" height="7" rx="1" />
          <rect x="13" y="13" width="7" height="7" rx="1" />
        </template>
        <template v-else-if="tab.icon === 'find'">
          <circle cx="11" cy="11" r="6" />
          <path d="M16 16l4 4" />
        </template>
        <template v-else>
          <circle cx="12" cy="8" r="3.4" />
          <path d="M5 20c0-3.6 3.1-6 7-6s7 2.4 7 6" />
        </template>
      </svg>
      <span class="label">{{ t('nav.' + tab.id) }}</span>
    </button>
  </nav>
</template>

<style scoped>
.nav {
  flex: none;
  display: flex;
  height: 62px;
  background: #00382c;
  z-index: 20;
}

.tab {
  flex: 1 1 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: none;
  background: transparent;
  color: rgba(247, 244, 236, 0.62);
  cursor: pointer;
  transition: color 160ms ease;
}

.tab.on {
  color: #f7f4ec;
}

.ico {
  width: 20px;
  height: 20px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.5;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.label {
  font-size: 9px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}
</style>
