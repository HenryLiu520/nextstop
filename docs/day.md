# How a day gets built

**English** · [简体中文](day.zh-CN.md) · [繁體中文](day.zh-TW.md)

One day, from three inputs to one published sequence. The numbers come from a worked Thursday and
from measurements against the mock. The planner behind it is still a stub, and section 11 says which
parts run.

## 1. Three sources

Planning starts with three inputs, and each one carries a different authority.

**The timetable** arrives from the school feed. The client reads it once at reveal and copies every
entry into the plan as an anchor: course code, building, room, start, end. It stays read-only. The
planner moves no anchor, shortens none, and questions none. A student who is late changes their own
availability, and the class still starts when it starts.

**The calendar subscription** merges in from an `.ics` URL. Data flows one way, into the app. A
tutorial slot, a society meeting, a dentist appointment booked in somebody else's calendar all
arrive as commitments. None of them travel back out.

**Manual additions** hold the one-off things: a parcel to collect, a lab report to hand in, a friend
to meet at 19:00. Inside the union a manual entry weighs the same as a class. One property separates
it, and that is deletion. The student removes it whenever they like.

Authority is the difference between the three. The feed is final, the subscription is an import, and
a manual entry is a reminder the student wrote for themselves.

## 2. The union

Three lists become one list of occupied time. Sort every interval by start time, sweep once, and
merge three cases: an overlap, a containment, and a gap shorter than fifteen minutes. A class that
ends at 10:20 and a class that starts at 10:30 produce one block. The ten minutes between them count
as corridor, and a window that short holds no stop.

The threshold has a cost worth writing down. Every break under fifteen minutes disappears into its
neighbour, so a real twelve minute pause between two lectures is spent in the building the student
just left. Twenty minutes survives, because twenty clears the bar.

Merging is symmetric about the sources. A calendar entry that overlaps a class merges by the same
rule as a manual entry that overlaps a class, so the order of arrival changes no result. The output
is one sorted list of blocks, each with a start, an end and a building. Everything downstream reads
this list and no earlier one.

## 3. Walking and buffer

Every gap pays two costs before it counts as free time.

**The buffer** takes two minutes off each end. The first two cover zipping a bag and standing up.
The last two cover the walk from the building entrance to the door of the next room, a distance
nobody estimates well.

**The walk** is measured in metres and converted at 75 metres per minute, which is corridor pace
with a bag on one shoulder. Floors add 1.2 minutes per five floors, so one flight costs about 0.24
minutes and a lift ride is priced at the same rate. The total is multiplied by the student's walk
factor, 1.00 until three observations move it.

Two samples carry the arithmetic. KB to CPD is about 250 metres with one floor up, so 3.3 minutes of
distance plus 0.24 of floor gives 3.5 minutes. CSIT to the Main Library is 8 minutes along the
covered walkway.

Both costs come off before a window is measured. A 190 minute gap inside one building leaves 186
usable minutes, and the next section starts from there.

## 4. Windows

The worked Thursday leaves three windows.

| Window | Gap | Walk | Buffer | Usable |
|---|---|---|---|---|
| 10:20 to 13:30, CSIT to CSIT | 190 | 0 | 4 | 186 |
| 14:20 to 15:30, CSIT to CSIT | 70 | 0 | 4 | 66 |
| 16:20 to 19:00, CSIT to the Main Library | 160 | 8 | 4 | 148 |

Two rules decide what a window can hold. A window under 25 minutes gets no study stop, because
twenty minutes covers unzipping a bag, opening a laptop and packing again. Such a window merges into
the block before it or into the walk. A window that crosses a meal band first cuts a meal segment at
the nearest edge, and the remainder becomes study time.

Both numbers come from the same arithmetic. 25 minutes is the smallest sit that leaves 15 minutes of
work after two minutes of settling at each end. The meal bands are 11:30 to 13:30 and 17:30 to
19:30.

The 186 window starts before noon, so it holds lunch as well as study. The 66 window is study from
end to end. The 148 window holds dinner, and its right wall is a commitment at 19:00 in a different
building.

## 5. What each window holds

Every window gets a purpose, and the purpose decides where its end comes from.

**A meal** is stored as a latest departure time. The value runs backwards from the next commitment:
13:30 minus 2 minutes of buffer minus 40 minutes at the table gives 12:48, and the card shows 12:45.
The student sees two options, eat at 12:05 or eat at 12:45, and either one protects the afternoon. A
third option, skip, costs the window its meal.

**A meeting** keeps the clock time two people agreed to. The card carries a 待预约 badge while the room
is unconfirmed, and repair may move the room around it. The start time stays where the agreement put
it.

**Study** takes every remaining minute. That is what keeps a window from rendering empty: 186
minutes minus lunch leaves two study blocks, 66 minutes is one block, and 148 minutes minus dinner
leaves two.

A window with three purposes in it reads as a short sequence on one card. The order follows purpose
priority, with the meal band as the hard edge that study fills around.

## 6. Choosing the room

Four filters run before a score exists, and one failed test ends that candidate.

