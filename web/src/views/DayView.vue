<script setup>
import { computed } from 'vue'
import { state, openBlock, toast } from '../store.js'
import { t } from '../i18n/index.js'
import DayBlock from '../components/DayBlock.vue'
import GapRow from '../components/GapRow.vue'

const stops = computed(() => (state.day ? state.day.stops : []))
const left = computed(() => stops.value.filter((s) => s.state === 'upcoming').length)

function endDay() {
  toast(t('toast.closed'), { undo: true })
}

function extendDay() {
  toast(t('toast.extended'), { undo: true })
}
</script>

<template>
  <section class="day">
    <!-- 一天 = 块 + 缝隙，竖着堆 -->
    <div class="stack">
      <template v-for="stop in stops" :key="stop.id">
        <GapRow v-if="stop.walkBefore > 0" :minutes="stop.walkBefore" />
        <DayBlock :stop="stop" @open="openBlock" />
      </template>
    </div>

    <!-- 一天剩下的部分：一条虚线，把时间往下带 -->
    <div class="runout" aria-hidden="true"><span class="spine" /></div>

    <p class="left num">{{ t('day.left', { n: left }) }}</p>

    <div class="tail">
      <button type="button" class="btn" @click="endDay">{{ t('day.end') }}</button>
      <button type="button" class="btn" @click="extendDay">{{ t('day.extend') }}</button>
    </div>
  </section>
</template>

<style scoped>
.day {
  display: flex;
  flex: 1 0 auto;
  flex-direction: column;
  min-height: 100%;
  padding: 14px var(--pad) 22px;
}

.stack {
  display: flex;
  flex-direction: column;
}

.runout {
  flex: 1 1 auto;
  min-height: 26px;
  padding: 6px 0;
}

.spine {
  display: block;
  width: 1px;
  height: 100%;
  margin-left: 1.5px;
  border-left: 1px dashed var(--line-dash);
}

.left {
  margin: 0 0 10px;
  text-align: center;
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--ink-3);
}

.tail {
  display: flex;
  gap: 10px;
}

.tail .btn {
  flex: 1 1 0;
}
</style>
