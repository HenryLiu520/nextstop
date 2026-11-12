<template>
  <div class="page">
    <div v-if="room" class="card">
      <h2>{{ room.code }}</h2>
      <dl class="kv">
        <dt>{{ t('find.building') }}</dt>
        <dd>{{ room.building }}</dd>
        <dt>{{ t('room.floor') }}</dt>
        <dd>{{ room.floor || t('room.unknown') }}</dd>
        <dt>{{ t('room.capacity') }}</dt>
        <dd>{{ room.capacity }}</dd>
        <dt>{{ t('room.seatType') }}</dt>
        <dd>{{ room.seatType || t('room.unknown') }}</dd>
        <dt>{{ t('find.sockets') }}</dt>
        <dd>{{ room.sockets === true ? '✓' : room.sockets === false ? '✗' : t('room.unknown') }}</dd>
      </dl>

      <div class="scores">
        <div class="tile overall">
          <span class="k">{{ t('rev.score') }}</span>
          <b class="v">{{ overall === null ? '—' : overall.toFixed(1) }}<em v-if="overall !== null">/10</em></b>
        </div>
        <div class="grid">
          <div class="tile" v-for="f in FACTORS" :key="f">
            <span class="k">{{ t('rev.' + f) }}</span>
            <b class="v">{{ factorAvg(f) === null ? '—' : factorAvg(f) }}</b>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <h2>{{ t('room.timetable') }} · {{ date }}</h2>
      <div v-if="segments.length">
        <div class="strip">
          <div
            v-for="(s, i) in segments"
            :key="i"
            class="cell"
            :class="s.type === 'BUSY' ? 'busy' : 'free'"
            :style="{ flex: s.span }"
            :title="s.label || ''"
          >
            {{ s.type === 'BUSY' && s.span >= 3 ? s.label || '' : '' }}
          </div>
        </div>
        <div class="ticks">
          <span>{{ segments[0]?.hour || '08:00' }}</span>
          <span>{{ segments[segments.length - 1]?.to || '21:50' }}</span>
        </div>
      </div>
      <p v-else class="hint">{{ t('common.loading') }}</p>
    </div>

    <!-- 评价：先看，再写。写的那一层默认收起 -->
    <div class="card">
      <div class="head">
        <h2>
          {{ t('room.reviews') }}
          <span v-if="reviews && reviews.count" class="avg">{{ reviews.average }}<em>/10</em></span>
        </h2>
        <button v-if="!editing" class="addBtn" type="button" @click="editing = true">
          + {{ t('rev.add') }}
        </button>
      </div>

      <div v-for="rv in reviews?.items || []" :key="rv.id" class="review">
        <div class="rhead">
          <span class="score">{{ rv.rating }}<em>/10</em></span>
          <span class="who">{{ rv.author }}</span>
        </div>
        <div class="body">{{ rv.body }}</div>
        <div v-if="rv.dimensions && Object.keys(rv.dimensions).length" class="chips">
          <span v-for="(v, k) in rv.dimensions" :key="k">{{ t('rev.' + k) }} {{ v }}</span>
        </div>
        <div v-if="rv.photos && rv.photos.length" class="photos">
          <img v-for="p in rv.photos" :key="p.id" :src="p.url" alt="" loading="lazy" @click="zoom = p.url" />
        </div>
      </div>

      <p v-if="reviews && !reviews.count" class="hint">{{ t('room.noReviews') }}</p>

      <form v-if="editing" class="form" @submit.prevent="submit">
        <div class="row">
          <span class="lbl">{{ t('rev.overall') }}</span>
          <div class="stars">
            <button
              v-for="n in 10"
              :key="n"
              type="button"
              class="star"
              :class="{ on: n <= form.rating }"
              :aria-label="n"
              @click="form.rating = n"
            >★</button>
          </div>
          <b class="val">{{ form.rating }}</b>
        </div>

        <div class="row" v-for="f in FACTORS" :key="f">
          <span class="lbl">{{ t('rev.' + f) }}</span>
          <div class="stars small">
            <button
              v-for="n in 10"
              :key="n"
              type="button"
              class="star"
              :class="{ on: n <= form.dims[f] }"
              :aria-label="n"
              @click="form.dims[f] = n"
            >★</button>
          </div>
          <b class="val">{{ form.dims[f] }}</b>
        </div>

        <textarea v-model="form.body" rows="3" class="area" />

        <label class="file">
          <input type="file" accept="image/*" multiple @change="onFiles" />
          <span class="pick">{{ files.length ? t('rev.picked', { n: files.length }) : t('rev.addPhoto') }}</span>
        </label>

        <p v-if="formError" class="err">{{ formError }}</p>

        <div class="actions">
          <button class="ghost" type="button" @click="editing = false">{{ t('rev.cancel') }}</button>
          <button class="primary" type="submit" :disabled="saving">
            {{ saving ? t('common.loading') : t('rev.save') }}
          </button>
        </div>
      </form>
    </div>

    <div v-if="zoom" class="lightbox" @click="zoom = ''">
      <img :src="zoom" alt="" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { api, token, setToken, errorText } from '../api.js'
