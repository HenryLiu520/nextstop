/* ------------------------------------------------------------------
 * NextStop · 唯一的数据注入点
 *
 * 组件只认下面这个形状，不认 HTTP。以后接真接口，只改本文件：
 *
 *   GET /api/day?date=2026-10-30
 *   {
 *     date: '2026-10-30',
 *     weekday: 4,              // 1 = 周一 … 5 = 周五
 *     month: 10, day: 30,
 *     now: '11:24',            // 服务端时间，用来判断"当前块"
 *     stops: [{
 *       id, type: 'class'|'study'|'meal'|'meeting',
 *       start: '11:00', end: '12:00',
 *       title: 'COMP1110',     // 课程号 / 事项名
 *       place: 'KB 227',       // 楼栋 + 房间，语言中立
 *       state: 'past'|'current'|'upcoming',
 *       walkBefore: 5,         // 与上一站之间的步行分钟数，0 = 背靠背不画缝隙
 *       booked: false,         // 见面块是否已预约（实心 / 点线）
 *       pending: false         // true = 待订，块用点线描边
 *     }]
 *   }
 * ------------------------------------------------------------------ */

const DAY_FIXTURE = {
  date: '2026-10-30',
  weekday: 4,
  month: 10,
  day: 30,
  now: '11:24',
  stops: [
    {
      id: 's1',
      type: 'class',
      start: '09:00',
      end: '10:50',
      title: 'COMP1110',
      place: 'KB 223',
      state: 'past',
      walkBefore: 0,
    },
    {
      id: 's2',
      type: 'study',
      start: '11:00',
      end: '12:00',
      title: 'COMP1110',
      place: 'KB 227',
      state: 'current',
      walkBefore: 5,
    },
    {
      id: 's3',
      type: 'meal',
      start: '12:10',
      end: '12:40',
      title: '',
      place: 'CYM G/F',
      state: 'upcoming',
      walkBefore: 12,
    },
    {
      id: 's4',
      type: 'class',
      start: '14:00',
      end: '15:50',
      title: 'COMP1110',
      place: 'KB 426',
      state: 'upcoming',
      walkBefore: 8,
    },
    {
      id: 's5',
      type: 'meeting',
      start: '16:00',
      end: '16:50',
      title: 'Group 08',
      place: 'LIB 3/F',
      state: 'upcoming',
      walkBefore: 6,
      pending: true,
      booked: false,
    },
    {
      id: 's6',
      type: 'study',
      start: '17:00',
      end: '18:20',
      title: 'COMP1110',
      place: 'LIB 3/F',
      state: 'upcoming',
      walkBefore: 0,
    },
  ],
}

/* 规划页顶上一周：bars 是忙碌度，0–1 的四个小横杠长度 */
const WEEK_FIXTURE = [
  { weekday: 1, month: 10, day: 27, bars: [0.55, 0.9, 0.4], busy: 3 },
  { weekday: 2, month: 10, day: 28, bars: [0.85, 0.6, 0.95, 0.5], busy: 4 },
  { weekday: 3, month: 10, day: 29, bars: [0.45, 0.5], busy: 2 },
  { weekday: 4, month: 10, day: 30, bars: [0.9, 0.75, 0.85, 0.95], busy: 6 },
  { weekday: 5, month: 10, day: 31, bars: [0.6, 0.35], busy: 2 },
]

/* 换一个：一次三个候选，每个都带"最晚几点必须走" */
const SWAP_FIXTURE = {
  study: [
    { id: 'c1', place: '智华馆 B1', leaveBy: '11:45' },
    { id: 'c2', place: 'KB 305 空教室', leaveBy: '11:50' },
    { id: 'c3', place: 'LIB 2/F 静区', leaveBy: '11:40' },
  ],
  meal: [
    { id: 'c1', place: '庄月明 3/F', leaveBy: '12:05' },
    { id: 'c2', place: 'SU 食堂', leaveBy: '12:00' },
    { id: 'c3', place: '智华馆 1/F 咖啡', leaveBy: '12:15' },
  ],
  meeting: [
    { id: 'c1', place: 'LIB 3/F 讨论室 2', leaveBy: '15:50' },
    { id: 'c2', place: 'KB 715', leaveBy: '15:55' },
    { id: 'c3', place: 'Main Building 2/F', leaveBy: '15:40' },
  ],
  class: [],
}

const clone = (v) => JSON.parse(JSON.stringify(v))

/* ------------------------------------------------------------------
 * 真实数据层（Spring Boot，:8080，Vite 代理 /api）
 *
 * 这里只做一件事：把后端的 availability 结果接进来，让计划里的
 * 自习站指向真实存在的空房间。规划本身仍在客户端，还没实现。
 * ------------------------------------------------------------------ */

const DEFAULT_BUILDING = 'Central Podium'

const pad = (n) => String(n).padStart(2, '0')
const today = () => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** 后端是否活着。前端用它决定要不要显示"数据来自后端" */
export async function ping() {
  try {
    const r = await fetch('/api/buildings', { signal: AbortSignal.timeout(2500) })
    return r.ok
  } catch {
    return false
  }
}

/** 真实的空教室：GET /api/availability */
async function fetchAvailable(building, from, to) {
  const url = `/api/availability?date=${today()}&building=${encodeURIComponent(building)}&from=${from}&to=${to}`
  const r = await fetch(url, { signal: AbortSignal.timeout(3000) })
  if (!r.ok) throw new Error(`availability ${r.status}`)
  const data = await r.json()
  return (data.rooms || []).filter((x) => x.status === 'AVAILABLE' && x.matches)
}

