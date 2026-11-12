# NextStop: planning and repair algorithm

**English** · [简体中文](algorithm.zh-CN.md) · [繁體中文](algorithm.zh-TW.md)

How a day is planned from a fixed timetable, and how that plan is repaired when reality moves. This is a design document. The prototype planner is a stub (section 9), so nothing here is a
measured result. The claim it exists to support is one sentence: when something deviates, the system changes the fewest stops it can, and fewer than a full recompute would.

## 1. Problem statement

**Inputs.**

| Symbol | Input | Content |
|---|---|---|
| `A` | anchors | fixed commitments with building, room, start, end |
| `G` | gaps | free intervals derived from `A` |
| `W` | walk matrix | minutes per building pair, scaled by the learned walk factor |
| `R` | availability | `free(room, t0, t1)`, from the existing availability engine |
| `P` | profile | parameters: time tolerance, building tolerance, buffer, walk factor, meal window |
| `L` | location | one reading, at reveal only |
| `D` | deviation | `delay(min)`, `change(stop)`, `skip(anchor)`, `arrive(stop)` |

**Output.** An ordered sequence of stops between the first and last anchor. A stop is `(type, purpose, building, room, start, end)` with one of four types: `study`, `eat`, `meet`, `transit`.
Walking between buildings is a `transit` stop, walking inside one building is no stop at all. There is no `wait` type: every computed gap is filled or merged into a neighbour.

**Constraints.**

1. Anchors are authoritative. The planner never moves, shortens, or questions one.
2. No two stops overlap, and the last stop before an anchor ends early enough to walk there.
3. Travel between buildings is always paid for, as a `transit` stop or inside a gap.
4. A stop occupies a room only if `free(room, t0, t1)` holds for the whole interval.
5. `arrive(t)` is final. A past stop is truncated, never re-planned.
6. Attendance changes belong to the student. The planner never produces one.
7. The committed plan is the reference for every comparison (section 3).

**Success criteria.** Four numbers, all measurable, none measured yet.

| Metric | Definition | Direction | Status |
|---|---|---|---|
| Operation count | gestures needed to follow one day | non-increasing | not instrumented |
| Follow-through ratio | planned stops reaching their start without user replacement | higher | not instrumented |
| Changed stops per repair | stops whose `(purpose, building, start)` moved, per deviation | 1 to 2, below full recompute | not instrumented |
| Delta versus full recompute | changed stops and untouched follow-through, same deviation | repair must win | switch specified, not built |

The fourth is the load-bearing one. It is not a latency number, it is a stability number: does minimal repair leave more of the day alone than re-solving everything. The same deviation must
be replayable through both paths, or the comparison means nothing.

## 2. The four layers

```
+--------------------------------------------------------------------+
| L1  ANCHORS    fixed, authoritative, never questioned              |
|      09:00 COMP1100 CSIT   11:00 MATH1013 KB   13:00 COMP2100 CSIT |
+--------------------------------------------------------------------+
                     | gaps between commitments
                     v
+--------------------------------------------------------------------+
| L2  FREE TIME  gap = next_start - prev_end - walk - buffer         |
|                usable if >= min_stop, else merged, never a wait    |
+--------------------------------------------------------------------+
                     | usable intervals
                     v
+--------------------------------------------------------------------+
| L3  FILL       one stop per interval: study / eat / meet           |
|                transit between buildings, no wait type             |
+--------------------------------------------------------------------+
                     | a deviation is observed
                     v
+--------------------------------------------------------------------+
| L4  REPAIR     keep what still holds, re-solve the broken window,  |
|                then check the neighbouring stop                    |
+--------------------------------------------------------------------+
```

**L1 Anchors.** The timetable is read once and frozen for the day. It is not a preference and not something the planner may correct. If the student is late, the anchor still starts when it
starts: what changed is the student's availability, not the timetable. This immutability is what makes the rest tractable, because every window then has two fixed walls.

**L2 Free time.** Each gap is the interval between one anchor's end and the next anchor's start, minus the walk to the next building and minus a buffer. The remainder is usable only if it
clears the minimum duration for a stop (`min_stop`, default 15 minutes). If it does not, the gap is merged into the adjacent interval rather than producing a stub stop or an empty block.
This layer outputs durations only, so it can be tested on its own.

