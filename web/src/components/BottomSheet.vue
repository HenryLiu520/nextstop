<script setup>
import { computed, ref, watch } from 'vue'
import { state, closeSheet, applySwap, openSwap, addStop, toast, openReview } from '../store.js'
import { t } from '../i18n/index.js'

/* 动作矩阵：四个动作封顶，按块类型给。
   课只有「不去」。自习/吃饭只有「换一个」。见面才有「一键预约」。
   没有「我到了」，也没有「我晚了」——这两件事不再由按钮承担。 */
const ACTIONS = {
  class: ['review'],
  study: ['swap', 'review'],
  meal: ['swap'],
  meeting: ['book', 'swap'],
}

const name = ref('')
const start = ref('19:00')
const end = ref('20:00')

const sheet = computed(() => state.sheet)
const kind = computed(() => (sheet.value ? sheet.value.kind : null))
const stop = computed(() => (sheet.value && sheet.value.stop) || null)

watch(kind, (k) => {
  if (k === 'add') {
    name.value = ''
    start.value = '19:00'
    end.value = '20:00'
  }
})

function actionKey(id) {
  return 'act.' + id
}

function run(id) {
  if (!stop.value) return
  if (id === 'swap') {
    openSwap(stop.value)
    return
  }
  if (id === 'review') {
    openReview(stop.value.place)
    return
  }
  if (id === 'skip') {
    const s = state.day.stops.find((x) => x.id === stop.value.id)
    if (s) s.pending = true
    closeSheet()
    toast(t('toast.skip'), { undo: true })
    return
  }
  if (id === 'book') {
    const s = state.day.stops.find((x) => x.id === stop.value.id)
    if (s) {
      s.booked = true
      s.pending = false
    }
    closeSheet()
    toast(t('toast.booked'), { undo: true })
    return
  }
  closeSheet()
  toast(t('toast.changed'), { undo: true })
}

function submitAdd() {
  addStop(name.value.trim(), start.value, end.value)
}
</script>

<template>
  <div v-if="sheet" class="scrim" @click.self="closeSheet">
    <div class="panel" role="dialog" aria-modal="true">
      <span class="grab" aria-hidden="true" />

      <!-- 一块能做什么 -->
      <template v-if="kind === 'block' && stop">
        <p class="kicker num">{{ stop.start }}–{{ stop.end }}</p>
        <h2 class="title">{{ t('sheet.block', { type: t('type.' + stop.type), place: stop.place }) }}</h2>
        <p class="sub">{{ t('sheet.what') }}</p>

        <div class="actions">
          <button
            v-for="id in ACTIONS[stop.type] || []"
            :key="id"
            type="button"
            class="btn"
            :class="{ 'is-state': id === 'book' && stop.booked }"
            :disabled="id === 'book' && stop.booked"
            @click="run(id)"
          >
            {{ id === 'book' && stop.booked ? t('act.booked') : t(actionKey(id)) }}
          </button>
        </div>

        <button type="button" class="btn is-block close" @click="closeSheet">{{ t('act.close') }}</button>
      </template>

      <!-- 换一个：三个候选 -->
      <template v-else-if="kind === 'swap'">
        <h2 class="title">{{ t('sheet.swap.title') }}</h2>
        <p class="sub">{{ t('sheet.swap.sub') }}</p>

        <ul v-if="sheet.candidates && sheet.candidates.length" class="cands">
          <li v-for="c in sheet.candidates" :key="c.id">
            <button type="button" class="cand" @click="applySwap(c)">
              <span class="cand-name">{{ c.place }}</span>
              <span class="cand-leave num">{{ t('sheet.swap.leaveBy', { t: c.leaveBy }) }}</span>
            </button>
          </li>
        </ul>
        <p v-else class="sub empty">{{ t('sheet.swap.empty') }}</p>

        <button type="button" class="btn is-block more" @click="openSwap(stop)">{{ t('sheet.swap.more') }}</button>
        <button type="button" class="btn is-block close" @click="closeSheet">{{ t('act.close') }}</button>
      </template>

      <!-- 手动添加一站 -->
      <template v-else-if="kind === 'add'">
        <h2 class="title">{{ t('sheet.add.title') }}</h2>

        <label class="field">
          <span class="field-label">{{ t('sheet.add.name') }}</span>
          <input v-model="name" class="input" type="text" :placeholder="t('sheet.add.namePh')" />
        </label>

        <div class="two">
          <label class="field">
            <span class="field-label">{{ t('sheet.add.start') }}</span>
            <input v-model="start" class="input num" type="text" inputmode="numeric" />
          </label>
          <label class="field">
            <span class="field-label">{{ t('sheet.add.end') }}</span>
            <input v-model="end" class="input num" type="text" inputmode="numeric" />
          </label>
        </div>

        <button type="button" class="btn is-block add" @click="submitAdd">{{ t('act.add') }}</button>
        <button type="button" class="btn is-block close" @click="closeSheet">{{ t('act.cancel') }}</button>
      </template>
    </div>
  </div>
</template>

<style scoped>
.scrim {
  position: absolute;
  inset: 0;
  z-index: 40;
  display: flex;
  align-items: flex-end;
  background: rgba(28, 26, 23, 0.3);
}

/* 一张从底下升起的纸：顶部圆角 18，无阴影 */
.panel {
  width: 100%;
  max-height: 82%;
  overflow-y: auto;
  padding: 9px 20px calc(18px + env(safe-area-inset-bottom));
  background: var(--paper);
  border: 1px solid var(--line);
  border-bottom: 0;
  border-radius: var(--r-sheet) var(--r-sheet) 0 0;
}

.grab {
  display: block;
  width: 36px;
  height: 3px;
  margin: 0 auto 14px;
  border-radius: 2px;
  background: var(--line);
}

.kicker {
  margin: 0 0 4px;
  font-size: 11.5px;
  letter-spacing: 0.04em;
  color: var(--ink-3);
}

.title {
  margin: 0;
  font-size: 17px;
  font-weight: 400;
  letter-spacing: 0.01em;
  line-height: 1.35;
}

.sub {
  margin: 6px 0 0;
  font-size: 12px;
  letter-spacing: 0.03em;
  color: var(--ink-2);
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.actions .btn {
  flex: 1 1 0;
  min-width: 88px;
}

.close {
  margin-top: 8px;
}

.cands {
  list-style: none;
  margin: 14px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cand {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  padding: 12px 14px;
  text-align: left;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--r-block);
  transition: border-color 160ms var(--ease);
}

.cand:active {
  border-color: var(--ink);
}

.cand-name {
  font-size: 13.5px;
}

.cand-leave {
  flex: none;
  font-size: 11px;
  letter-spacing: 0.02em;
  color: var(--ink-3);
}

.empty {
  margin-top: 14px;
}

.more {
  margin-top: 14px;
}

.field {
  display: block;
  margin-top: 14px;
}

.field-label {
  display: block;
  margin-bottom: 6px;
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--ink-3);
}

.input {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--r-block);
  font-size: 13px;
  outline: none;
}

.input:focus {
  border-color: var(--ink);
}

.two {
  display: flex;
  gap: 10px;
}

.two .field {
  flex: 1 1 0;
}

.add {
  margin-top: 18px;
}

/* 升起：纯 CSS 动画，不依赖 JS 的帧回调 */
.scrim {
  animation: scrim-in 180ms var(--ease) both;
}

.panel {
  animation: panel-rise 240ms var(--ease) both;
}

@keyframes scrim-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes panel-rise {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .scrim,
  .panel {
    animation: none;
  }
}
</style>
