# NextStop

**English** · [简体中文](README.zh-CN.md) · [繁體中文](README.zh-TW.md)

> Following It Is All You Need.

A student walks out of a lecture with seventy minutes in front of them. The campus is full of
rooms. What they do not know is **where to go**. NextStop decides that for them, and keeps the
decision standing when the day moves.

COMP1110 Group 08 · HKU · built by **LIU Haoran (u3686264)**

<img src="product/web-day.png" width="330" alt="One card, a stack of blocks, seams of walking time between them">

---

## What it is

| | |
|---|---|
| **It answers** | what to do with the next hour, and where |
| **It does not** | hand you a list and let you choose |
| **You do** | open it once a day, then follow it |
| **It is not** | a room finder, and not a calendar |

A room finder tells you which rooms are empty. A calendar stores what you have already decided.
Neither answers the question a student actually has in a corridor, which is *what now*.

Three things a student does not know between two classes: **where they can go, where they are
allowed to go, what to do**. The room is one small step inside that.

📄 **[One page PDF](product/NextStop.pdf)** · 🧭 **[Why this exists](docs/why.md)** · ✨ **[What it does that others do not](docs/highlights.md)**

---

## The interface

One card. Below it, the day as a stack of blocks. Between the blocks, only the walking time.
Nothing is ever empty: a gap defaults to study.

<img src="product/web-desktop.png" width="640" alt="On a desktop the same interface sits in a phone frame, because it is the same interface">

Three tabs, three jobs, no toolbar:

| Tab | Job |
|---|---|
| **Day** | one card, a stack of blocks, four actions |
| **Plan** | the week as five columns, one day below it, one sentence to change it |
| **Me** | the numbers it learned, editable |

Deeper: **[screens](docs/screens.md)** (block specification, action matrix, tokens) and
**[features](docs/features.md)** (every feature by user journey).

---

## How it decides

```
1  Anchors     the timetable. Fixed, authoritative, never questioned.
2  Free time   gaps between commitments, minus walking and a buffer.
3  Fill        what goes in each gap, and where, from the availability layer.
4  Repair      when reality deviates, change as little as possible.
```

The hard part is not deciding a day. It is deciding **how little to change** when the day moves.
Two stops are the same stop only if the purpose, the building and the start time still line up.
Repair then climbs a cost order: move a few minutes, change room in the same building, change
building, drop a stop, and only then touch attendance, which is the user's decision.

> A finder answers a set. A plan answers a sequence.

**[algorithm.md](docs/algorithm.md)** has the criterion, the procedure, the complexity and one
worked example.

---

## Architecture in one screen

```
web/            Vue 3 + Vite, one build, four containers
                browser · PWA · Android shell · iOS shell
     |
     |  REST + JSON  (/api, proxied in dev)
     v
backend/        Spring Boot 4.1.1, Java 21, PostgreSQL 17
                timetable · rooms · availability · room updates · audit · accounts
     |
     v
mock-school-api/ simulated school feed (the real endpoint is not connected)
```

The backend stopped being the product. It is the layer that answers *where can I be*, and it
exists so the plan never points at a place that does not exist. The plan lives in the client,
which is why the corridor case works with no signal.

**[architecture.md](docs/architecture.md)** · decisions in **[adr/](docs/adr/)**
(0001 data layer, 0002 plan in the client, 0003 repair instead of recompute, 0004 one UI four
targets)

---

## Run it

```bash
git clone https://github.com/HenryLiu520/nextstop
cd nextstop

# data layer
./scripts/start-all.sh          # PostgreSQL 5433 → Spring Boot 8080 → first client 5173

# the interface
cd web && npm install && npm run dev     # http://127.0.0.1:5175
```

`web/` proxies `/api` to 8080, so the day you see is built from real availability answers. The
planning rules behind it are still a stub, which the interface does not hide.

There is also a **single file prototype** with no build step: open
[`product/demo/index.html`](product/demo/index.html), or use the hosted copy at
<https://henryliu520.github.io/nextstop/>. English, 简体, 繁體 switch in the header.

---

## What is built, and what is not

| Part | State |
|---|---|
| Availability engine, rules, roles, audit, 27 tests | **built and tested** |
| Web interface, three tabs, three languages, real availability | **built** |
| Single file prototype, clickable end to end | **built** |
| Planner and repair ladder | **specified, not implemented** |
| Learning layer | **specified, not implemented** |
| PWA manifest, Android and iOS shells | **chosen, not built** |

Read **[features.md](docs/features.md)** for the same table per feature, and
[architecture.md section 8](docs/architecture.md) for what is missing and why.

---

## Repo map

```
web/               the interface: Vue 3 + Vite, one codebase for phone and desktop
backend/           Spring Boot service: timetable, availability, accounts, audit
frontend/          earlier desktop client, kept because the availability work lives here
mobile/            earlier phone client, same reason
mock-school-api/   simulated school timetable feed, with its design log
docs/              start at docs/README.md
product/           prototype, screenshots, one page PDF
scripts/           start / stop / test
```

Old clients are not showcased. New interface work happens in `web/`.

---

## Design rules we hold to

1. **Never ask what the user would not have done anyway.** No forms, no ratings, no "was this
   helpful". Everything learned comes from actions taken for the user's own reasons.
2. **The timetable is authoritative.** We do not model it as something that might be wrong.
3. **No empty time.** Gaps are filled by default, because a planner that permits "nothing"
   degrades into a copy of the timetable.
4. **Actions belong to blocks, not to pages.** No toolbar.
5. **Every change is reversible**, including a skipped class.

---

## What we do not claim

- The timetable is sample data. `V2__seed.sql` is a plausible mock and the University endpoint is
  not connected.
- "Free" is not "empty". The system answers *is this room taken*, never *how many people are in it*.
- The one-block-versus-five-block comparison is illustrative, not a measured result.
- Authentication is prototype grade. Tokens in memory, salted SHA-256, no HTTPS.
- The learning layer exists as a specification. The interface uses fixed defaults.

---

## Licence and credits

Coursework, not distributed. Third party components are used under permissive licences (MIT for
the front end stack, Apache-2.0 for Spring Boot and Flyway, the PostgreSQL licence, BSD-2 for the
JDBC driver) and credited in [THIRD-PARTY.md](THIRD-PARTY.md). No code is copied from the
[vue3-element-admin](https://github.com/youlaitech/vue3-element-admin) template (MIT) that inspired
the earlier client layout. The header uses the University shield, wordmark and HKU Green (#024638)
only to identify this coursework prototype.
