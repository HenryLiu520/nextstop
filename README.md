# NextStop

**English** · [简体中文](README.zh-CN.md) · [繁體中文](README.zh-TW.md)

> A student walks out of a lecture with seventy minutes in front of them. The campus is full of
> rooms. What they do not know is **where to go**. NextStop decides that for them, and keeps the
> decision standing when the day moves.

COMP1110 Group 08 · HKU · built by **LIU Haoran (u3686264)**

<img src="product/day.png" width="330" alt="The day as a stack of blocks: 09:00 class, 11:00 study, 12:10 lunch, 14:00 class, 16:00 meeting, 17:00 study">

---

## What it is

| | |
|---|---|
| **It answers** | what to do with the next hour, and where |
| **It does not** | hand you a list and let you choose |
| **You do** | open it once a day, and follow it |
| **It is not** | a room finder, and not a calendar |

A room finder tells you which rooms are empty. A calendar stores what you have already decided.
Neither answers the question a student actually has, standing in a corridor: *what now?*

---

## The problem, stated properly

Between two classes a student does not know three things:

**where to go · where they are allowed to go · what to do**

"Which room is empty" is a small part of the middle one, and the easiest part. Every existing tool
answers that part and hands the other two back.

So the question here is one level up from the one we started with. Not *which room*, but *where
should this person be, and when should they move*.

---

## How it decides

Four layers, each checkable on its own. That separability is what makes the thing buildable.

```
1  Anchors     the timetable. Fixed, authoritative, never questioned.
2  Free time   gaps between commitments, minus walking and a buffer.
3  Fill        what goes in each gap, and where, from the availability layer.
4  Repair      when reality deviates, change as little as possible.
```

The user makes one gesture a day: reveal the day. After that the plan is read-only until something
deviates — a class overruns, a friend appears, a lecture is skipped.

### The part that is actually hard

Not deciding a day. Deciding **how little to change** when the day moves.

Two stops count as *the same thing* only if the purpose is unchanged, the place is the same
building (or within walking tolerance), and the start time is within ±15 minutes. On that criterion
the system repairs the broken window and leaves everything else alone, because a plan that rewrites
itself every time a class overruns is indistinguishable from having no plan at all.

Repair has a cost order, and it climbs it in this sequence: move a stop 5–10 minutes · change room
in the same building · change building · drop a stop · and only then touch attendance, which is the
user's decision and never the system's.

<img src="product/repair.png" width="330" alt="After a 20-minute overrun: one block changed, the rest untouched">

> A finder answers a set. A plan answers a sequence.

---

## What exists today

| Part | State | Where |
|---|---|---|
| Concept, feature map, screen plan | written | [`product/`](product/) |
| Clickable prototype — one day, three tabs | **runs in a browser** | [`product/demo/index.html`](product/demo/index.html) |
| Availability engine — free rooms derived from the timetable | **built and tested** | [`backend/`](backend/) · [`mock-school-api/`](mock-school-api/) |
| Two clients (desktop + phone) on one service | built | [`frontend/`](frontend/) · [`mobile/`](mobile/) |
| Repair criterion and its cost order | designed; not yet measured | report, in progress |

The availability engine is not the product. It answers *where can I be*, and its job in this design
is to keep the plan from pointing at a place that does not exist.

---

## Try the prototype

```bash
open product/demo/index.html       # no build, no server, no install
```

Three tabs: **day · plan · mine**. Everything is clickable — swap a meal, book a meeting room, skip
a class, move a block, add something by hand. Every change can be undone.

The one worth trying: open the day, click **"something changed?"** top right, pick *the class ran 20
minutes late*, and watch it change **one** block rather than five. The link in the toast shows what
full recomputation would have done to the same day.

The prototype is a design instrument, not an implementation. The data is a fixed day and the
planner behind it is a stub. What is real is the state machine: every action changes the plan,
propagates only as far as it must, and can be reversed.

<img src="product/plan.png" width="330" alt="The plan tab: the week as five columns, the selected day below, free slots tappable">

---

## Run the whole system

The two-client application behind the availability layer is fully runnable.

```bash
git clone https://github.com/HenryLiu520/nextstop
cd nextstop
./scripts/start-all.sh     # PostgreSQL 5433 → Spring Boot 8080 → Vite 5173
```

First run installs dependencies and applies the Flyway migrations — allow 3–8 minutes. Then open
<http://localhost:5173/login>. Seeded accounts cover all three roles; passwords are in the Flyway
migrations under `backend/src/main/resources/db/migration/`.

Docker is supported too: **[docs/DEPLOY.md](docs/DEPLOY.md)** ([简体](docs/DEPLOY.zh-CN.md) ·
[繁體](docs/DEPLOY.zh-TW.md)).

```bash
./scripts/test.sh          # 27 automated tests, needs the database
```

---

## Repo map

```
product/           the idea: concept, features, screens, prototype, one-page PDF
backend/           Spring Boot service — timetable, availability, accounts, audit
frontend/          Vue 3 desktop client (5173)
mobile/            Vue 3 phone client (5174)
mock-school-api/   simulated school timetable feed, plus its design log
docs/              deployment, ER diagrams, screenshots, folder conventions
scripts/           start / stop / test
```

Worth opening:

- [`product/concept.md`](product/concept.md) — the idea in one page
- [`product/features.md`](product/features.md) — every feature, and how to see it working
- [`product/screens.md`](product/screens.md) — screen structure, actions, and the rules behind them
- [`docs/ERD.md`](docs/ERD.md) — the data model, and three things it deliberately lacks
- [`docs/DEPLOY.md`](docs/DEPLOY.md) — how to run all of it

---

## Design rules we hold to

1. **Never ask what the user would not have done anyway.** No forms, no ratings, no "was this
   helpful". Everything learned comes from actions taken for the user's own reasons.
2. **The timetable is authoritative.** We do not model it as something that might be wrong.
3. **No empty time.** Gaps are filled by default; a planner that permits "nothing" degrades into a
   copy of the timetable.
4. **Show the number, not the parameter.** One card, one time, one destination.
5. **Every change is reversible** — including a skipped class.

---

## What we do not claim

- **The timetable is sample data.** `V2__seed.sql` is a plausible mock; the University's endpoint
  is not connected. Figures must come from real data before anyone claims them.
- **"Free" is not "empty".** The system answers *is this room taken by a class or a posted change*,
  never *how many people are in it*. Rooms are shared, and the interface says so.
- **The repair comparison is still a design claim.** The one-block-versus-five-block example is
  illustrative, not a measured result.
- **Prototype-grade auth.** Tokens live in memory, passwords use salted SHA-256, no HTTPS.
- **No learning yet.** The self-learning layer is designed and written up, not implemented; the
  prototype uses fixed defaults.

---

## Licence and credits

Coursework, not distributed; no licence file. Third-party components are used under permissive
licences (MIT front-end stack, Apache-2.0 Spring Boot and Flyway, PostgreSQL licence, BSD-2 JDBC
driver) and credited in [THIRD-PARTY.md](THIRD-PARTY.md). No code is copied from the
[vue3-element-admin](https://github.com/youlaitech/vue3-element-admin) template (MIT) that inspired
the layout. The header uses the University's shield, wordmark and HKU Green (#024638) only to
identify this coursework prototype.
