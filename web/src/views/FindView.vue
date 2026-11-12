<script setup>
/* 查找：楼栋 + 日期 + 时段，问后端这栋楼这段时间还空着哪些教室。
 * 数据全部来自 /api/buildings 与 /api/availability，失败就说失败。 */
import { ref, computed, onMounted } from 'vue'
import { fetchBuildings, fetchFreeRooms } from '../api/index.js'
import { t } from '../i18n/index.js'

const buildings = ref([])
const building = ref('')
const date = ref(new Date().toISOString().slice(0, 10))
const from = ref('09:00')
const to = ref('10:50')

const loading = ref(false)
const searched = ref(false)
const error = ref('')
const rooms = ref([])

const pad = (n) => String(n).padStart(2, '0')
const hours = Array.from({ length: 13 }, (_, i) => `${pad(i + 8)}:00`)
const toOptions = Array.from({ length: 12 }, (_, i) => `${pad(i + 9)}:50`)

const countText = computed(() => {
  if (!searched.value || error.value) return ''
  return rooms.value.length
    ? t('find.count').replace('{n}', rooms.value.length)
    : t('find.none')
})

onMounted(async () => {
  try {
    buildings.value = await fetchBuildings()
  } catch {
    /* 楼栋拿不到就先留空，搜索时会报错 */
  }
})

async function search() {
  if (loading.value) return
  if (from.value >= to.value) {
    error.value = t('find.order')
    return
  }
  loading.value = true
  error.value = ''
  try {
    rooms.value = await fetchFreeRooms({ date: date.value, building: building.value, from: from.value, to: to.value })
    searched.value = true
  } catch (e) {
    error.value = t('find.failed') + ' · ' + e.message
    rooms.value = []
  } finally {
    loading.value = false
  }
}

const floorText = (room) => (room.floor ? room.floor : '')
</script>

<template>
  <div class="find">
    <section class="form">
      <label class="field">
        <span class="lbl">{{ t('find.building') }}</span>
        <select v-model="building">
          <option value="">{{ t('find.allBuildings') }}</option>
          <option v-for="b in buildings" :key="b" :value="b">{{ b }}</option>
        </select>
      </label>

      <label class="field">
        <span class="lbl">{{ t('find.date') }}</span>
        <input v-model="date" type="date" />
      </label>

      <div class="pair">
        <label class="field">
          <span class="lbl">{{ t('find.from') }}</span>
          <select v-model="from">
            <option v-for="h in hours" :key="h" :value="h">{{ h }}</option>
          </select>
        </label>
        <label class="field">
          <span class="lbl">{{ t('find.to') }}</span>
          <select v-model="to">
            <option v-for="h in toOptions" :key="h" :value="h">{{ h }}</option>
          </select>
        </label>
      </div>

      <button class="btn go" type="button" :disabled="loading" @click="search">
        {{ loading ? t('find.loading') : t('find.search') }}
      </button>
      <p v-if="error" class="err">{{ error }}</p>
    </section>

    <p v-if="countText" class="count">{{ countText }}</p>

    <ul v-if="rooms.length" class="list">
      <li v-for="r in rooms" :key="r.room.code" class="room">
        <span class="bar" aria-hidden="true" />
        <div class="main">
          <div class="code">{{ r.room.code }}</div>
          <div class="where num">
            {{ r.room.building }}<template v-if="floorText(r.room)"> · {{ floorText(r.room) }}</template>
            <template v-if="r.room.capacity"> · {{ t('find.seats').replace('{n}', r.room.capacity) }}</template>
          </div>
        </div>
        <div class="side num">
          {{ r.nextBusyStart ? t('find.nextBusy').replace('{t}', r.nextBusyStart) : t('find.allDay') }}
        </div>
      </li>
    </ul>

    <p v-else-if="searched && !error" class="empty">{{ t('find.none') }}</p>
  </div>
</template>

<style scoped>
.find {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px var(--pad) 20px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--card);
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.lbl {
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--ink-2);
}

select,
input[type='date'] {
  width: 100%;
  height: 38px;
  padding: 0 10px;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: var(--paper);
  color: var(--ink);
  font-size: 15px;
}

.pair {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.go {
  margin-top: 4px;
  height: 42px;
}

.err {
  margin: 0;
  font-size: 13px;
  color: var(--red);
}

.count {
  margin: 0 2px;
  font-size: 13px;
  color: var(--ink-2);
}

.list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.room {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  min-height: 58px;
  padding: 10px 12px 10px 16px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--card);
  overflow: hidden;
}

.bar {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--green);
}

.code {
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 0.01em;
}

.where {
  margin-top: 2px;
  font-size: 11px;
  color: var(--ink-2);
}

.side {
  flex: none;
  font-size: 11px;
  color: var(--ink-2);
  text-align: right;
}

.empty {
  margin: 0 2px;
  font-size: 13px;
  color: var(--ink-2);
}
</style>