/** 一天 · 六站。拿到后端就用后端，拿不到就用 fixture，不假装 */
export async function fetchDay(_date) {
  const day = clone(DAY_FIXTURE)
  try {
    const live = await fetchAvailable(DEFAULT_BUILDING, '08:00', '15:50')
    if (!live.length) throw new Error('no room')

    const codes = live.map((x) => x.room.code)
    let i = 0
    for (const stop of day.stops) {
      if (stop.type === 'study') {
        stop.place = codes[i % codes.length]
        i += 1
      }
    }
    day.source = 'live'
    day.building = DEFAULT_BUILDING
    day.roomsFree = live.length
  } catch {
    day.source = 'fixture'
  }
  return day
}

/** 一周的忙碌度，给规划页顶上那五列 */
export async function fetchWeek() {
  return clone(WEEK_FIXTURE)
}

/** 跨楼找空房间：一个楼往往只有一间，所以逛到凑够三个为止 */
async function roomsAcrossBuildings(want) {
  const r = await fetch('/api/buildings', { signal: AbortSignal.timeout(2500) })
  if (!r.ok) throw new Error('buildings ' + r.status)
  const buildings = await r.json()
  const names = (Array.isArray(buildings) ? buildings : buildings.buildings || [])
    .map((b) => (typeof b === 'string' ? b : b.name))
    .filter(Boolean)

  const out = []
  for (const b of names.slice(0, 6)) {
    try {
      const live = await fetchAvailable(b, '08:00', '15:50')
      for (const x of live) out.push({ code: x.room.code, floor: x.room.floor })
      if (out.length >= want) break
    } catch {
      /* 一个楼失败不影响其他楼 */
    }
  }
  return out
}

/** 换一个 · 三个候选（exclude 掉已经看过的，永不重复） */
export async function fetchCandidates(type, excludeIds = []) {
  if (type === 'study') {
    try {
      const rooms = await roomsAcrossBuildings(9)
      const pool = rooms
        .map((x) => ({ id: x.code, place: x.code, leaveBy: x.floor }))
        .filter((c) => !excludeIds.includes(c.id))
        .slice(0, 3)
      if (pool.length) return pool
    } catch {
      /* 落到 fixture */
    }
  }
  const pool = SWAP_FIXTURE[type] || []
  return clone(pool.filter((c) => !excludeIds.includes(c.id)))
}

/** 偏好落盘 */
export async function savePrefs(prefs) {
  // 真接口：await fetch('/api/me/prefs', { method: 'PUT', body: JSON.stringify(prefs) })
  return clone(prefs)
}

/** 规划页应用一句话 / 手动添加 */
export async function applyDayPlan(_payload) {
  // 真接口：return (await fetch('/api/day/plan', { method: 'POST', body: ... })).json()
  return { ok: true, changed: 2 }
}

/* ------------------------------------------------------------------
 * 查找 · 真实接口
 * 两块数据都来自后端，失败就抛错，不假装
 * ------------------------------------------------------------------ */

/** 楼栋清单：GET /api/buildings，返回名字数组 */
export async function fetchBuildings() {
  const r = await fetch('/api/buildings', { signal: AbortSignal.timeout(2500) })
  if (!r.ok) throw new Error('buildings ' + r.status)
  const data = await r.json()
  return (Array.isArray(data) ? data : data.buildings || [])
    .map((b) => (typeof b === 'string' ? b : b.name))
    .filter(Boolean)
}

/** 某楼某段时间还空着的房间：GET /api/availability */
export async function fetchFreeRooms({ date, building, from, to }) {
  const qs = new URLSearchParams({ date, from, to })
  if (building) qs.set('building', building)
  const r = await fetch('/api/availability?' + qs.toString(), { signal: AbortSignal.timeout(4000) })
  if (!r.ok) throw new Error('availability ' + r.status)
  const data = await r.json()
  const rooms = data.rooms || []
  return rooms.filter((x) => x.status === 'AVAILABLE' && x.matches)
}

/* ------------------------------------------------------------------
 * 评价 · 真实接口
 * 读公开；写要登录，原型用演示账号 user1 / user123 换一个 token
 * ------------------------------------------------------------------ */

const TOKEN_KEY = 'nextstop.token'

async function ensureToken() {
  const saved = localStorage.getItem(TOKEN_KEY)
  if (saved) return saved
  const r = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'user1', password: 'user123' }),
    signal: AbortSignal.timeout(3000),
  })
  if (!r.ok) throw new Error('login ' + r.status)
  const data = await r.json()
  localStorage.setItem(TOKEN_KEY, data.token)
  return data.token
}

/** 某间房的评价：平均分 + 条数 + 条目 */
export async function fetchReviews(code) {
  const r = await fetch(`/api/rooms/${encodeURIComponent(code)}/reviews`, { signal: AbortSignal.timeout(3000) })
  if (!r.ok) throw new Error('reviews ' + r.status)
  return r.json()
}

/** 提交或更新我自己的评价 */
export async function submitReview(code, payload) {
  const token = await ensureToken()
  const r = await fetch(`/api/rooms/${encodeURIComponent(code)}/reviews`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + token },
    body: JSON.stringify(payload),
    signal: AbortSignal.timeout(4000),
  })
  const data = await r.json().catch(() => ({}))
  if (!r.ok) throw new Error(data.message || 'review ' + r.status)
  return data
}

/** 给一条评价加一张照片 */
export async function uploadReviewPhoto(reviewId, file) {
  const token = await ensureToken()
  const form = new FormData()
  form.append('file', file)
  const r = await fetch(`/api/reviews/${reviewId}/photos`, {
    method: 'POST',
    headers: { Authorization: 'Bearer ' + token },
    body: form,
    signal: AbortSignal.timeout(8000),
  })
  const data = await r.json().catch(() => ({}))
  if (!r.ok) throw new Error(data.message || 'photo ' + r.status)
  return data
}
