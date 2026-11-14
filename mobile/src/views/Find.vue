<template>
  <div class="page">
    <div class="card">
      <h2 class="ttl">{{ useTime ? t('find.title') : t('find.allRooms') }}</h2>

      <label class="field">
        <span>{{ t('find.building') }}</span>
        <select v-model="building">
          <option value="">{{ t('find.allBuildings') }}</option>
          <option v-for="b in buildings" :key="b" :value="b">{{ b }}</option>
        </select>
      </label>

      <label class="toggle">
        <input type="checkbox" v-model="useTime" />
        <span class="tgl" aria-hidden="true" />
        <span class="tglText">{{ t('find.timeFilter') }}</span>
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
    </div>

    <p v-if="!error" class="count">
      {{ useTime ? t('find.count', { n: rooms.length }) : t('find.countAll', { n: rooms.length }) }}
    </p>

    <router-link
      v-for="r in rooms"
      :key="r.code"
      class="row"
      :to="{ path: '/room/' + r.code, query: { date } }"
    >
      <i class="bar" aria-hidden="true" />
      <div class="main">
        <div class="code">{{ r.code }}</div>
        <div class="where">
          {{ r.building }} · {{ floorText(r) }} · {{ t('find.seats', { n: r.capacity }) }}
        </div>
      </div>
      <div class="right">
        <div class="score" :class="{ none: r.score === null }">
          {{ r.score === null ? '—' : r.score.toFixed(1) }}<em v-if="r.score !== null">/10</em>
        </div>
        <div class="sub">
          <template v-if="useTime">{{ r.free ? t('find.free') : t('find.busy') }}</template>
          <template v-else-if="r.reviewCount">{{ t('find.reviews', { n: r.reviewCount }) }}</template>
          <template v-else>{{ t('find.noScore') }}</template>
        </div>
      </div>
    </router-link>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { api, errorText } from '../api.js'
import { t } from '../i18n.js'
import '../../../product/demo/personal-state.js'

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
    let profile = 'device'
    try { profile = JSON.parse(localStorage.getItem('ec.user') || '{}').username || profile } catch {}
    rooms.value = (data || []).map(r => globalThis.NextStopPersonal.score(r, profile))
      .filter((r) => !building.value || r.building === building.value)
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

<style scoped>
.card { border-radius: 6px; }
.ttl { font-family: Georgia, 'Times New Roman', serif; font-weight: 400; font-size: 20px; }

.toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 10px 0 2px;
  font-size: 12px;
  color: var(--muted);
  cursor: pointer;
}
.toggle input { position: absolute; opacity: 0; width: 1px; height: 1px; }
.tgl {
  width: 34px; height: 20px; border-radius: 999px;
  background: #dfe4df; position: relative; transition: background 160ms ease;
  flex: none;
}
.tgl::after {
  content: ''; position: absolute; top: 2px; left: 2px;
  width: 16px; height: 16px; border-radius: 50%; background: #fff;
  transition: transform 160ms ease;
}
.toggle input:checked + .tgl { background: var(--green); }
.toggle input:checked + .tgl::after { transform: translateX(14px); }
.tglText { font-size: 12px; }

.count {
  margin: 4px 2px 8px;
  font-size: 10px;
  letter-spacing: 0.08em;
  color: var(--muted);
}

.row {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 62px;
  padding: 9px 12px 9px 16px;
  margin-bottom: 8px;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: #fff;
  text-decoration: none;
  color: var(--ink);
  overflow: hidden;
}
.bar { position: absolute; left: 0; top: 0; bottom: 0; width: 3px; background: var(--green); }
.code { font-family: Georgia, 'Times New Roman', serif; font-size: 17px; }
.where { margin-top: 2px; font-size: 10px; color: var(--muted); }
.right { margin-left: auto; text-align: right; }
.score { font-size: 15px; color: var(--green); font-variant-numeric: tabular-nums; }
.score em { font-style: normal; font-size: 10px; color: var(--muted); margin-left: 1px; }
.score.none { color: var(--muted); }
.sub { margin-top: 2px; font-size: 9px; color: var(--muted); }

.err { color: #a3312c; font-size: 11px; }
</style>
