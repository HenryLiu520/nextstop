import { reactive } from 'vue'
import { fetchDay, fetchWeek, fetchCandidates, savePrefs, applyDayPlan } from './api/index.js'
import { t } from './i18n/index.js'

export const state = reactive({
  tab: 'day',
  loading: true,
  day: null,
  week: [],
  selectedWeekday: 4,

  sheet: null, // { kind: 'block' | 'swap' | 'add', stop, candidates, seen }
  toast: null, // { text, undo: false }

  planText: '',
  planPreview: null,

  prefs: {
    walkSpeed: 1.25,
    weekdayEnd: '17:00',
  },
  savedPrefs: null,
})

let toastTimer = null

export function toast(text, options = {}) {
  state.toast = { text, undo: Boolean(options.undo) }
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    state.toast = null
    toastTimer = null
  }, 3600)
}

export function undoToast() {
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = null
  state.toast = null
}

export function setTab(tab) {
  state.tab = tab
  state.sheet = null
}

export async function load() {
  state.loading = true
  const [day, week] = await Promise.all([fetchDay(), fetchWeek()])
  state.day = day
  state.week = week
  state.selectedWeekday = day.weekday
  state.loading = false
}

/* ---------------- 一天 ---------------- */

/* 打开 sheet 时把浮着的那条 toast 收掉，别盖在纸上 */
function muteToast() {
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = null
  state.toast = null
}

export function openBlock(stop) {
  muteToast()
  state.sheet = { kind: 'block', stop }
}

const swapSeen = new Map()

export async function openSwap(stop) {
  muteToast()
  const seen = swapSeen.get(stop.id) || []
  const candidates = await fetchCandidates(stop.type, seen)
  state.sheet = { kind: 'swap', stop, candidates, seen }
}


export function openAdd(prefill) {
  muteToast()
  state.sheet = { kind: 'add', prefill: prefill || null }
}

export function closeSheet() {
  state.sheet = null
}

export function applySwap(candidate) {
  const sheet = state.sheet
  if (!sheet || !sheet.stop) return
  const stop = state.day.stops.find((s) => s.id === sheet.stop.id)
  if (stop) {
    swapSeen.set(stop.id, [...(sheet.seen || []), candidate.id])
    stop.place = candidate.place
    stop.pending = false
    stop.booked = true
  }
  closeSheet()
  toast(t('toast.swapped', { place: candidate.place }), { undo: true })
}

export function addStop(name, start, end) {
  if (!state.day) return
  state.day.stops.push({
    id: 'm' + Date.now(),
    type: 'study',
    start: start || '19:00',
    end: end || '20:00',
    title: name || t('type.study'),
    place: 'LIB 2/F',
    state: 'upcoming',
    walkBefore: 4,
  })
  closeSheet()
  toast(t('toast.added', { name: name || t('type.study') }), { undo: true })
}

/* ---------------- 规划 ---------------- */

export function selectWeekday(wd) {
  state.selectedWeekday = wd
  state.planPreview = null
}

export function buildPreview() {
  const text = state.planText.trim()
  if (!text) {
    state.planPreview = null
    toast(t('toast.needLine'))
    return
  }
  const slot = /两小时|2 ?小时|two hours/i.test(text)
    ? { a: '15:50', b: '17:50' }
    : { a: '15:50', b: '17:00' }
  state.planPreview = { a: slot.a, b: slot.b }
}

export async function applyPreview() {
  if (!state.planPreview) {
    buildPreview()
    return
  }
  await applyDayPlan({ text: state.planText, ...state.planPreview })
  const p = state.planPreview
  state.planPreview = null
  toast(t('toast.swapped', { place: `${p.a}–${p.b}` }), { undo: true })
}

/* ---------------- 我的 ---------------- */

export async function persistPrefs() {
  const saved = await savePrefs({ ...state.prefs })
  state.savedPrefs = saved
  toast(t('toast.saved'))
}