**L3 Fill.** Each usable interval receives one stop, and the stop's end is derived from whatever follows it rather than chosen freely. Type comes from purpose priority (a meal window that
would otherwise be missed outranks study), place comes from the availability engine plus the walk matrix. Fill runs once per day, at reveal, and the plan is read-only afterwards until a
deviation. This is the layer that makes the output a sequence rather than a set: the stop chosen for 10:00 decides which building the student is standing in at 11:00. Which room fills an interval is a separate procedure with its own document, [`room-scoring.md`](room-scoring.md).

**L4 Repair.** Any deviation enters here. Repair identifies which stops no longer hold against the committed plan, keeps the rest untouched, re-solves inside the broken window only, then
checks the following stop to make sure the fix did not spill past the window. Repair is the only layer that may write to a published plan, and it always writes an undo record.

## 3. The sameness criterion

A candidate stop `s'` is the same thing as the committed stop `s` if and only if all three hold.

1. **Purpose is unchanged.** The same course's study block, the same set of people for a `meet`, eating that is still eating. Purpose is `(type, subject_ref)`: studying COMP1100 in another
   building is the same purpose, studying COMP2100 instead is not.
2. **Place is the same building, or within walking tolerance.** Same building id, or `walk(s'.building, s.building) <= building_tolerance` (default 4 minutes). Another building two minutes
   away counts as the same place. One twelve minutes away does not.
3. **Start time differs by at most the time tolerance.** `abs(s'.start - s.start) <= time_tolerance` (default 15 minutes).

All three, not any two. Purpose alone would let the day slide anywhere. Place alone would let a study block turn into a lunch. Time alone would let the student end up in a different building
and be told that nothing changed.

The reference is always the committed plan, the version the student actually saw. Comparing against the previous internal candidate would allow slow drift: fourteen minutes per repair, every
repair reporting no change, an hour gone by lunchtime.

The criterion exists because repair needs a stopping condition. Without an identity test every deviation has to treat every downstream stop as suspect, and repair collapses into recompute
with extra steps. The criterion is the gate that decides how much work repair does, so its two tolerances are the most load-bearing parameters in the system. They are parameters, not
constants, and section 8 treats them as such.

One deliberate consequence: an established stop is never re-optimised, even when a strictly better placement exists. The plan is not optimal, it is stable. A student who remembers "10:00,
CSIT 105" and finds it still there has received something a better plan does not provide. The price is permanent local suboptimality, accepted knowingly.

## 4. Repair procedure

```text
repair(plan, deviation):
    observe = (t_now, building_now)                # the deviation, as reported
    W       = window(plan, observe)                # first broken stop to last broken stop
    keep    = [s for s in plan if same_thing(s, committed[s])]
    broken  = plan - keep
    if broken is empty: return plan
    for rank in [SHIFT, ROOM, BUILDING, CANCEL]:   # attendance is not a planner rank
        cand = solve(W, keep, rank)
        if feasible(cand): break
    cand = expand_until_stable(W, cand)            # at most 2 expansions
    return commit(cand, undo_log)
```

1. Two values in, one plan out. The call is replayable, which is what makes the comparison in section 1 possible at all.
2. `observe` normalises the event into a time and a building. A 20 minute overrun and an "I am late" tap produce the same observation, so repair has one entry point.
3. `W` is the broken window: from the first stop that can no longer hold to the last one the observation invalidates. Anchors are walls of the window, never members.
4. `keep` applies the three tests from section 3 to every stop, against the committed version.
5. `broken` is the complement. In a typical deviation it has one or two members.
6. No broken stops means the plan still holds, so nothing is written. This case is cheap.
7. The ladder from section 5 is walked in order. Each rank is a separate solve, the first feasible rank wins, and the walk stops there.
8. `solve` enumerates candidate placements inside `W` only. It never sees the rest of the day, which is what bounds its cost.
9. Feasibility is tested before a rank is accepted. An infeasible candidate is discarded and the next rank is tried.
10. The neighbour check asks one question: does the new boundary of `W` push the next kept stop past the time tolerance, or break the walk to it. Cascades are caught at the seam.
11. If it does, the window grows by one stop and is re-solved, twice at most. After that the caller falls back to a full replan of the remaining day, which is honest and rare.
12. `commit` publishes the plan and appends an undo record. A bad repair costs one tap, not a day.