import { t } from '../i18n.js'

const route = useRoute()
const code = route.params.code
const date = route.query.date || new Date().toISOString().slice(0, 10)

const FACTORS = ['quiet', 'desk', 'sockets', 'aircon', 'space', 'light']
const form = ref({ rating: 8, dims: { quiet: 7, desk: 7, sockets: 7, aircon: 7, space: 7, light: 7 }, body: '' })
const files = ref([])
const saving = ref(false)
const formError = ref('')
const editing = ref(false)

const overall = computed(() => {
  const v = FACTORS.map((f) => factorAvg(f)).filter((x) => x !== null)
  if (!v.length) return null
  return v.reduce((a, b) => a + b, 0) / v.length
})

function factorAvg(f) {
  const items = (reviews.value?.items || []).filter((r) => r.dimensions && r.dimensions[f] != null)
  if (!items.length) return null
  return Math.round((items.reduce((a, r) => a + r.dimensions[f], 0) / items.length) * 10) / 10
}

const room = ref(null)
const segments = ref([])
const reviews = ref(null)
const zoom = ref('')

function onFiles(e) {
  files.value = Array.from(e.target.files || []).slice(0, 3)
}

async function ensureToken() {
  if (token()) return token()
  const { data } = await api.post('/auth/login', { username: 'user1', password: 'user123' })
  setToken(data.token)
  return data.token
}

async function submit() {
  saving.value = true
  formError.value = ''
  try {
    await ensureToken()
    const { data } = await api.post(`/rooms/${code}/reviews`, {
      rating: form.value.rating,
      body: form.value.body,
      dimensions: form.value.dims,
    })
    for (const f of files.value) {
      const fd = new FormData()
      fd.append('file', f)
      await api.post(`/reviews/${data.id}/photos`, fd)
    }
    const fresh = await api.get(`/rooms/${code}/reviews`, { params: { sort: 'new' } })
    reviews.value = fresh.data
    files.value = []
    form.value.body = ''
    editing.value = false
  } catch (e) {
    formError.value = errorText(e)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const { data } = await api.get('/rooms')
    room.value = (data || []).find((r) => r.code === code) || null
  } catch (e) {
    /* 房间信息拿不到也不挡时间轴 */
  }
  try {
    const { data } = await api.get(`/rooms/${code}/timeline`, { params: { date } })
    segments.value = data.segments || []
  } catch (e) {
    segments.value = []
  }
  try {
    const { data } = await api.get(`/rooms/${code}/reviews`, { params: { sort: 'new' } })
    reviews.value = data
  } catch (e) {
    reviews.value = { count: 0, items: [] }
  }
})
</script>

<style scoped>
h2 {
  font-family: Georgia, 'Times New Roman', serif;
  font-weight: 400;
  font-size: 19px;
  margin: 0 0 10px;
}

.head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.avg {
  font-size: 13px;
  color: var(--green);
  margin-left: 6px;
}
.avg em,
.score em {
  font-style: normal;
  font-size: 10px;
  color: var(--muted);
}

