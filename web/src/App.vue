<script setup>
import { computed, onMounted } from 'vue'
import { state, load } from './store.js'
import { t, fmtDate } from './i18n/index.js'
import LangSwitch from './components/LangSwitch.vue'
import BottomNav from './components/BottomNav.vue'
import BottomSheet from './components/BottomSheet.vue'
import ToastBar from './components/ToastBar.vue'
import DayView from './views/DayView.vue'
import PlanView from './views/PlanView.vue'
import MeView from './views/MeView.vue'

onMounted(load)

const dateLine = computed(() => fmtDate(state.day))
</script>

<template>
  <div class="stage">
    <div class="device">
      <div class="screen">
        <header class="topbar">
          <div class="titles">
            <h1 class="brand">{{ t('app.name') }}</h1>
            <p class="date num">{{ dateLine || '—' }}</p>
          </div>
          <LangSwitch />
        </header>

        <main class="body">
          <DayView v-if="state.tab === 'day'" />
          <PlanView v-else-if="state.tab === 'plan'" />
          <MeView v-else />
        </main>

        <BottomNav />
        <BottomSheet />
        <ToastBar />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 舞台：桌面是一张工作台，手机是整块屏幕 */
.stage {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e9e3d6;
}

.device {
  position: relative;
  width: 100%;
  height: 100dvh;
  overflow: hidden;
  background: var(--paper);
}

/* 手机宽度：铺满全屏；桌面：390×844 的手机框 */
@media (min-width: 861px) {
  .device {
    width: 390px;
    height: min(844px, calc(100dvh - 56px));
    border: 1px solid #d8d0c0;
    border-radius: 34px;
    box-shadow:
      0 30px 80px rgba(28, 26, 23, 0.18),
      0 2px 8px rgba(28, 26, 23, 0.07);
  }
}

.screen {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--paper);
}

.topbar {
  flex: none;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 18px var(--pad) 12px;
  border-bottom: 1px solid var(--line-soft);
}

.titles {
  min-width: 0;
}

.brand {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 0.01em;
  line-height: 1.2;
}

.date {
  margin: 4px 0 0;
  font-size: 12px;
  letter-spacing: 0.04em;
  color: var(--ink-2);
}

.body {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  overscroll-behavior: contain;
  scrollbar-width: thin;
  scrollbar-color: var(--line) transparent;
}

.body::-webkit-scrollbar {
  width: 3px;
}

.body::-webkit-scrollbar-thumb {
  background: var(--line);
  border-radius: 3px;
}
</style>
