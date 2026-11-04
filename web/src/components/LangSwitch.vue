<script setup>
import { LOCALES, locale, setLocale } from '../i18n/index.js'
</script>

<template>
  <div class="langs" role="group" aria-label="Language">
    <button
      v-for="l in LOCALES"
      :key="l.id"
      type="button"
      class="lang"
      :class="{ on: locale === l.id }"
      :aria-pressed="locale === l.id"
      @click="setLocale(l.id)"
    >
      {{ l.label }}
    </button>
  </div>
</template>

<style scoped>
.langs {
  flex: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding-top: 2px;
}

.lang {
  height: 26px;
  min-width: 30px;
  padding: 0 8px;
  border: 1px solid transparent;
  border-radius: var(--r-pill);
  color: var(--ink-3);
  font-size: 11.5px;
  letter-spacing: 0.03em;
  line-height: 1;
  transition: color 160ms var(--ease), border-color 160ms var(--ease), background 160ms var(--ease);
}

.lang.on {
  border-color: var(--ink);
  background: var(--card);
  color: var(--ink);
}
</style>
