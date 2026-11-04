<script setup>
import { computed } from 'vue'
import { state, openAdd, buildPreview, applyPreview } from '../store.js'
import { t } from '../i18n/index.js'
import WeekStrip from '../components/WeekStrip.vue'

const TIMES = ['09:00', '11:00', '14:00', '16:00']
const TYPES = ['class', 'study', 'class', 'meeting']
const PLACES = ['KB 223', 'KB 227', 'KB 426', 'LIB 3/F']

const selected = computed(() => state.week.find((d) => d.weekday === state.selectedWeekday) || null)
const isToday = computed(() => Boolean(state.day) && state.selectedWeekday === state.day.weekday)

const rows = computed(() => {
  if (isToday.value) {
    return state.day.stops.map((s) => ({
      id: s.id,
      time: s.start,
      type: s.type,
      place: s.place,
      state: s.state,
    }))
  }
  const d = selected.value
  if (!d) return []
  return (d.bars || []).map((b, i) => ({
    id: `${d.day}-${i}`,
    time: TIMES[i] || '18:00',
    type: TYPES[i] || 'study',
    place: PLACES[i] || 'LIB 3/F',
    state: 'upcoming',
  }))
})

const dayLabel = computed(() => {
  const d = selected.value
  if (!d) return ''
  return `${t('wd.' + d.weekday)} · ${d.day}`
})
</script>

<template>
  <section class="plan">
    <div class="label">
      <span>{{ t('plan.week') }}</span>
    </div>
    <WeekStrip />

    <div class="label">
      <span>{{ t('plan.thisDay') }} · {{ dayLabel }}</span>
      <span class="count num">{{ t('plan.busy', { n: rows.length }) }}</span>
    </div>

    <!-- 那一天的列 -->
    <div class="daylist">
      <div v-for="r in rows" :key="r.id" class="drow" :class="'type-' + r.type">
        <span class="dt num">{{ r.time }}</span>
        <span class="dk">{{ t('type.' + r.type) }}</span>
        <span class="dp">{{ r.place }}</span>
      </div>

      <button v-if="isToday" type="button" class="free" @click="openAdd()">
        <span class="free-main num">{{ t('plan.free', { a: '15:50', b: '17:00' }) }}</span>
        <span class="free-hint">{{ t('plan.freeHint') }}</span>
      </button>
    </div>

    <div class="row-actions">
      <button type="button" class="btn" @click="openAdd()">{{ t('plan.addManual') }}</button>
    </div>

    <!-- 说一句 → 预览 → 应用 -->
    <div class="label">
      <span>{{ t('plan.say') }}</span>
    </div>
    <input v-model="state.planText" class="say num" type="text" :placeholder="t('plan.sayPh')" @keyup.enter="buildPreview" />

    <div class="row-actions">
      <button type="button" class="btn" @click="buildPreview">{{ t('plan.preview') }}</button>
    </div>

    <div v-if="state.planPreview" class="preview">
      <p class="p-label">{{ t('plan.previewTitle') }}</p>
      <p class="p-line num">
        {{ t('plan.previewLine', { a: state.planPreview.a, b: state.planPreview.b }) }}
      </p>
      <p class="p-impact">{{ t('plan.previewImpact') }}</p>
      <button type="button" class="btn is-block apply" @click="applyPreview">{{ t('plan.apply') }}</button>
    </div>
  </section>
</template>

<style scoped>
.plan {
  padding: 14px var(--pad) 30px;
}

.label {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin: 18px 0 8px;
  font-size: 11px;
  letter-spacing: 0.08em;
  color: var(--ink-3);
}

.plan > .label:first-child {
  margin-top: 4px;
}

.count {
  letter-spacing: 0.02em;
}

.daylist {
  border-top: 1px dashed var(--line);
}

.drow {
  display: grid;
  grid-template-columns: 48px 1fr auto;
  align-items: baseline;
  gap: 8px;
  padding: 9px 2px;
  border-bottom: 1px dashed var(--line);
}

.dt {
  font-size: 11.5px;
  color: var(--ink-2);
}

.dk {
  font-size: 12.5px;
  letter-spacing: 0.03em;
}

.type-class .dk {
  color: var(--t-class);
}
.type-study .dk {
  color: var(--t-study);
}
.type-meal .dk {
  color: var(--t-meal);
}
.type-meeting .dk {
  color: var(--t-meeting);
}

.dp {
  font-size: 11.5px;
  letter-spacing: 0.02em;
  color: var(--ink-3);
}

/* 空档：虚线，一点就预填 */
.free {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
  margin-top: 12px;
  padding: 11px 12px;
  border: 1px dashed var(--line-dash);
  border-radius: var(--r-block);
  text-align: left;
}

.free-main {
  font-size: 12px;
  color: var(--ink-2);
}

.free-hint {
  font-size: 11px;
  color: var(--ink-3);
}

.row-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.row-actions .btn {
  flex: 1 1 0;
}

.say {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--r-block);
  font-size: 13px;
  letter-spacing: 0.01em;
  outline: none;
}

.say::placeholder {
  color: var(--ink-3);
}

.say:focus {
  border-color: var(--ink);
}

.preview {
  margin-top: 14px;
  padding: 14px;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--r-block);
}

.p-label {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.08em;
  color: var(--ink-3);
}

.p-line {
  margin: 8px 0 0;
  font-size: 13.5px;
  line-height: 1.5;
  color: var(--ink);
}

.p-impact {
  margin: 6px 0 0;
  font-size: 11.5px;
  letter-spacing: 0.02em;
  color: var(--ink-2);
}

.apply {
  margin-top: 14px;
}
</style>