## 5. Cost order

Repair climbs this ladder and stops at the first rank that yields a feasible window. The order is lexicographic, not a weighted sum. A weighted sum would let three cheap moves outvote one
expensive one, and the student experiences the expensive one as the change.

| Rank | Action | What the student notices | Who may trigger | Undoable |
|---|---|---|---|---|
| 1 | Shift an edge by 5 to 10 minutes | same stop, same room, minutes moved | system | yes |
| 2 | Change room, same building | same building and time, different room number | system | yes |
| 3 | Change building | a different building and a longer walk | system | yes |
| 4 | Cancel the whole stop | one stop disappears, its time is merged | system | yes |
| 5 | Change attendance | the class is treated as free, for this student only | user only | yes |

A shift forced by the deviation is not charged to the planner. If the student is 20 minutes late, the stop's start moves because the student moved, not because the planner chose to. The
ladder prices what the planner adds on top of reality. That is also why rank 1 is capped at 10 minutes: a larger shift means the stop is being re-derived, and that belongs to the window
solve.

Rank 4 is a cancellation, not a wait. The freed interval does not become an empty block, it is merged into a neighbouring stop or into the walk. A cancelled stop that leaves visible dead
time is a bug against constraint 3 in section 1.

Rank 5 is never entered by the planner. Attendance is a fact only the student can assert. The system will plan around a skipped class, it will never decide one.

## 6. Complexity

`n` is stops in the day (4 to 8 in practice). `m` is candidate locations per stop (tens to low hundreds). `k` is broken stops (1 to 2 typical). `w` is the number of stops in the broken
window (2 to 4 typical).

| Routine | Cost | When it runs |
|---|---|---|
| Full plan | `O(n * m)` | at reveal, once per day |
| Repair | `O(k * m * w)` | per deviation |
| Neighbour check | `O(1)` | after every repair |
| Worst case repair | `O(n * m)` | deviation at the first stop of the day |

The honest comparison. Full recompute is `O(n * m)`. Repair is `O(k * m * w)`, which beats it by a factor of `n / (k * w)`. With `n = 6`, `k = 1` and `w = 3` that factor is 2. With an early
deviation the window covers most of the remaining day and the factor approaches 1. There is no asymptotic advantage in the worst case, and this document does not claim one.

The absolute numbers are small either way. Six stops against a hundred candidate rooms is a few hundred feasibility checks, each answered from a precomputed interval index. Nobody will
notice the milliseconds. The reason to repair minimally is not speed, it is the changed-stop count. Full recompute may re-rank every room and re-derive every duration, so its output differs
in places where nothing was wrong, and each difference costs the student a re-read of their day.

## 7. A worked example

Illustrative example, not a measured result. The numbers were chosen to be walkable, and the planner that would produce them is the stub described in section 9.

Anchors: 09:00 to 10:00 COMP1100 in CSIT, 11:00 to 12:00 MATH1013 in KB, 13:00 to 14:00 COMP2100 in CSIT, 16:00 to 17:00 PHYS1200 in CSIT. Walk CSIT to KB is 7 minutes, buffer is 3.

| # | Type | Place | Committed | After repair |
|---|---|---|---|---|
| S1 | study | CSIT 105 | 10:03 to 10:47 | 10:23 to 10:46, changed |
| S2 | transit | CSIT to KB | 10:47 to 10:56 | 10:46 to 10:53, kept |
| S3 | eat | KB food court | 12:05 to 12:42 | unchanged, kept |
| S4 | transit | KB to CSIT | 12:42 to 12:51 | unchanged, kept |
| S5 | study | CSIT 227 | 14:05 to 15:50 | unchanged, kept |
| S6 | study | CSIT 227 | 17:05 to 18:20 | unchanged, kept |

At 10:20 the student reports a 20 minute overrun, still in CSIT. The 50 minute usable gap becomes 33 minutes, and the walk to KB cannot move because MATH1013 is an anchor.

