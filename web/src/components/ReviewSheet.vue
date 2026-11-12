<script setup>
/* 评价面板：总分 + 六个固态因子，各 1 到 10；可写文字、可加照片。
 * 数据走 /api/rooms/{code}/reviews 与 /api/reviews/{id}/photos，失败就说失败。 */
import { ref, computed, watch } from 'vue'
import { state, closeReview, toast } from '../store.js'
import { t } from '../i18n/index.js'
import { fetchReviews, submitReview, uploadReviewPhoto } from '../api/index.js'

const FACTORS = ['quiet', 'desk', 'sockets', 'aircon', 'space', 'light']

const roomCode = computed(() => (state.review ? state.review.roomCode : null))
const open = computed(() => Boolean(state.review))

const loading = ref(false)
const error = ref('')
const list = ref({ average: 0, count: 0, items: [] })

const rating = ref(8)
const dims = ref(FACTORS.reduce((o, k) => ({ ...o, [k]: 7 }), {}))
const body = ref('')
const files = ref([])
const busy = ref(false)

function reset() {
  rating.value = 8
  dims.value = FACTORS.reduce((o, k) => ({ ...o, [k]: 7 }), {})
  body.value = ''
  files.value = []
  error.value = ''
}

async function load() {
  if (!roomCode.value) return
  loading.value = true
  error.value = ''
  try {
    list.value = await fetchReviews(roomCode.value)
    const mine = (list.value.items || []).find((r) => r.mine)
    if (mine) {
      rating.value = mine.rating
      body.value = mine.body
      if (mine.dimensions) dims.value = { ...dims.value, ...mine.dimensions }
    }
  } catch (e) {
    error.value = e.message
    list.value = { average: 0, count: 0, items: [] }
  } finally {
    loading.value = false
  }
}

watch(open, (v) => {
  if (v) {
    reset()
    load()
  }
})

function onFiles(e) {
  files.value = Array.from(e.target.files || []).slice(0, 3)
}

async function submit() {
  if (busy.value) return
  busy.value = true
  error.value = ''
  try {
    const saved = await submitReview(roomCode.value, {
      rating: rating.value,
      body: body.value,
      dimensions: dims.value,
    })
    for (const f of files.value) {
      await uploadReviewPhoto(saved.id, f)
    }
    await load()
    toast(t('review.saved'), { undo: false })
  } catch (e) {
    error.value = e.message
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <div v-if="open" class="wrap" @click.self="closeReview()">
    <section class="sheet" role="dialog" aria-modal="true">
      <header class="head">
        <div>
          <h2 class="code">{{ roomCode }}</h2>
          <p class="avg" v-if="list.count">{{ list.average }} / 10 · {{ t('review.count').replace('{n}', list.count) }}</p>
          <p class="avg" v-else>{{ t('review.none') }}</p>
        </div>
        <button class="x" type="button" :aria-label="t('review.close')" @click="closeReview()">×</button>
      </header>

      <div class="scroll">
        <p v-if="loading" class="note">{{ t('review.loading') }}</p>

        <ul v-if="list.items.length" class="list">
          <li v-for="r in list.items" :key="r.id" class="item">
            <div class="row">
              <span class="who">{{ r.author }}</span>
              <span class="score num">{{ r.rating }}/10</span>
            </div>
            <p class="text">{{ r.body }}</p>
            <div v-if="r.dimensions && Object.keys(r.dimensions).length" class="dims">
              <span v-for="(v, k) in r.dimensions" :key="k" class="chip">{{ t('review.dim.' + k) }} {{ v }}</span>
            </div>
            <div v-if="r.photos && r.photos.length" class="photos">
              <img v-for="p in r.photos" :key="p.id" :src="p.url" alt="" />
            </div>
          </li>
        </ul>

        <div class="form">
          <label class="line">
            <span class="lbl">{{ t('review.overall') }}</span>
            <input v-model.number="rating" type="range" min="1" max="10" step="1" />
            <span class="val num">{{ rating }}</span>
          </label>

          <label v-for="k in FACTORS" :key="k" class="line">
            <span class="lbl">{{ t('review.dim.' + k) }}</span>
            <input v-model.number="dims[k]" type="range" min="1" max="10" step="1" />
            <span class="val num">{{ dims[k] }}</span>
          </label>

          <textarea v-model="body" class="area" rows="3" :placeholder="t('review.placeholder')" />

          <label class="file">
            <input type="file" accept="image/*" multiple @change="onFiles" />
            <span>{{ files.length ? t('review.picked').replace('{n}', files.length) : t('review.addPhoto') }}</span>
          </label>

          <p v-if="error" class="err">{{ error }}</p>

          <button class="btn primary" type="button" :disabled="busy" @click="submit">
            {{ busy ? t('review.saving') : t('review.save') }}
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.wrap {
  position: absolute;
  inset: 0;
  z-index: 40;
  display: flex;
  align-items: flex-end;
  background: rgba(28, 26, 23, 0.42);
}

.sheet {
  width: 100%;
  max-height: 88%;
  display: flex;
  flex-direction: column;
  border-radius: 14px 14px 0 0;
  background: var(--paper);
}

.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 16px var(--pad) 10px;
  border-bottom: 1px solid var(--line-soft);
}

.code {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.avg {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--ink-2);
}

.x {
  flex: none;
  width: 32px;
  height: 32px;
  border: 1px solid var(--line);
  border-radius: 50%;
  background: transparent;
  font-size: 18px;
  line-height: 1;
  color: var(--ink-2);
}

.scroll {
  overflow-y: auto;
  padding: 12px var(--pad) 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.note {
  margin: 0;
  font-size: 13px;
  color: var(--ink-2);
}

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.item {
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--card);
}

.row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 8px;
}

.who {
  font-size: 13px;
  font-weight: 600;
}

.score {
  font-size: 13px;
  color: var(--green);
}

.text {
  margin: 6px 0 0;
  font-size: 14px;
  line-height: 1.5;
}

.dims {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.chip {
  padding: 2px 7px;
  border-radius: 999px;
  background: var(--green-soft, #eceee9);
  font-size: 11px;
  color: var(--ink-2);
}

.photos {
  display: flex;
  gap: 6px;
  margin-top: 8px;
}

.photos img {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid var(--line);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--card);
}

.line {
  display: grid;
  grid-template-columns: 92px 1fr 28px;
  align-items: center;
  gap: 10px;
}

.lbl {
  font-size: 12px;
  color: var(--ink-2);
}

.val {
  font-size: 13px;
  text-align: right;
}

input[type='range'] {
  accent-color: var(--green);
}

.area {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--line);
  border-radius: 6px;
  background: var(--paper);
  font: inherit;
  resize: vertical;
}

.file {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--ink-2);
}

.file input {
  font-size: 12px;
}

.err {
  margin: 0;
  font-size: 12px;
  color: var(--red);
}

.btn.primary {
  height: 42px;
  background: var(--ink);
  color: var(--paper);
  border-color: var(--ink);
}
</style>