1. Free for the whole window, first minute to last.
2. The account may enter. A card reader on a lab door removes every student outside that programme.
3. A different room from the one just left.
4. Reachable, with five minutes left to sit down.

About 200 rooms enter the scoring pass. The score has two halves: seven static factors on the room
after a rejection discount, and a dynamic half built from the walk in, the walk out and the
contiguous free time. A short window leans on the walk, a long window leans on the room. The
factors, the weights and one worked example sit in [room-scoring.md](room-scoring.md).

Cost, measured against the mock: 200 rooms, three windows, 600 evaluations, about 6 milliseconds.
The filter pass is most of the reason, since it removes candidates before a single factor is read.

## 7. Publishing

The whole sequence leaves the planner in one write, stamped with a version number. Version 1 is the
day as revealed. Each repair that commits writes version 2, then 3, and every earlier version stays
readable so an undo can reach it.

After publish the plan is read-only. No control in the interface edits a stop directly, and repair
is the only writer. Each commit appends an undo record, so a bad repair costs one tap.

Four actions sit on the card, and each one reports on the world, which the client reads as input:

| Action | Stored as | Effect |
|---|---|---|
| 我到了 | `arrive(stop)` | the stop is truncated at now, later stops hold |
| 换一个 | a rejection on the room | the room takes a 0.88 discount, the next candidate is offered |
| 我晚了 | `delay(minutes)` | repair runs against the committed plan |
| 不去 | `skip(anchor)` | the anchor becomes free time for this student |

The version number is what makes a double tap safe. Two taps write two versions, and one undo
removes both.

## 8. When the day moves

A deviation arrives as a time and a building. A 20 minute overrun, a tap on 我晚了 and a missed bus
normalise into that same pair, so repair has one entry point.

Two stops count as the same station when three tests pass together: the purpose matches, the
building matches, and the starts sit within 15 minutes. The comparison runs against the committed
plan, the version the student actually saw. That reference is what stops a slow drift of 14 minutes
per repair from reporting itself as no change.

Repair then solves inside one window. The left wall is the first stop that can no longer hold. The
right wall is the last stop the observation touches. Stops outside the window copy across untouched.
Anchors act as walls, so a window ends at a class even when the class is the thing that broke.

A seam check follows, asking whether the new boundary pushes the next kept stop past the tolerance
or breaks the walk to it. If it does, the window grows by one stop, twice at most. The criterion,
the ladder and the complexity sit in [algorithm.md](algorithm.md).

## 9. What it learns

Four quantities, one parameter each, one counter each.

| Learned | Parameter | Default | Observation |
|---|---|---|---|
| Walk speed | walk factor | 1.00 | the gap left before 我晚了 |
| End of day | end time | 22:00 | the tap that ends the day |
| Where to eat | meal venue | nearest canteen | the option picked from two |
| Attendance | rate per course | 1.00 | 不去 on one class |

A counter increments on its own observation and stops at three. Under three the default stands and
the card reads still learning you. At the third the parameter takes the smoothed value, so one
strange Tuesday weighs as one point inside a mean. Editing a value by hand resets that counter to
zero.

The algorithm stays fixed. Only those four numbers move, which is what makes a learned day
auditable: four values, four counters, readable on one screen. The walk factor is designed to travel
from 1.00 to 1.28, so a slow walker stretches every estimate by up to 28 percent.

## 10. When data is missing

Two inputs can arrive stale, and each has its own fallback.

**Availability.** Every answer is stamped with its arrival time. A snapshot older than 10 minutes
still renders the plan. The contiguous availability factor drops to 0.5, the neutral value, and the
affected cards carry 未核实. Repair keeps the room where the plan already put it, and moves no new stop
into a room whose answer has aged.

**Room attributes.** A failed fetch falls back to the IndexedDB copy under its revision. A device
that holds no copy scores all six dimensions at 0.5 and marks the card unverified. A room with no
attribute record takes 0.5 the same way.

**The timetable.** Anchors are copied at reveal and frozen. A feed outage after that changes no
stop, because the plan carries its own copy.

One rule covers all three. A missing fact becomes a neutral number plus a visible label. The plan
renders, the student can see which stops rest on an old answer, and every score stays inside the
range the fusion expects.

## 11. What runs today

Three states sit side by side in this document.

| Part | State |
|---|---|
| Availability service, Spring Boot on 8080 | built, 27 tests passing |
| Walk matrix, anchors, gap arithmetic | built |
| Four-state machine | built |
| Room scoring, seven factors and fusion | runs in the mock, about 6 ms for a day |
| Planner behind the plan | a stub, one fixed study block per window |
| Repair ladder, identity test, seam check | specified, implementation pending |

The stub reads anchors, computes gap durations and places one study block of fixed length in every
usable window. It queries no rooms and walks no ladder. Calling repair on it returns the plan it was
handed.

Read this document as the shape the pieces are meant to take. The availability engine underneath
already answers the one question everything else depends on, and the day on screen today is built
from real answers. The choice of room inside a window is the part still moving from mock to client.
