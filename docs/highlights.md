# NextStop: four highlights, and how each one is built

**English** · [简体中文](highlights.zh-CN.md) · [繁體中文](highlights.zh-TW.md)

> A highlight is only worth keeping if you can point at the mechanism.

[`why.md`](why.md) argues that the problem a student has is a missing decision rather than a
missing room. [`algorithm.md`](algorithm.md) specifies the planning and repair machinery that
produces the decision, and [`why.md`](why.md) states the same four
highlights in one page. This document sits between them. It takes the four things the project
thinks are worth remembering, and for each one it states the mechanism that produces it, because
a highlight that cannot be described as a mechanism is a slogan.

The four are not in order of importance, and the first one is the one still owed (see
[What is not built](#what-is-not-built)).

## 1. It gets smarter the more you use it

The system learns four quantities. Each is small, each is about this one student rather than about
the campus, and each is readable from an action the student performs for reasons of their own. None
of it is ever collected through a form.

| Quantity | Read from | Becomes | Designed direction |
|---|---|---|---|
| walking speed | the gap between the plan and the moment "I am late" is tapped | a multiplier on the walk matrix | ×1.00 → ×1.28 |
| when the day usually ends | "End the day" | that weekday's default end time | 17:00 → 20:00 |
| where they actually eat | which option is picked when a meal is changed | a ranking hint, marked as often visited | one venue promoted |
| whether a class is attended | "Not going" on that class | that hour treated as free, for this student only | a recurring skip |

### How it is achieved

**One parameter and one counter, per quantity.** The walk factor is a scalar defaulting to 1.00,
the weekday end time is a clock value, the venue hint is a weight per place, the attendance value
is a rate per course. Beside each one sits an integer counting how many observations have landed
on it. There is no model file, no training run, and no inference call at plan time. Planning reads
a number in exactly the way it reads a three minute buffer.

**Three observations before a default is replaced.** While the counter is under three, the default
stands and the interface says so, in the form of "still learning you" next to the value. At the
third observation the parameter takes the observed value, smoothed so that one odd day does not
become a rule. The gate is the entire anti-flap mechanism. A student who is late once because the
lift was out does not acquire a slower walk for the rest of the term.

**Only the parameter changes, never the algorithm.** Learning adds no rules, reorders no rung of
the cost ladder, and cannot introduce an action. A learned walk factor of 1.28 multiplies every
entry of the walk matrix, which is exactly the input `W` in section 1 of
[`algorithm.md`](algorithm.md). The planner is untouched. That is also what makes the learned
value auditable: to explain why the student was told to leave at 10:47 rather than 10:52, read one
number and multiply.

**The value is visible, and it is editable.** The Me screen lists each learned value with its
counter, in the units the planner uses: `walk × 1.00`, `Thursdays usually end 20:00`, `usual lunch
spot`. Any of them can be typed over, and saving writes the parameter and resets the counter, so
the next three observations refine the value the student chose instead of arguing with it. The
attendance value is the exception: it stays on the device, is never aggregated, and is not shown as
a number, because a skip is a fact about the student and not a statistic about them.

**A parameter change is observable.** The move from ×1.00 to ×1.28 shows up as departures a few
minutes earlier, and those minutes appear on the plan where the student can see them. The rule
held throughout is that learning costs the student nothing they were not already doing. It never
asks how fast they walk. It watches what happens when they say they are late.

## 2. It is almost effortless

The design target is one gesture per day and nothing after it. The number that matters is not taps
per screen but operations per day: how many things a student has to do in order to follow a plan
they did not write.

| Version | Operations to follow one day | What removed the work |
|---|---|---|
| first | 3 | pick a room, pick a time, confirm the day |
| second | 2 | the time is derived from the gap, so only the room is picked |
| third | 1 | the reveal computes the day, so the work is opening it |
| target | 0 | the day is already right when it is read, and reading is not an operation |

The claim is directional. The count may only fall as capability is added, so any feature has to be
paid for inside the block that already exists, not with a new screen or a new question.

### How it is achieved

Three mechanisms, each removing a different kind of waiting.

**All of the arithmetic is local.** The plan layer is client code, as [`architecture.md`](architecture.md)
argues, and the plan itself lives nowhere else. Gaps, walk scaling, stop placement and the identity
test run in the tab or on the phone, against availability answers already fetched as a window. No
part of planning waits on a round trip.

**Planning happens once, at reveal, and the plan is read-only afterwards.** Fill runs at reveal and
the day is published (section 2 of [`algorithm.md`](algorithm.md)). Opening the app a second time
renders the same plan from memory rather than re-solving it, so the afternoon cannot churn while
the student is looking at it.

**Recalculation is triggered only by deviation, and is bounded to the broken window.** Of the four
states, only `repairing` writes to a published plan. Repair keeps every stop whose purpose,
building and start time still hold, re-solves inside the window from the first broken stop to the
last, then checks the neighbour at the seam so the fix does not spill (section 4 of
[`algorithm.md`](algorithm.md)). The full replan exists as a rare fallback and as the comparison
switch, and it is not on the everyday path.

Between them these three mean the interface does not grow as the system grows. Features accumulate
inside the plan layer. What reaches the screen stays one card, one time, one destination.

## 3. It never speaks first

There is no push notification, no chat bubble, no confirmation dialog, and no "need anything?". The
system produces no unsolicited text at all, and it never asks the student to approve its own
decisions.

### How it is achieved

The surface is four actions and nothing else.

| Action | Belongs to | What it does | Undo |
|---|---|---|---|
| Reveal | the day | plans from now to the last commitment, once | not needed, it only writes the first plan |
| Change | one stop | moves that stop only, never the day | one tap, restores the previous stop |
| Skip | one class | retracts the student's attendance, the class still runs | one tap, attendance returns |
| End the day | the day | closes the plan at the current stop | one tap, the remaining stops return |

**Every system utterance has one of those four behind it.** A message is a description of what an
action just did, followed by an undo. There is no second channel, and no code path can emit text
without a state transition underneath it. This is a structural property rather than a discipline,
which is why it survives feature work.

**Silence accumulates nothing.** If the student closes the app for six hours, nothing queues up in
their absence, because nothing was produced in their absence. There is no notification permission
to grant, no digest to unsubscribe from, and no badge counting what was missed.

**The one sentence allowed to arrive unasked is a repair notice.** When repair changes something it
says what changed, what stayed, and offers the undo. It is permitted because a plan that moves
silently is worse than a plan that announces the move, and it is bounded because it can only be
triggered by the student's own report of a deviation. The system's first word is always a reply.

## 4. One sentence changes the plan

"Make tomorrow afternoon study time, back in the lab by 3" is a supported input. The rule that
keeps it honest is that language is a second door onto the same set of buttons, never a privileged
channel that can do things the interface cannot.

### How it is achieved

**Language can only call actions that already exist.** A sentence resolves to a sequence drawn from
the four actions in section 3, where "Change" is the one that covers the edit vocabulary of a stop
(move the edge, move it earlier or later, change the room, change the building). If a request cannot
be expressed that way, it fails rather than inventing a capability.

| Sentence | Resolves to | Button that does the same |
|---|---|---|
| "I am running twenty minutes late" | a `delay` deviation | "I am late" |
| "move lunch a bit later" | change, on the meal stop | "Change" on the meal |
| "not going to the 11:00" | skip, on that anchor | "Not going" on the class |
| "I am done for today" | end the day | "End the day" |

**Every action stays reachable by hand.** Removing the sentence box would leave a complete app, not
a crippled one. That is the test applied to each new capability before it is exposed to language:
if only the sentence can reach it, the capability has a hole in it and the button gets built first.

**A sentence never applies on its own.** It produces a preview of the change before anything is
committed: the block affected, the new end time, and the line that matters most, that everything
else is untouched. Applying writes the same undo record a button press would, so an
over-interpreted sentence costs one tap rather than an afternoon.

**A sentence that maps to nothing says so and changes nothing.** A silent no-op would teach the
student that the box is unreliable, so a failed parse reports the failure and leaves the plan
exactly as it was.

## What is not built

The learning layer in section 1 is a design and not running code. The planner behind the plan layer
is a stub: it reads the anchors, computes gap durations, and places one fixed study block per
usable interval. It does not read observations, does not maintain counters, and does not replace a
default. The prototype therefore runs on fixed defaults, `walk × 1.00` and a hard-coded weekday
end time, and displays "still learning you" because it is in fact still not learning.

That has consequences for the rest of this document, and they are worth stating rather than
burying.

- The numbers in section 1 (×1.00 to ×1.28, 17:00 to 20:00) describe the direction a parameter is
  designed to move. They are not values measured from use.
- The operation counts 3, 2, 1 and 0 are a design sequence. Operation count appears in section 1 of
  [`algorithm.md`](algorithm.md) as a metric, and its status there is "not instrumented", which is
  still true.
- The two tolerance parameters that gate repair stay at their defaults. Learning them is outside
  the four quantities above and outside the prototype.
- The preview in section 4 exists in the prototype for one worked sentence. It is a demonstration
  of the shape, not a parser.
- Nothing here is an A/B result. The claim that the four mechanisms produce these four properties
  is an argument from construction, and each one is falsifiable by pointing at the code that
  should implement it.

What is real: the state machine and its deviation events, the walk matrix, the availability engine
over the timetable, the gap arithmetic, and the four actions with their undo records. The first
highlight is the one that is still only a design.
