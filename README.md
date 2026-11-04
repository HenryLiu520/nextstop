<h1 align="center">Following It Is All You Need.</h1>

<p align="center">
  <b>NextStop</b><br>
  Decides where a student should go between classes,<br>
  and keeps that decision standing when the day moves.
</p>

<p align="center">
  <a href="https://henryliu520.github.io/nextstop/"><b>▶ Try the prototype</b></a> &nbsp;·&nbsp;
  <a href="docs/why.md">Why it exists</a> &nbsp;·&nbsp;
  <a href="docs/algorithm.md">The hard part</a> &nbsp;·&nbsp;
  <a href="product/NextStop.pdf">One page PDF</a>
</p>

<p align="center">
  <a href="README.md">English</a> · <a href="README.zh-CN.md">简体中文</a> · <a href="README.zh-TW.md">繁體中文</a>
</p>

<p align="center">
  <img alt="Vue 3" src="https://img.shields.io/badge/Vue-3-42b883?logo=vuedotjs&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white">
  <img alt="PostgreSQL 17" src="https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white">
  <img alt="27 tests" src="https://img.shields.io/badge/tests-27%20passing-1C1A17">
  <img alt="prototype" src="https://img.shields.io/badge/status-prototype-C8452D">
</p>

<p align="center">
  <img src="product/web-day.png" width="230" alt="One card, a stack of blocks">
</p>

<p align="center"><sub>One card, one time, one destination. The rest of the day is underneath.</sub></p>

---

A room finder tells you which rooms are empty. A calendar stores what you already decided. Neither
answers the question a student actually has in a corridor at 09:47, which is **what now**.

NextStop answers it, and when the day moves it changes as little as it can.

---

## Who this happens to

- **The first-year who finished gaokao** and no longer has anyone telling them where to go and grind.
- **The exchange student** with two hours between classes and nowhere legal to nap.
- **The one standing in the lift lobby** who has been deciding for eleven minutes.
- **The one eating lunch at 15:40** because at 12:00 the decision was too big.
- **Five people who all say "wherever you want"** and then spend twenty minutes not going anywhere.

None of them lack information. All of them lack a next step.

---

## What it does

<p align="center">
  <img src="product/day.png" width="240" alt="Day">
  <img src="product/repair.png" width="240" alt="Repair after a 20 minute overrun">
  <img src="product/plan.png" width="240" alt="Plan tab">
</p>

<p align="center"><sub>Left: the day as a stack. Middle: a 20 minute overrun changes one block, not five. Right: the week, one day, one sentence.</sub></p>

Three tabs, no toolbar. Blocks carry their own actions, and nothing is ever empty.

---

## The part that is actually hard

Not deciding a day. Deciding **how little to change** when the day moves.

```
RECOMPUTE EVERYTHING            REPAIR
────────────────────────        ────────────────────────
11:00–12:00  Study   →  11:00–11:40 Study      ← changed
12:10–12:40  Lunch   →  11:50–12:20 Lunch      ← changed
14:00–15:50  Class   →  14:00–15:50 Class        same
16:00–16:50  Meeting →  15:40–16:30 Meeting    ← changed
17:00–18:20  Study   →  16:40–18:00 Study      ← changed

5 blocks moved                  1 block moved
```

A plan that rewrites itself every time something moves is indistinguishable from having no plan.
Two stops count as the same stop when the purpose, the building and the start time still line up.
👉 **[algorithm.md](docs/algorithm.md)**

---

## Why the backend is not the product

```
web/            Vue 3 + Vite. One build, four containers:
                browser · PWA · Android shell · iOS shell
     |
     |  REST + JSON
     v
backend/        Spring Boot + PostgreSQL  (timetable, rooms, availability, audit)
     |
     v
mock-school-api/ a simulated school feed
```

The backend answers **where can I be**, which is one of the three things a student does not know,
and the easiest one. The plan lives in the client, so the corridor case works with no signal, and
a second device still cannot see your plan. Both consequences are honest.

---

## Documentation, in three languages

| | English | 简体 | 繁體 |
|---|---|---|---|
| **Why this exists** | [`why.md`](docs/why.md) | [`zh-CN`](docs/why.zh-CN.md) | [`zh-TW`](docs/why.zh-TW.md) |
| **What others cannot do** | [`highlights.md`](docs/highlights.md) | [`zh-CN`](docs/highlights.zh-CN.md) | [`zh-TW`](docs/highlights.zh-TW.md) |
| **Every feature** | [`features.md`](docs/features.md) | [`zh-CN`](docs/features.zh-CN.md) | [`zh-TW`](docs/features.zh-TW.md) |
| **Architecture** | [`architecture.md`](docs/architecture.md) | [`zh-CN`](docs/architecture.zh-CN.md) | [`zh-TW`](docs/architecture.zh-TW.md) |
| **The hard part** | [`algorithm.md`](docs/algorithm.md) | [`zh-CN`](docs/algorithm.zh-CN.md) | [`zh-TW`](docs/algorithm.zh-TW.md) |
| **Screens and rules** | [`screens.md`](docs/screens.md) | [`zh-CN`](docs/screens.zh-CN.md) | [`zh-TW`](docs/screens.zh-TW.md) |
| **Decision records** | [`adr/`](docs/adr/README.md) | [`zh-CN`](docs/adr/README.zh-CN.md) | [`zh-TW`](docs/adr/README.zh-TW.md) |
| **Everything, indexed** | [`docs/README.md`](docs/README.md) | [`zh-CN`](docs/README.zh-CN.md) | [`zh-TW`](docs/README.zh-TW.md) |

---

## Run it

```bash
git clone https://github.com/HenryLiu520/nextstop
cd nextstop

./scripts/start-all.sh                  # PostgreSQL 5433 → Spring Boot 8080
cd web && npm install && npm run dev    # http://127.0.0.1:5175
```

`web/` proxies `/api` to 8080, so the day on screen is built from real availability answers. The
planner behind it is still a stub, and the interface does not pretend otherwise.

No build step? Open [`product/demo/index.html`](product/demo/index.html). One file, no server,
works offline, three languages in the header.

---

## Built, specified, not built

| Part | State |
|---|---|
| Availability engine, rules, roles, audit, 27 tests | **built and tested** |
| Web interface, three tabs, three languages, real availability | **built** |
| Single file prototype, clickable end to end | **built** |
| Planner and repair ladder | **specified, not implemented** |
| Learning layer | **specified, not implemented** |
| PWA manifest, Android and iOS shells | **chosen, not built** |

The timetable is sample data. "Free" is not "empty". The one-block-versus-five-block comparison is
illustrative, not measured. Authentication is prototype grade.

---

<p align="center"><sub>
COMP1110 Group 08 · The University of Hong Kong · LIU Haoran (u3686264)<br>
The name is a pun on a paper about attention. That paper is about what to attend to. So is this.
</sub></p>
