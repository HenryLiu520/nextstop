<script setup>
import { computed } from 'vue'
import { t } from '../i18n/index.js'

const props = defineProps({
  stop: { type: Object, required: true },
})
defineEmits(['open'])

const typeClass = computed(() => 'type-' + props.stop.type)
</script>

<template>
  <button
    type="button"
    class="block"
    :class="[typeClass, 'is-' + stop.state, { 'is-pending': stop.pending }]"
    @click="$emit('open', stop)"
  >
    <span class="bar" aria-hidden="true" />

    <span class="row">
      <span class="time num">{{ stop.start }}–{{ stop.end }}</span>
      <span v-if="stop.state === 'current'" class="here-dot" aria-hidden="true" />
    </span>

    <span class="meta">
      <span class="kind">{{ t('type.' + stop.type) }}</span>
      <span v-if="stop.title" class="title">{{ stop.title }}</span>
      <span v-if="stop.title" class="sep" aria-hidden="true">·</span>
      <span class="place">{{ stop.place }}</span>
    </span>

    <span v-if="stop.state === 'current'" class="note">{{ t('day.here') }}</span>
  </button>
</template>

<style scoped>
.block {
  position: relative;
  display: block;
  width: 100%;
  padding: 13px 14px 14px 17px;
  text-align: left;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--r-block);
  overflow: hidden;
  transition: border-color 160ms var(--ease);
}

.block:active {
  border-color: var(--ink);
}

/* 左侧 4pt 竖条：颜色 = 类型 */
.bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: var(--bar-w);
  background: var(--ink);
}

.type-class .bar {
  background: var(--t-class);
}
.type-study .bar {
  background: var(--t-study);
}
.type-meal .bar {
  background: var(--t-meal);
}
.type-meeting .bar {
  background: var(--t-meeting);
}

.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.time {
  font-size: 14px;
  letter-spacing: 0.02em;
  color: var(--ink);
}

.meta {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-top: 3px;
}

.kind {
  font-size: 12px;
  letter-spacing: 0.04em;
}

.type-class .kind {
  color: var(--t-class);
}
.type-study .kind {
  color: var(--t-study);
}
.type-meal .kind {
  color: var(--t-meal);
}
.type-meeting .kind {
  color: var(--t-meeting);
}

.place {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: var(--ink-2);
}

.title {
  font-size: 12px;
  letter-spacing: 0.02em;
  color: var(--ink);
}

.sep {
  font-size: 12px;
  color: var(--ink-3);
}

/* 当前块：放大，带一个朱红点，其余什么都不加 */
.is-current {
  padding: 15px 16px 16px 19px;
  border-color: var(--line);
}

.is-current .time {
  font-size: 26px;
  font-weight: 400;
  letter-spacing: 0.01em;
  line-height: 1.1;
}

.is-current .meta {
  margin-top: 6px;
}

.is-current .kind,
.is-current .title,
.is-current .place {
  font-size: 13px;
}

.is-current .place {
  color: var(--ink);
}

.here-dot {
  flex: none;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--red);
}

.note {
  display: block;
  margin-top: 7px;
  font-size: 12px;
  letter-spacing: 0.04em;
  color: var(--ink-2);
}

/* 已过的块：压成细条，淡，划掉 */
.is-past {
  padding: 8px 12px 9px 16px;
  opacity: 0.34;
}

.is-past .time {
  font-size: 12.5px;
  text-decoration: line-through;
  text-decoration-thickness: 1px;
}

.is-past .meta {
  margin-top: 2px;
}

.is-past .kind,
.is-past .title,
.is-past .place {
  font-size: 11.5px;
  text-decoration: line-through;
}

/* 待订：点线 */
.is-pending {
  border-style: dotted;
  border-color: var(--line-dash);
}
</style>