Identity test. S1 cannot start at 10:03, since the student is not free until 10:20, which is already 17 minutes past the planned start and outside the 15 minute tolerance, so S1 is broken.
S2 keeps its purpose, its building pair and its start to within a minute, so it is kept. S3 to S6 sit outside the window and are kept.

The ladder is walked inside the window spanning S1 and S2, bounded by 10:20 on the left and 11:00 on the right. Rank 1 applies: the same study stop in the same room, edges adjusted, which
fits 23 minutes of study and still leaves the walk and the buffer intact. Ranks 2 to 4 are never reached.

Changed stops: 1. The neighbour check passes because the next kept stop is an anchor and the walk to it still fits.

For contrast, a full recompute of the same deviation re-derives all four gaps. In this illustration it lands on three differences: the morning study moves to the KB side, the lunch stop
shifts to a shorter window because the solver no longer sees the original boundary, and the afternoon study changes room because the availability snapshot is re-ranked. Changed stops: 3. The
result is no better on paper and strictly more disruptive in practice, which is the quantity the comparison in section 1 is built to measure.

## 8. Failure modes

**A criterion that is too loose.** Widen the three tests and most candidates match, so the plan drifts without anyone noticing. Two shapes follow: the student arrives at CSIT 105 and finds
it empty because the block quietly moved within the building, or a study block grows and eats the lunch because purpose and place matched and only the duration changed. The guard against
both is that identity is measured against the committed plan, and that a stop keeps its type and subject.

**A criterion that is too tight.** Narrow the tolerances and ordinary deviations break everything downstream. The student is 8 minutes late and the whole afternoon is re-solved. The symptom
is follow-through collapsing while changed stops per repair approaches the full recompute figure, which makes the repair layer pointless.

**Tolerance sensitivity.** Both tolerances move all four metrics, which is why they are swept rather than fixed. `time_tolerance` at 5 turns a 6 minute nudge into a broken stop. At 30,
distinct stops merge into one identity and the day can slide half an hour per event. There is also a consistency constraint: rank 1 shifts a stop by at most 10 minutes, so a `time_tolerance`
below 10 would make the cheapest planner action contradict the identity test, producing repairs that report changes they did not intend. The pair must satisfy `time_tolerance > max shift at
rank 1`. `building_tolerance` is in walk minutes, and raising it quietly disables the same building rule by making short cross-building hops free.

**Cascades.** A repair can push the next stop past the tolerance, which breaks the one after. The neighbour check catches one step, the expansion loop catches two, then the remaining day is
re-planned in full. That fallback is a real failure of the minimality claim, so it is counted and reported separately rather than hidden inside the changed-stop average. The loop terminates
because each expansion strictly grows the window and the window is bounded by the plan.

**Permanent local suboptimality.** Established stops are never re-optimised, so a room that has become noisy or full stays while it still counts as the same thing. The remedy is the user's
own "change this one" action, which is a user-initiated break, not a repair.

**Stale availability.** Availability is read at reveal and trusted afterwards. If a room fills between reveal and arrival, the plan is wrong until the user reports it. The system does not
poll, so this is accepted rather than solved.

**Overrun longer than the gap.** A delay that exceeds the gap must not be absorbed by shortening the anchor. Anchors win, the stop is cancelled and the next anchor is joined late. A planner
that compresses an anchor to fit a stop is out of contract.

## 9. What is not implemented

The planner in the prototype is a stub. It reads the anchors, computes gap durations, and places one study stop per usable interval with a fixed duration. It does not query rooms, does not
walk the cost ladder, does not run the identity test, and has no repair path. Calling `repair` on it returns the plan it was given.

What exists: anchor ingestion, gap arithmetic, the availability engine over the timetable, the building to building walk matrix, the four-state state machine, and the deviation events. What
does not exist: the window solver, the ladder, the neighbour check, the identity test in code, and the instrumentation for the four metrics in section 1.

So every number in this document is a design parameter or an illustration, and section 7 carries its own warning for that reason. The claim that repair changes fewer stops than full
recompute remains a hypothesis. It is the first thing the comparison switch should test, before any interface work sits on top of it.

Also deliberately absent: automatic attendance changes, multi-day learning of the tolerances (they start at defaults), and any booking beyond the claim and retract path for small groups.
Those are boundaries of the design, not gaps in the prototype.
