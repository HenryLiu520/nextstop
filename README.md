<h1 align="center">Following It Is All You Need.</h1>

<p align="center">
  <b>NextStop</b><br>
  You have 73 minutes and no plan.<br>
  It writes one, and repairs it when the day moves.
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
  <img src="product/day.png" width="340" alt="One card, a stack of blocks">
</p>

<p align="center"><sub>One card, one time, one destination. The rest of the day sits underneath.</sub></p>

---

It is 09:47 on a Thursday. The lecture in KB just ended, the next one is at 11:00 in CPD, and the
walk between the two buildings takes eleven of the minutes in between.

You will spend a while not deciding. Then you will walk to CPD and sit outside the room for
another forty, because a place you are certainly allowed to sit asks nothing of you.

The hour was not wasted on purpose. It was never decided.

NextStop decides it.

---

## Who this keeps happening to

- The **international student** who landed this term and still cannot work out where to sit for ninety minutes.
- The **first-year after gaokao**, in the first term nobody tells them where to go and grind.
- The one in the **lift lobby**, eleven minutes into choosing a floor.
- The one **eating lunch at 15:40**, because the noon decision was too big.
- The **five who all said "wherever you want"**, then spent twenty minutes going nowhere.

All five own a phone that knows exactly which rooms are empty. 📱

---

## ⚡ It decides for you

A room finder hands you a list, then asks you to fill four boxes it already knows the answer to.
It has your timetable. It knows the gap is 73 minutes and the walk is eleven of them. NextStop
asks nothing. Open it and the answer is already there. One card, one time, one destination.
KB 227, until 12:00, walk 5 min.

The number that matters is operations per day, and it may only fall. The first version needed 3. Pick a room, pick a time, confirm the day. Then the time came to be
derived from the gap, so 2. Then the reveal came to compute the day, so 1, and the only work left
is opening the app. The
target is 0, a day that is already right when you read it. Every new feature pays for itself
inside the card, never with a new screen or a new question.

The arithmetic all runs on the phone, so nothing waits on a round trip in a corridor, and no
signal is not a case it fails. Planning runs once, at reveal, and the plan is read-only
afterwards. Open the app again and it renders the day it already holds, so the afternoon cannot
reshuffle while you are looking at it. Features accumulate inside the plan layer. What reaches
the screen stays one card.

---

## 📈 It gets smarter the longer you use it

NextStop learns four numbers about you, each one read from an action you were doing anyway,
never from a form. Tap "I am late" and the gap between the plan and the tap is how fast you actually walk. Tap "End the day" and that hour is when your day really ends. Pick an option
while changing a meal and the pick is where you actually eat. Tap "Not going" on a class and
the tap is whether you attend it.

Each quantity is one parameter plus one counter. No model file, no training run, no inference
call at plan time. A default stands until three observations land on it, and the interface says
"still learning you" until then. One late day because the lift was out does not make you a slow
walker for the term. Learning never touches the algorithm. A walk factor of 1.28 multiplies the
walk matrix and nothing else, which is why you can audit the planner by reading one number.

Every value sits on the Mine screen next to its counter, in the units the planner uses. It is
visible and it is editable. Type over one and the counter resets, so the next three observations
refine the number you chose instead of arguing with it.

*Status: the values are on Mine and editable today. The part that derives them is specified,
not running.*

---

## 🧠 It is genuinely intelligent

NextStop takes a timetable and one round of preferences, and computes a whole day. Every gap
gets walking time and a buffer subtracted before anything is placed in it. Every gap gets a
purpose and a place, so no gap is ever empty. A meal is not given a guessed duration. It is
given a latest departure time, and two options.

One sentence changes the plan. "Leave tomorrow afternoon for study, back in the lab by three"
resolves into a sequence of actions that already exist, shows you a preview, and applies with
an undo. Language is a second door onto the same buttons, never a privileged channel. A sentence
that cannot be expressed as existing actions fails, and says so, instead of inventing a
capability. Delete the sentence box and what is left is a complete app, which is the test every
capability passes before it reaches language.

When the day slips it repairs rather than recomputes. A 20 minute overrun moves one block,
where a full replan moves five. A stop survives the repair when its purpose, its building and
its start time still line up. Underneath, it weighs several things at once, how far you walk,
how long you wait, whether you have eaten, and how hard the next commitment is.

*Status: the planner behind this is a stub today. The repair ladder and the sentence parser are
specified, not running.*

---

## What it looks like

<p align="center">
  <img src="product/triptych.png" width="620" alt="Day, a 20 minute overrun repaired, and the plan tab">
</p>

<p align="center"><sub>Left: the day as a stack · middle: a 20 minute overrun, repaired, one block moved · right: the week, one day, one sentence.</sub></p>

A class offers Skip, a meal offers Change, a meeting offers Book it. Nothing is ever empty, and
a gap defaults to study.

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

Five blocks moving is how a plan loses your trust. Repair asks a narrower question than "what is
the best new day". It asks which stops are actually broken, touches only those, and counts two
stops as the same stop when the purpose, the building and the start time still line up.

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

Planning, repair and the interface all run in the client, which is why the corridor case works
with no signal. The availability service underneath was built earlier in the project and answers
one question well, which rooms are free, so the plan never points at a place that does not
exist.

### From a timetable to a room

Five steps, in order, every time.

1. **Read.** The timetable arrives from the school feed, read only, and it is taken as correct.
Nothing checks it and nothing questions it.
2. **Sync.** A calendar subscription is merged in. Nothing is written back to it.
3. **Add.** The one-off things you enter yourself, a dentist appointment, a parcel to collect. Same
weight as a class, except that yours can be deleted.
4. **Union.** The three become one list of committed time. Overlaps merge, and so do neighbours
closer than fifteen minutes, because a ten minute hole is not a hole, it is a corridor.
5. **Subtract.** Walking time and a buffer come off every gap, and walking is multiplied by your
own factor, 1.00 until it learns otherwise.

What is left is a set of windows. Each one gets a purpose and a place. A meal gets a latest
departure time rather than a guessed duration, and study fills whatever remains, so no window is
ever empty. Then the room, where the hard filters run first, free for the whole window, enough
seats, you are allowed in, not the building you just left, and a cost picks among the survivors.

```
cost = 1.0 x walk minutes
     + 0.3 x |floor - the floor you prefer|
     + 0.2 x (1 - visits / 10)
     + 0.5 x noise
```

A failed filter ends that candidate. The weights only choose between rooms that already passed.

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
| The Mine screen and its editable values | **built** |
| The planner | **a stub, one study stop per gap** |
| The repair ladder and the identity test | **specified, not implemented** |
| The learning layer | **specified, not implemented** |
| The sentence preview | **one worked sentence in the prototype** |
| PWA manifest, Android and iOS shells | **chosen, not built** |

The timetable is sample data. "Free" is not "empty". The one-block-versus-five-block comparison
is illustrative, not measured. Authentication is prototype grade. The learned numbers are
designed directions, not measurements.

We keep this table instead of a roadmap.

---

<p align="center"><sub>
COMP1110 Group 08 · The University of Hong Kong · LIU Haoran (u3686264)<br>
The name is a pun on a paper about attention.
</sub></p>
