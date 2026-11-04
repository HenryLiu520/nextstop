<script setup>
import { t } from '../i18n/index.js'

defineProps({
  minutes: { type: Number, default: 0 },
})
</script>

<template>
  <!-- 缝隙：细虚线 + 一句小字。不占块，不写"空着" -->
  <div class="gap">
    <span class="dash" aria-hidden="true" />
    <span class="label num">{{ t('day.walk', { n: minutes }) }}</span>
  </div>
</template>

<style scoped>
.gap {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 34px;
  padding-left: 1px;
}

.dash {
  width: 1px;
  height: 100%;
  margin-left: 1.5px;
  border-left: 1px dashed var(--line-dash);
}

.label {
  font-size: 11.5px;
  letter-spacing: 0.03em;
  color: var(--ink-2);
}
</style>
