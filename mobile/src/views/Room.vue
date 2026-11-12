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
      <div class="meta" style="margin-top: 10px" v-if="room.facilitiesVerifiedAt">
        <span class="chip">{{ t('room.facilities') }}: {{ room.facilitiesVerifiedAt }}</span>
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

    <div class="card">
      <h2>
        {{ t('room.reviews') }}
        <span v-if="reviews && reviews.count" class="hint" style="font-weight: 400">
          · {{ reviews.average }} / 10 ({{ reviews.count }})
        </span>
      </h2>
      <div v-for="rv in reviews?.items || []" :key="rv.id" class="review">
        <span class="stars">{{ rv.rating }} / 10</span>
        <span class="who">{{ rv.author }}</span>
        <div class="body">{{ rv.body }}</div>
        <div v-if="rv.dimensions && Object.keys(rv.dimensions).length" class="meta">
          <span v-for="(v, k) in rv.dimensions" :key="k" class="chip">{{ t('rev.' + k) }} {{ v }}</span>
        </div>
        <div v-if="rv.photos && rv.photos.length" class="photos">
          <img
            v-for="p in rv.photos"
            :key="p.id"
            :src="p.url"
            alt=""
            loading="lazy"
            @click="zoom = p.url"
          />
        </div>
      </div>
      <p v-if="reviews && !reviews.count" class="hint">{{ t('room.noReviews') }}</p>
    </div>

    <div class="card">
      <h2>{{ t('room.writeReview') }}</h2>
      <label class="line">
        <span>{{ t('rev.overall') }}</span>
        <input type="range" min="1" max="10" step="1" v-model.number="form.rating" />
        <b>{{ form.rating }}</b>
      </label>
      <label class="line" v-for="f in FACTORS" :key="f">
        <span>{{ t('rev.' + f) }}</span>
        <input type="range" min="1" max="10" step="1" v-model.number="form.dims[f]" />
        <b>{{ form.dims[f] }}</b>
      </label>
      <textarea v-model="form.body" rows="3" :placeholder="t('rev.body')" class="area" />
      <input type="file" accept="image/*" multiple @change="onFiles" class="file" />
      <p v-if="formError" class="err">{{ formError }}</p>
      <button class="btn" :disabled="saving" @click="submit">
        {{ saving ? t('common.loading') : t('rev.save') }}
      </button>
    </div>

    <!-- 点开看大图：手机上 104px 的缩略图看不清字体/插座位置 -->
    <div v-if="zoom" class="lightbox" @click="zoom = ''">
      <img :src="zoom" alt="" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
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
