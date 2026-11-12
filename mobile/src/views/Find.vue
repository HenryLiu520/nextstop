<template>
  <div class="page">
    <div class="card">
      <h2>{{ t('find.title') }}</h2>

      <label class="field">
        <span>{{ t('find.building') }}</span>
        <select v-model="building">
          <option value="">{{ t('find.allBuildings') }}</option>
          <option v-for="b in buildings" :key="b" :value="b">{{ b }}</option>
        </select>
      </label>

      <label class="toggle">
        <input type="checkbox" v-model="useTime" />
        <span>{{ t('find.timeFilter') }}</span>
      </label>

      <template v-if="useTime">
        <label class="field">
          <span>{{ t('find.date') }}</span>
          <input v-model="date" type="date" />
        </label>

        <div class="row">
          <label class="field">
            <span>{{ t('find.from') }}</span>
            <select v-model="from">
              <option v-for="h in hours" :key="h" :value="h">{{ h }}</option>
            </select>
          </label>
          <label class="field">
            <span>{{ t('find.to') }}</span>
            <select v-model="to">
              <option v-for="h in toOptions" :key="h" :value="h">{{ h }}</option>
            </select>
          </label>
        </div>
      </template>

      <p v-if="error" class="err">{{ error }}</p>
      <p v-else class="hint">{{ t('find.count', { n: rooms.length }) }}</p>
    </div>

    <router-link
      v-for="r in rooms"
      :key="r.code"
      class="room"
      :to="{ path: '/room/' + r.code, query: { date } }"
    >
      <div class="code">{{ r.code }}</div>
      <div class="where">
        {{ r.building }} · {{ floorText(r) }} · {{ t('find.seats', { n: r.capacity }) }}
      </div>
      <div class="meta">
        <span class="score" :class="{ none: r.score === null }">
          {{ r.score === null ? t('find.noScore') : r.score.toFixed(1) + ' / 10' }}
        </span>
        <span class="chip" v-if="r.reviewCount">{{ t('find.reviews', { n: r.reviewCount }) }}</span>
        <span class="chip" v-if="useTime && r.free === true">{{ t('find.free') }}</span>
        <span class="chip off" v-else-if="useTime && r.free === false">{{ t('find.busy') }}</span>
      </div>
    </router-link>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { api, errorText } from '../api.js'
import { t } from '../i18n.js'

const today = () => new Date().toISOString().slice(0, 10)
const pad = (n) => String(n).padStart(2, '0')

const rooms = ref([])
const buildings = ref([])
const building = ref('')
const useTime = ref(false)
const date = ref(today())
const from = ref('09:00')
const to = ref('10:50')
const error = ref('')

const hours = Array.from({ length: 13 }, (_, i) => `${pad(i + 8)}:00`)
const toOptions = Array.from({ length: 12 }, (_, i) => `${pad(i + 9)}:50`)

const floorText = (room) => (room.floor ? `${t('room.floor')} ${room.floor}` : '')

async function load() {
  error.value = ''
  try {
    const params = useTime.value ? { date: date.value, from: from.value, to: to.value } : {}
    const { data } = await api.get('/rooms/overview', { params })
    rooms.value = (data || []).filter((r) => !building.value || r.building === building.value)
  } catch (e) {
    error.value = errorText(e)
    rooms.value = []
  }
}

watch([building, useTime, date, from, to], load)

onMounted(async () => {
  try {
    const { data } = await api.get('/buildings')
    buildings.value = data || []
  } catch {
    /* 楼栋拿不到不影响列表 */
  }
  await load()
})
</script>
