# Core logic and highlights

The short version of what this project argues, and what it does that the obvious alternatives do not.
Written down so the argument survives being summarised.

---

## Part 1 · The core logic

### 1. The subject is a confused student, not a room

A student leaves a lecture and has to decide something. What they do not know is **where they can
go, and what to do there**. "Which room is empty" is one small part of that, and the easiest part.

Every existing tool answers that part and hands the rest back.

### 2. A day is a sequence, not a query

A room finder returns a **set**: the rooms free at a moment. A day is a **sequence**: what you do
now changes what is possible next, and the choice you make at 09:47 is made without seeing 14:00.

So a per-query answer cannot, even in principle, produce a good day. It can only produce a good
moment.

> A finder answers a set. A plan answers a sequence.

### 3. The plan starts when the day starts, and only once

There is no "start time" to configure. The first time the student opens it that day, the system
takes one location read and plans from *now* to the last commitment. After that the day is
read-only until something deviates.

This matters because a plan computed from a nominal 08:00 is already out of date by the time
anyone looks at it.

### 4. Everything must be minimal-change

When the day moves — a class overruns, a friend appears, a lecture is skipped — the system changes
as little as it can. Two stops count as *the same thing* only if the purpose, the building (±
walking tolerance) and the start time (±15 min) are all unchanged. Everything else is left alone.

Repair climbs a cost order: move 5–10 minutes · change room in the same building · change building ·
drop a stop · and only then touch attendance, which is the student's decision and never the system's.

**A plan that rewrites itself every time something moves is indistinguishable from no plan.**

### 5. Nothing is asked that the user would not have done anyway

No forms, no ratings, no "was this helpful", no "what time do you usually leave". Everything the
system learns comes from actions taken for the student's own reasons: opening the day, changing a
block, skipping a class, ending the day early.

### 6. No empty time

Gaps are filled by default — with study, unless something better is known. A planner that permits
"nothing" degrades into a copy of the timetable, which is the thing we were trying to get away from.

### 7. Actions belong to blocks, not to pages

There is no toolbar. A class offers *skip*; a meal offers *change*; a meeting offers *book*. The
screen is one card and a stack of blocks, and the bottom bar only switches between three views.

---

## Part 2 · The highlights

### It gets smarter the more you use it

Four things are learned, all from actions the student produces anyway:

| Learned | From | Becomes |
|---|---|---|
| how fast you actually walk | the gap when you say you are late | earlier departures (×1.00 → ×1.28) |
| when your day usually ends | ending the day | Thursday's default end, 17:00 → 20:00 |
| which places you actually go | which option you pick when you change one | ranked first, marked *you go often* |
| whether you attend a given class | skipping it | that hour is treated as free |

Two rules keep it honest: **≥3 observations before a parameter replaces its default**, and
**what is learned is always a default, never a control taken away** — the buttons stay.

### It is almost effortless

One gesture a day. After that, the number of actions needed to follow a plan goes **down** over
time, not up: three taps in the first version, two, then one, then none. Features may keep growing
inside; what reaches the screen stays *one card, one time, one destination*.

### It never speaks first

No push notifications, no chat bubbles, no "need anything?". The system is silent unless the user
opens it or something has genuinely changed. When it does have to speak, it says what changed and
offers to undo it — nothing else.

### Saying one sentence changes the plan

"Make tomorrow afternoon study time, back in the lab by 3." The system shows what it would do,
applies it, and can reverse it. Natural language is an *interface to the same actions the buttons
perform* — never a privileged path that can do things the interface cannot.

### The difference from a calendar, in one line

A calendar **stores** what you already decided. This **decides**, and keeps the decision alive.
A calendar entry never changes on its own; a plan here is expected to, and is judged on how little.

### The one thing it will not do

It will not pretend to allocate a room it cannot allocate. For ordinary classrooms it claims a seat
for a small group and can retract it; for anything that needs a real booking, it says when to book,
books it if allowed, and reflects the outcome as a fact on the plan.

---

## The boundary conditions we hold

1. The timetable is authoritative. We never model it as something that might be wrong.
2. Location is read once, at the moment the day is revealed. No tracking.
3. Skipping a class is local to the student: the class still runs, the room is still taken.
4. Nothing about attendance is uploaded, aggregated, or used to rank anyone.
5. Every change is reversible, including a skipped class.
