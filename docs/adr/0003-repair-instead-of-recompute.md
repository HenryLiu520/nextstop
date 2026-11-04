# 0003. Repair instead of recompute

**English** · [简体中文](0003-repair-instead-of-recompute.zh-CN.md) · [繁體中文](0003-repair-instead-of-recompute.zh-TW.md)

- Status: Accepted
- Date: 2026-11-04
- Scope: plan layer, `docs/algorithm.md`, `product/demo/index.html`

## Context

A day never runs as planned. A class overruns by twenty minutes, a friend appears in the gap, a
lecture is skipped. Each event invalidates part of the plan and leaves the rest intact.

There are two ways to respond. Recompute the rest of the day from the deviation, which is simple
and always correct. Or repair the broken part and leave everything else alone.

Recompute re-derives every gap. It re-ranks rooms and re-derives durations where nothing was
wrong, so its output differs from the committed plan at random. Each difference costs the student
a re-read of their day.

That cost is the whole product. A student who remembers "10:00, CSIT 105" and finds it still
there has received something a better plan does not provide. A plan that rewrites itself every
time a class overruns is indistinguishable from having no plan at all.

The quantity that matters is changed stops per repair, not latency. Six stops against a hundred
candidate rooms is a few hundred feasibility checks either way. Nobody notices the milliseconds.
Everybody notices five blocks moving.

## Decision

On deviation, repair. The system keeps every stop that still holds, re-solves inside the broken
window only, then checks the neighbouring stop so the fix cannot spill past the window.

A candidate stop is the same thing as the committed stop only if all three hold. Purpose is
unchanged, which means the same course and the same people. Place is the same building or within
`building_tolerance` walk minutes, default 4. Start is within `time_tolerance`, default 15.

All three, not any two. Purpose alone lets the day slide. Place alone lets a study block become a
lunch. Time alone lets the student end up in another building and be told nothing changed.

The reference is always the committed plan the student saw, never the previous internal
candidate. Comparing against the last candidate would allow slow drift, fourteen minutes per
repair, each reporting no change, an hour gone by lunchtime.

Repair climbs a cost ladder and stops at the first rank that yields a feasible window. The order
is lexicographic, not a weighted sum:

| Rank | Action | Who may trigger |
|---|---|---|
| 1 | Shift an edge by 5 to 10 minutes | system |
| 2 | Change room in the same building | system |
| 3 | Change building | system |
| 4 | Cancel the stop and merge its time | system |
| 5 | Change attendance | user only |

Anchors are walls of the window, never members. The window expands at most twice. After that the
remaining day is replanned in full, and that fallback is counted and reported separately.

Every repair writes an undo record, so a bad repair costs one tap rather than a day.

## Consequences

A typical deviation changes one or two stops out of six. The other four are untouched, and the
student's memory of the day survives the interruption.

The plan is stable but not optimal. An established stop is never re-optimised, even when a
strictly better room exists. A noisy room stays while it still counts as the same thing, and the
remedy is the student's own "change this one" action.

Cost is bounded by the window, `O(k * m * w)` against `O(n * m)` for a full plan. With typical
values that is a factor of two, not an order of magnitude.

The two tolerances become the most load bearing parameters in the system. Too loose and the plan
drifts without anyone noticing. Too tight and an eight minute delay re-solves the afternoon. Both
have to be swept rather than fixed.

There is a consistency constraint between the ladder and the criterion. Rank 1 shifts a stop by
at most 10 minutes, so `time_tolerance` must exceed 10. Below that, the cheapest action the
planner can take contradicts the test that decides what changed.

The worst case is not better than recompute. A deviation at the first stop of the day puts most
of the remaining stops inside the window, and the factor approaches one.

Cascades are real. The neighbour check catches one step, the expansion loop catches two, and
after that the day is replanned. Each fallback is a failure of the minimality claim, so it is
counted separately instead of being averaged away.

The comparison itself is specified and not built. Changed stops per repair against full
recompute is a hypothesis until the replay switch exists.

## Alternatives considered

**Full recompute on every deviation.** Rejected. It is simpler and it destroys the one property
the product claims. The student also learns that the plan is not worth reading.

**Freeze the plan and let the student fix it.** Rejected. It moves the arithmetic into the
corridor, at the moment the student has the least attention to spare.

**A weighted cost function instead of a lexicographic ladder.** Rejected. Three cheap moves would
outvote one expensive move, and the student experiences the expensive one. Weighting also adds a
tuning parameter with no natural value.

**Global re-optimisation with a stability penalty.** Keep the whole day in the objective and
punish movement. Rejected. It reaches a similar result with more moving parts, and the penalty
weight is another knob that has to be justified.

**A hysteresis rule on the whole plan.** Only replan when the deviation exceeds a threshold.
Rejected. It answers when to act, not what to change, and a large deviation still triggers a full
rewrite at the worst possible moment.
