<h1 align="center">Following It Is All You Need.</h1>

<p align="center">
  <b>NextStop</b><br>
  You have 73 minutes and no plan.<br>
  It writes one, then defends it when the day moves.
</p>

<p align="center">
  <a href="https://henryliu520.github.io/nextstop/"><b>▶ Try the prototype</b></a> &nbsp;·&nbsp;
  <a href="docs/why.md">Why it exists</a> &nbsp;·&nbsp;
  <a href="docs/highlights.md">What makes it different</a> &nbsp;·&nbsp;
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
  <img alt="planner still a stub" src="https://img.shields.io/badge/planner-still%20a%20stub-C8452D">
</p>

<p align="center">
  <img src="product/web-day.png" width="230" alt="One card, a stack of blocks">
</p>

<p align="center"><sub>One card, one time, one destination. The rest of the day is underneath.</sub></p>

---

It is 09:47 on a Thursday. A lecture has just ended in KB. The next one is in CPD at 11:00.

You will spend eleven minutes not deciding, then walk to CPD and sit outside the classroom for
another forty, because sitting somewhere you are definitely allowed to sit asks nothing of you.

That hour was not wasted on purpose. It just never got decided.

NextStop decides it.

---

## Who this keeps happening to

- The **international student** who moved countries and still cannot work out where to sit for ninety minutes.
- The **first-year after gaokao**, now that nobody is telling them where to go and grind.
- The one in the **lift lobby**, eleven minutes into a decision about which floor to press.
- The one **eating lunch at 15:40**, because at 12:00 the decision was too big.
- **Five people who all say "wherever you want"**, then spend twenty minutes not going anywhere.

All five own a phone that knows exactly which rooms are empty.

---

## Four things it does that the usual tools do not

| | | |
|---|---|---|
| **It decides, instead of listing** | A finder answers a set, a plan answers a sequence | [why](docs/why.md) |
| **It gets better the longer you use it** | Learns how fast you really walk, when your day really ends, which places you actually go. Three observations before a parameter replaces its default | [highlights](docs/highlights.md) |
| **It is almost effortless** | One gesture a day, then follow it. The number of actions needed goes down over time, not up | [screens](docs/screens.md) |
| **It never speaks first** | No push, no chat bubble, no "need anything?". When something changes it says what changed and offers undo | [highlights](docs/highlights.md) |

What is learned is always a default, never a control taken away. The buttons stay.

---

## What it looks like

<p align="center">
  <img src="product/day.png" width="240" alt="Day">
  <img src="product/repair.png" width="240" alt="After a 20 minute overrun">
  <img src="product/plan.png" width="240" alt="Plan tab">
</p>

<p align="center"><sub>Left: the day as a stack. Middle: a 20 minute overrun. Right: the week, one day, one sentence.</sub></p>

A class offers skip, a meal offers change, a meeting offers booking. Nothing is ever empty, a gap
defaults to study.

---

## When the day moves

A class overruns by twenty minutes. Two ways to answer:

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

Five blocks moving is how a plan loses your trust. So repair asks a narrower question than
"what is the best new day". It asks which stops are actually broken, and touches only those. Two
stops count as the same stop when the purpose, the building and the start time all still line up.

👉 **[algorithm.md](docs/algorithm.md)** for the criterion, the procedure and the cost order.

---

## How it is built

```
web/     Vue 3 + Vite, one build, four containers
         browser · PWA · Android shell · iOS shell
   |
   |  REST + JSON
   v
backend  Spring Boot + PostgreSQL
         timetable · rooms · availability · audit · accounts
```

The planning, the repair and the interface all run in the client, which is why the corridor case
works with no signal. The service underneath was built earlier in the project and answers one
question well, **which rooms are free**, so the plan never points at a place that does not exist.

---

## Documentation, in three languages

| | English | 简体 | 繁體 |
|---|---|---|---|
| **Why this exists** | [`why.md`](docs/why.md) | [`zh-CN`](docs/why.zh-CN.md) | [`zh-TW`](docs/why.zh-TW.md) |
| **What makes it different** | [`highlights.md`](docs/highlights.md) | [`zh-CN`](docs/highlights.zh-CN.md) | [`zh-TW`](docs/highlights.zh-TW.md) |
| **Every feature** | [`features.md`](docs/features.md) | [`zh-CN`](docs/features.zh-CN.md) | [`zh-TW`](docs/features.zh-TW.md) |
| **Architecture** | [`architecture.md`](docs/architecture.md) | [`zh-CN`](docs/architecture.zh-CN.md) | [`zh-TW`](docs/architecture.zh-TW.md) |
| **The repair criterion** | [`algorithm.md`](docs/algorithm.md) | [`zh-CN`](docs/algorithm.zh-CN.md) | [`zh-TW`](docs/algorithm.zh-TW.md) |
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

`web/` proxies `/api` to 8080, so the day on screen is built from real availability answers.

No build step? Open [`product/demo/index.html`](product/demo/index.html). One file, no server,
works offline, three languages in the header.

---

## What is real and what is not

| Part | State |
|---|---|
| Availability engine, rules, roles, audit, 27 tests | **built and tested** |
| Web interface, three tabs, three languages, real availability | **built** |
| Single file prototype, clickable end to end | **built** |
| The planner and the repair ladder | **specified, not implemented** |
| The learning layer | **specified, not implemented** |
| PWA manifest, Android and iOS shells | **chosen, not built** |

The timetable is sample data. "Free" is not "empty". The one-block-versus-five-block comparison is
illustrative, not measured. Authentication is prototype grade.

---

<p align="center"><sub>
COMP1110 Group 08 · The University of Hong Kong · LIU Haoran (u3686264)<br>
The name is a pun on a paper about attention. That paper is about what to attend to. So is this.
</sub></p>
