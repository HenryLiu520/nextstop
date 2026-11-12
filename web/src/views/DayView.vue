<script setup>
/* 一天：焦点卡 + 余下几站。版式对齐原型：
   320px 照片卡（NOW 标 + 房号 + 时间 + 剩余 + 换一个），下面一条带色条的列表。 */
import { computed } from 'vue'
import { state, openBlock } from '../store.js'
import { t } from '../i18n/index.js'
import photo from '../assets/loke-yew.jpg'

const stops = computed(() => (state.day ? state.day.stops : []))
const now = computed(() => (state.day && state.day.now ? state.day.now : '11:24'))

function mins(hhmm) {
  const [h, m] = hhmm.split(':').map(Number)
  return h * 60 + m
}

const focus = computed(() => {
  const list = stops.value
  const n = mins(now.value)
  const live = list.find((s) => mins(s.start) <= n && n < mins(s.end))
  if (live && live.type === 'study') return live
  return list.find((s) => s.type === 'study' && mins(s.start) > n) || null
})

const rest = computed(() => stops.value.filter((s) => !focus.value || s.id !== focus.value.id))

const isNow = computed(() => {
  if (!focus.value) return false
  const n = mins(now.value)
  return mins(focus.value.start) <= n && n < mins(focus.value.end)
})

const leftMin = computed(() => (focus.value ? Math.max(0, mins(focus.value.end) - mins(now.value)) : 0))
const inMin = computed(() => (focus.value ? Math.max(0, mins(focus.value.start) - mins(now.value)) : 0))

function gapBefore(i) {
  const prev = rest.value[i - 1]
  const cur = rest.value[i]
  if (!prev) return 0
  return Math.max(0, mins(cur.start) - mins(prev.end))
}

const barClass = (s) => (s.type === 'study' ? 'study' : s.type === 'class' ? 'class' : 'other')
const typeName = (s) => t('type.' + s.type)
</script>

<template>
  <div class="day">
    <section v-if="focus" class="hero" :style="{ backgroundImage: 'url(' + photo + ')' }" @click="openBlock(focus)">
      <div class="veil" />
      <span class="pill" :class="{ now: isNow }">{{ isNow ? t('badge.now') : t('badge.next') }}</span>
      <button class="hdots" type="button" :aria-label="t('act.more')" @click.stop="openBlock(focus)">⋯</button>

      <div class="heroText">
        <h2 class="room">{{ focus.place }}</h2>
        <p class="where">{{ focus.title || t('type.study') }}</p>
        <p class="time">{{ focus.start }}–{{ focus.end }}</p>
        <p class="left">{{ isNow ? t('hero.left').replace('{n}', leftMin) : t('hero.starts').replace('{n}', inMin) }}</p>
      </div>

      <button class="change" type="button" @click.stop="openBlock(focus)">{{ t('act.change') }}</button>
    </section>

    <p class="todayline">{{ t('day.today') }}</p>

    <ul class="list">
      <li v-for="(s, i) in rest" :key="s.id">
        <div v-if="i > 0 && gapBefore(i) > 0 && gapBefore(i) <= 25" class="walk">
          <span>{{ t('day.walk').replace('{n}', gapBefore(i)) }}</span>
        </div>
        <div class="blk" @click="openBlock(s)">
          <span class="bar" :class="barClass(s)" />
          <div class="bl">
            <span class="bt">{{ typeName(s) }}</span>
            <span class="bn">{{ s.place }}</span>
          </div>
          <div class="br">{{ s.start }}–{{ s.end }}</div>
          <button class="bdots" type="button" :aria-label="t('act.more')" @click.stop="openBlock(s)">⋯</button>
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.day {
  display: flex;
  flex-direction: column;
  padding: 10px 16px 4px;
}

.hero {
  position: relative;
  height: 320px;
  border-radius: 14px;
  overflow: hidden;
  background-size: cover;
  background-position: center 38%;
  background-color: var(--deep);
  cursor: pointer;
}

.veil {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 56, 44, 0.04) 0%, rgba(0, 56, 44, 0.38) 58%, rgba(0, 56, 44, 0.62) 100%);
}

.pill {
  position: absolute;
  top: 12px;
  left: 14px;
  padding: 3px 7px;
  border: 1px solid rgba(255, 255, 255, 0.85);
  border-radius: 3px;
  color: #fff;
  font-size: 9px;
  letter-spacing: 0.14em;
}

.pill.now {
  background: #00664f;
  border-color: #00664f;
  color: #e3c68c;
  font-weight: 600;
}

.hdots {
  position: absolute;
  top: 6px;
  right: 8px;
  width: 30px;
  height: 30px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.9);
  font-size: 18px;
  cursor: pointer;
}

.heroText {
  position: absolute;
  left: 16px;
  bottom: 16px;
  right: 118px;
  color: #fff;
}

.room {
  margin: 0;
  font-family: var(--serif);
  font-size: 28px;
  font-weight: 400;
}

.where {
  margin: 6px 0 0;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.82);
}

.time {
  margin: 8px 0 0;
  font-size: 13px;
}

.left {
  margin: 4px 0 0;
  font-size: 9px;
  letter-spacing: 0.06em;
  color: rgba(255, 255, 255, 0.72);
}

.change {
  position: absolute;
  right: 14px;
  bottom: 16px;
  height: 32px;
  padding: 0 14px;
  border: 1px solid rgba(255, 255, 255, 0.7);
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 9px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  cursor: pointer;
}

.todayline {
  margin: 14px 2px 8px;
  font-size: 9px;
  letter-spacing: 0.14em;
  color: var(--ink-3);
}

.list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.walk {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 3px 0 3px 12px;
}

.walk span {
  font-size: 9px;
  letter-spacing: 0.1em;
  color: var(--ink-3);
  text-transform: uppercase;
}

.walk::after {
  content: '';
  flex: 1;
  height: 1px;
  background: repeating-linear-gradient(90deg, #d8dfd9 0 3px, transparent 3px 7px);
}

.blk {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 58px;
  padding: 9px 40px 9px 16px;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: var(--card);
  overflow: hidden;
  cursor: pointer;
}

.bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
}

.bar.study { background: var(--green); }
.bar.class { background: #3f6e7a; }
.bar.other { background: #b08968; }

.bl {
  display: flex;
  align-items: baseline;
  gap: 10px;
  min-width: 0;
}

.bt {
  font-size: 9px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--ink-2);
}

.bn {
  font-family: var(--serif);
  font-size: 17px;
  color: var(--ink);
}

.br {
  margin-left: auto;
  font-size: 11px;
  color: var(--ink-2);
}

.bdots {
  position: absolute;
  right: 4px;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--ink-3);
  font-size: 16px;
  cursor: pointer;
}
</style>
