<script setup>
import { state, persistPrefs } from '../store.js'
import { t } from '../i18n/index.js'
</script>

<template>
  <section class="me">
    <div class="label">{{ t('me.prefs') }}</div>

    <div class="card">
      <div class="prow">
        <div class="plabel">
          <span class="pname">{{ t('me.walk') }}</span>
          <span class="phint">{{ t('me.walkHint') }}</span>
        </div>
        <div class="pval">
          <input v-model="state.prefs.walkSpeed" class="num-input num" type="number" step="0.05" min="0.5" max="2" />
          <span class="unit">{{ t('me.walkUnit') }}</span>
        </div>
      </div>

      <div class="prow">
        <div class="plabel">
          <span class="pname">{{ t('me.thu') }}</span>
          <span class="phint">{{ t('me.thuHint', { t: '20:00' }) }}</span>
        </div>
        <div class="pval">
          <input v-model="state.prefs.weekdayEnd" class="num-input num wide" type="text" inputmode="numeric" />
        </div>
      </div>
    </div>

    <div class="row-actions">
      <button type="button" class="btn" @click="persistPrefs">{{ t('me.save') }}</button>
    </div>

    <div class="label">{{ t('me.learn') }}</div>

    <div class="card">
      <div class="lrow">
        <span class="lname">{{ t('me.learnWalk') }}</span>
        <span class="lvalue num">×1.28</span>
      </div>
      <div class="lrow">
        <span class="lname">{{ t('me.learnSpot') }}</span>
        <span class="lvalue">{{ t('me.learnSpotValue') }}</span>
      </div>
      <div class="lrow">
        <span class="lname">{{ t('me.learnAttend') }}</span>
        <span class="lvalue">{{ t('me.learnAttendValue') }}</span>
      </div>
    </div>

    <div class="label">{{ t('me.about') }}</div>

    <div class="card">
      <div class="lrow">
        <span class="lname">{{ t('me.version') }}</span>
        <span class="lvalue num">0.1.0</span>
      </div>
      <div class="lrow">
        <span class="lname">{{ t('me.group') }}</span>
        <span class="lvalue">COMP1110 · G08</span>
      </div>
    </div>

    <p class="foot">{{ t('me.localNote') }}</p>
  </section>
</template>

<style scoped>
.me {
  padding: 14px var(--pad) 30px;
}

.label {
  margin: 18px 0 8px;
  font-size: 11px;
  letter-spacing: 0.08em;
  color: var(--ink-3);
}

.me > .label:first-child {
  margin-top: 4px;
}

.card {
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--r-block);
  padding: 0 14px;
}

.prow {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 13px 0;
  border-bottom: 1px solid var(--line-soft);
}

.prow:last-child {
  border-bottom: 0;
}

.plabel {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.pname {
  font-size: 13px;
  letter-spacing: 0.02em;
}

.phint {
  font-size: 11px;
  line-height: 1.35;
  color: var(--ink-3);
}

.pval {
  flex: none;
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.num-input {
  width: 52px;
  padding: 3px 0 2px;
  text-align: right;
  background: transparent;
  border: 0;
  border-bottom: 1px dashed var(--line-dash);
  font-size: 15px;
  letter-spacing: 0.01em;
  color: var(--ink);
  outline: none;
  -moz-appearance: textfield;
}

.num-input::-webkit-outer-spin-button,
.num-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.num-input:focus {
  border-bottom-color: var(--ink);
}

.num-input.wide {
  width: 62px;
}

.unit {
  font-size: 11px;
  letter-spacing: 0.02em;
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

.lrow {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 11px 0;
  border-bottom: 1px solid var(--line-soft);
}

.lrow:last-child {
  border-bottom: 0;
}

.lname {
  font-size: 12.5px;
  letter-spacing: 0.02em;
  color: var(--ink-2);
}

.lvalue {
  font-size: 12.5px;
  color: var(--ink);
}

.foot {
  margin: 16px 0 0;
  text-align: center;
  font-size: 11px;
  letter-spacing: 0.04em;
  color: var(--ink-3);
}
</style>
