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

---

## 09:47, Thursday

The lecture in KB ends early. The next one starts at 11:00 in CPD.

```
09:47  KB      you are here, holding a laptop and seventy minutes
10:50          the next class starts, in a different building
        ?
```

You know the campus has empty rooms. You know there is a library. You know you have a deadline.
What you do not know is **which of those things to do, in the next hour, in this building or the
one next door**.

So you stand in the corridor and decide. You do this three to five times a day, every day, and you
are usually fine with it. That is the part nobody has built for.

---

## What the room finder says, and what you needed

```
ROOM FINDER                     YOU
────────────────────────────    ────────────────────────────
"14:00 to 15:50, 12 rooms       "I have 70 minutes, my laptop
 free in this building."         is dying, and I have 2 hours
                                 of reading to do."
   ↑ an answer to a question       ↑ a decision about a day
     you did not ask
```

A finder returns a **set**. A day is a **sequence**, and the choice you make now changes what is
possible later. That is why more availability data never fixed this.

---

## What it does instead

One card. A stack of blocks. Walking time in the seams. Nothing is ever empty.

```
┌────────────────────────────────────────────┐
│ ✓ 09:00–10:50  Class  COMP1110 · KB        │
│     ┊ walk 5 min                           │
│ ▎ 11:00–12:00  Study  KB 227  ●            │
│     ┊ walk 10 min                          │
│ ┆ 12:10–12:40  Lunch  CYM G/F  ┆           │
│     ┊ walk 10 min                          │
│ ▎ 14:00–15:50  Class  CPD · LG05           │
└────────────────────────────────────────────┘
     End the day        Stay a bit longer
```

Block states are the whole legend: **solid** means solved, **dashed** means an external fact such
as a booked room, **dotted** means still to book. A block that has passed folds into a thin line,
because it should not keep asking for attention.

---

## It changes one block, not five

A class overruns by twenty minutes. Two ways to respond:

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
So repair asks a narrower question than "what is the best new day". It asks **which stops are
actually broken**, and touches only those. Two stops count as the same stop when the purpose, the
building and the start time all still line up. Tolerances are parameters, not laws of nature.

👉 **[algorithm.md](docs/algorithm.md)** for the criterion, the procedure and the cost order.

---

## Four actions, no toolbar

| Where you are | What you can do | What you cannot |
|---|---|---|
| a class | skip it | move it, cancel it, reschedule it |
| a study block | change it | split it into two |
| lunch | change it | order for you |
| a meeting | book it, change it | book a room the university does not let you book |

That last row matters. **The system does not pretend to allocate rooms it cannot allocate.** For an
ordinary classroom it makes a retractable claim. For anything with a real booking process it says
when to book, and then treats the outcome as a fact on the plan.

---

## Why the backend is not the product

```
web/            Vue 3 + Vite. One build, four containers:
                browser · PWA · Android shell · iOS shell
     |
     |  REST + JSON
     v
backend/        Spring Boot + PostgreSQL
                timetable · rooms · availability · room updates · audit · accounts
     |
     v
mock-school-api/ a simulated school feed (the real endpoint is not connected)
```

The backend answers **where can I be**. That is one of the three things a student does not know,
and it is the easiest. The plan lives in the client, which is why the corridor case works with no
signal at all, and why a second device cannot see your plan yet. Both consequences are honest.

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

No build step wanted? Open [`product/demo/index.html`](product/demo/index.html). One file, no
server, works offline, switches between English, 简体 and 繁體 in the header.

---

## What it will never do

1. Ask you a question it could have answered by watching.
2. Show you an empty block and call it a plan.
3. Move three blocks when one would have done, without telling you.
4. Pretend a room is yours when it is not.
5. Pass judgement on whether you went to class.

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
illustrative, not a measured result. Authentication is prototype grade. Nothing here is a claim it
cannot back.

---

## Repo map

```
web/               the interface: Vue 3 + Vite, one codebase for phone and desktop
backend/           Spring Boot service: timetable, availability, accounts, audit
frontend/          earlier desktop client, kept because the availability work lives here
mobile/            earlier phone client, same reason
mock-school-api/   simulated school timetable feed, with its design log
docs/              twelve documents, three languages each
product/           prototype, screenshots, one page PDF
scripts/           start / stop / test
```

---

<p align="center"><sub>
COMP1110 Group 08 · The University of Hong Kong · LIU Haoran (u3686264)<br>
The name is a pun on a paper about attention. The paper is about what to attend to. So is this.
</sub></p>