.addBtn {
  border: 1px solid var(--green);
  background: transparent;
  color: var(--green);
  border-radius: 6px;
  height: 30px;
  padding: 0 10px;
  font-size: 11px;
  letter-spacing: 0.06em;
  cursor: pointer;
}

.review {
  border-top: 1px solid var(--line);
  padding: 10px 0 2px;
}

.rhead {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.score {
  font-size: 14px;
  color: var(--green);
}

.who {
  font-size: 11px;
  color: var(--muted);
}

.body {
  margin-top: 5px;
  font-size: 12px;
  line-height: 1.55;
  color: var(--ink);
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 7px;
}

.chips span {
  font-size: 9px;
  padding: 2px 6px;
  border-radius: 999px;
  background: var(--mint);
  color: var(--muted);
}

.photos {
  display: flex;
  gap: 6px;
  margin-top: 7px;
}

.photos img {
  width: 54px;
  height: 54px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid var(--line);
}

/* 表单：三重网格，标签 / 控件 / 数值 各占一列，行行对齐 */
.form {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.row {
  display: grid;
  grid-template-columns: 92px 1fr 22px;
  align-items: center;
  gap: 8px;
}

.lbl {
  font-size: 11px;
  color: var(--muted);
}

.val {
  font-size: 11px;
  text-align: right;
  color: var(--ink);
}

.stars {
  display: flex;
  gap: 2px;
}

.star {
  border: none;
  background: none;
  padding: 0;
  font-size: 17px;
  line-height: 1;
  color: #d7ddd7;
  cursor: pointer;
}

.star.on {
  color: var(--gold);
}

.ticks10 {
  display: flex;
  gap: 3px;
}

.tick {
  flex: 1;
  height: 12px;
  border: 1px solid var(--line);
  border-radius: 2px;
  background: transparent;
  padding: 0;
  cursor: pointer;
}

.tick.on {
  background: var(--green);
  border-color: var(--green);
}

.area {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: var(--paper);
  font: inherit;
  font-size: 12px;
  padding: 8px;
  color: var(--ink);
  resize: vertical;
}

.file {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 10px;
  color: var(--muted);
}

.file input {
  font-size: 10px;
}

.err {
  margin: 0;
  font-size: 11px;
  color: #a3312c;
}

.actions {
  display: flex;
  gap: 8px;
}

.ghost,
.primary {
  flex: 1;
  height: 38px;
  border-radius: 6px;
  font-size: 11px;
  letter-spacing: 0.08em;
  cursor: pointer;
}

.ghost {
  border: 1px solid var(--line);
  background: transparent;
  color: var(--muted);
}

.primary {
  border: 1px solid var(--green);
  background: var(--green);
  color: #f7f4ec;
}

.lightbox {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.82);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.lightbox img {
  max-width: 92%;
  max-height: 86%;
  border-radius: 6px;
}

/* 六个评分方框：一行三个，两排，全部等宽等高；样式照首页的 NOW 标签 */
.scores {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 12px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 6px;
}

.tile {
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  height: 34px;
  padding: 0 8px;
  border: 1px solid var(--line);
  border-radius: 4px;
  background: #fff;
}

.tile .k {
  min-width: 0;
  font-size: 9px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tile .v {
  font-size: 12px;
  color: var(--ink);
  font-variant-numeric: tabular-nums;
}

.tile .v em {
  font-style: normal;
  font-size: 8px;
  color: var(--muted);
}

.tile.overall {
  background: var(--green);
  border-color: var(--green);
}

.tile.overall .k { color: rgba(247, 244, 236, 0.72); }
.tile.overall .v { color: #e3c68c; }
.tile.overall .v em { color: rgba(227, 198, 140, 0.75); }

.stars.small .star { font-size: 13px; }

.file input { display: none; }

.pick {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 10px;
  border: 1px solid var(--line);
  border-radius: 6px;
  font-size: 10px;
  color: var(--muted);
  cursor: pointer;
}
</style>
