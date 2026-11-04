<script setup>
import { state, selectWeekday } from '../store.js'
import { t } from '../i18n/index.js'

function barsOf(day) {
  return day.bars || []
}
</script>

<template>
  <!-- 一周五列：每列几根小横杠 = 那天多满；选中的那一列带白底描边 -->
  <div class="week" role="tablist">
    <button
      v-for="d in state.week"
      :key="d.day"
      type="button"
      role="tab"
      class="col"
      :class="{ on: state.selectedWeekday === d.weekday }"
      :aria-selected="state.selectedWeekday === d.weekday"
      @click="selectWeekday(d.weekday)"
    >
      <span class="dow">{{ t('wdShort.' + d.weekday) }}</span>
      <span class="bars">
        <i v-for="(b, i) in barsOf(d)" :key="i" :style="{ width: Math.round(b * 20) + 'px' }" />
      </span>
      <span class="date num">{{ d.day }}</span>
    </button>
  </div>
</template>

<style scoped>
.week {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 6px;
}

.col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  padding: 9px 0 8px;
  border: 1px solid transparent;
  border-radius: var(--r-block);
  background: transparent;
  transition: background 160ms var(--ease), border-color 160ms var(--ease);
}

.col.on {
  background: var(--card);
  border-color: var(--line);
}

.dow {
  font-size: 11.5px;
  letter-spacing: 0.04em;
  color: var(--ink-2);
}

.col.on .dow {
  color: var(--ink);
}

.bars {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-height: 22px;
}

.bars i {
  display: block;
  height: 2.5px;
  border-radius: 2px;
  background: var(--line-dash);
}

.col.on .bars i {
  background: var(--ink-3);
}

.date {
  font-size: 10.5px;
  letter-spacing: 0.02em;
  color: var(--ink-3);
}
</style>
