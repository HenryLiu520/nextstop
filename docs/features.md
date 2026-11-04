# NextStop: features, in the order a student meets them

NextStop is one screen with six moments in it. This document walks the moments in
the order a student hits them: the day is revealed, read as one card, followed
through four actions, repaired when reality moves, edited a day ahead, and tuned
in a page that is mostly tuning itself.

The argument for the product existing is in [`why.md`](why.md). The planning and
repair mechanics are in [`algorithm.md`](algorithm.md) and are not repeated here.
Every feature below ends with one line naming what it stands on, so the
dependency is traceable without the derivation.

## How to read this

Each feature answers the same three questions.

| Column | Question |
|---|---|
| What it does | the behaviour, in one sentence |
| What you see | the screen, as the student would describe it |
| Depends on | the single thing underneath it, named and not unpacked |

Three status words are used throughout, and no row is written above its status.

| Word | Meaning |
|---|---|
| Built | runs in the prototype at `product/demo/index.html` |
| Data layer | answered by the Spring service, see [`architecture.md`](architecture.md) |
| Specified | designed and not implemented, which includes the whole repair path |

Two rules hold on every screen and are not repeated per feature.

- **A gap is never empty.** It takes a stop, or it is merged into a neighbour.
  There is no wait type and no placeholder row to fill in.
- **Four actions, and only four.** Nothing gets a fifth, and a class block gets
  exactly one of them.

## 1. Revealing the day

The first open of the day is the only open that computes anything.

| Feature | What it does | What you see | Depends on |
|---|---|---|---|
| Reveal once | plans the whole day on the first open and never plans it again | a finished day, with no spinner and no blank form | Specified. The planner is a stub, see section 9 of the algorithm |
| One location read | reads position once, at reveal, and does not track | nothing afterwards, and no second permission prompt | Specified |
| Fallback without location | falls back to the building of the last class, then to three building names | three names, which is the only question the day asks | Specified |
| Ends at the last commitment | ends the plan at the last thing the student promised to attend, not at a closing time | "until 18:20" that follows a timetable instead of a clock | Built as a bound, anchors from the sample feed |
| Read-only after that | later opens render the day already held and compute nothing | the card you left, in the state you left it | Built. The plan lives in the tab |
| One day at a time | rebuilds the plan at reveal and never carries it overnight | today always starts fresh | Built |

The output of the reveal is a sequence, not a set. Concretely: 09:00 class in KB,
11:00 study in KB 227, 12:10 lunch, 14:00 class in CPD. The choice made for 11:00
decides which building the student is standing in at 13:50, which is why a list of
free rooms cannot produce it.

## 2. One card

The home screen has no toolbar and no form. It has a card and the day below it.

| Feature | What it does | What you see | Depends on |
|---|---|---|---|
| The card | shows the current stop as one time and one destination, with the walk and the moment to leave | "KB 227 · until 12:00" and "walk 5 min" beneath it | the fill layer, L3 in the algorithm |
| The stack below | compresses the rest of the day into three to five blocks | rows of time, type and place, where tapping one is reading, not editing | Built |
| Walking as a visible cost | pays for every building change and shows the cost | "walk 8 min" on the line between two blocks | static walk matrix, the learned factor is Specified |
| Nothing to fill in | every gap already holds a stop, study by default | no empty row, no "free", no field to complete | Built at the surface, Specified as a guarantee (L2) |
| A room it does not own | a meeting without a room shows TBD rather than an invented number | "TBD" on the block, plus one action | Built |
| An unsettled fact | an unbooked meeting stays visibly unsettled | the block carries an action where a booked one carries a room | Built |
| Stroke means status | a solved block is solid, an external fact is dashed, a thing to book is dotted | three line styles, and never a block the system has not checked | Specified |

**What is not on this screen.** There is no "I'm here" button. Arrival is not a
gesture the app asks for, because a tap that confirms what the clock already knows
is a tap that will be skipped. A block whose end has passed renders as done, and
its sheet answers "That block is already gone". Arrival is inferred from the plan
and the time, never from a tap.

## 3. The four actions

Actions belong to blocks, not to pages. There is no toolbar, and the bottom bar
only switches between Day, Plan and Mine.

| Action | Where | What it does | What you see | Depends on |
|---|---|---|---|---|
| Change | study, meal, meeting | replaces this stop's place while holding its purpose and its time | three places with walk minutes and "free for the whole block", and more on demand | availability from the data layer, identity test Specified |
| Skip | class only | marks this student absent for that hour, the class still runs and the room stays taken | the block greys out and the hour becomes study | Built locally, the re-fill is Specified |
| Book it | a stop in a space that was already bookable | books the room and writes the outcome into the plan as a fact | "Booked · LIB 3/F Room 2" with Cancel beside it | Specified. No booking service is connected |
| End the day, stay longer | the bottom of the day | closes the plan early, or extends the last stop past its edge | the day collapses to "That's the day", with "Not yet" to come back | Built, and both feed the learned end time |

**A class block offers exactly one of these.** Skip. A lecture cannot be moved,
shortened or re-roomed by a student reading a card, so the block does not pretend
otherwise.

**Claim and book are different, and the interface keeps them apart.** For an
ordinary classroom a small group gets a claim: the system says it will use the
room, the claim is retractable in one tap, and retraction puts the block back
where it was. One tap booking is offered only where a booking was already
available to anyone. A room the system cannot allocate is never rendered as
allocated, which is why this is two behaviours and not one button. Depends on: the
availability answer, plus a booking path that does not exist yet.

## 4. Deviation

Every deviation enters at one place, the line at the top of the screen that says
"Something changed?", which offers two sentences rather than a form.

| Deviation | What happens | What you see | Depends on |
|---|---|---|---|
| Ran late, inside the gap | the open stop's edges move and the rest of the day is untouched | the card rewrites in place with "One block changed · the rest untouched (ends 10:46)" | Specified, the ladder in section 4 of the algorithm |
| Change this one | only the chosen stop's place changes, purpose and time held | "Moved to KB 221 · only this block changed" | data layer, identity test Specified |
| Overrun past the whole gap | the anchor wins, the stop is cancelled and its time merges into the walk, the next class is joined late | one fewer block and a shorter day, with no shortened class | Specified, rank 4 plus the merge in L2 |
| Undo | every write records what it replaced, and one tap restores it | "Undo" on the notice, for a few seconds | Built |
| Compare against a full replan | replays the same deviation through a recompute and shows both counts | "see full recompute", then "Full recompute: 3 blocks changed" | Specified as a test switch |

All five share one manner. The refresh is inline, there is no modal, no
confirmation, and no question about why. A change that costs a dialog costs more
than the change was worth.

Skipping a class is not a repair input. It is the skip action of section 3, and
the planner plans around it while never producing one itself.

The word for the mechanism is repair, and its stopping condition is a definition
of sameness: same purpose, same building within walking tolerance, start within
fifteen minutes. Anything failing that test is re-solved inside the broken window,
the window is checked against its neighbour, and the rest of the plan is left
standing. Depends on: [`algorithm.md`](algorithm.md), sections 3, 4 and 5. None of
it is in the prototype, where a deviation is one fixed edge shift.

## 5. Plan page

The second tab, used a day ahead rather than during the day.

| Feature | What it does | What you see | Depends on |
|---|---|---|---|
| Five week columns | lays the working week out as five narrow columns with one bar per block | five columns, today underlined, the bars carrying the shape of each day | Built |
| The day column | lists the selected day with start times in the margin and place in the row | times down the left, type and room across | Built |
| Start and end | two editable bounds at the head of today | "Today from 09:00 until 18:20", tap a pill, pick a half hour from 07:00 to 23:00 | Built, and it feeds the reveal |
| Only today is editable | marks every other day as read-only | "only today can be edited" where the editing controls would be | Built |
| Tap a block to change it | opens the same sheet the day screen uses | the four actions, scoped to that block | Built |
| Add by hand | adds a block, then fills the gaps on either side of it | "+ Add manually", a type and two times | Built, placement Specified |
| One sentence | takes a described change, previews what it would do, then applies it | "Make tomorrow afternoon study time, back in the lab by 3", then "What it did (preview)" and Apply | Specified. The preview is a fixed line today |

Two properties matter more than the layout. The page never shows an unsolved
block, so a hand-added study block is re-solved into a room before it is shown as
having one. And the page edits the same plan the day screen reads, so an edit here
can be undone there.

## 6. Mine

Two things live here, the parameters you may change and the ones it is changing.

| Feature | What it does | What you see | Depends on |
|---|---|---|---|
| Editable parameters | exposes the numbers the planner reads, with their current value | walking speed as a factor, the usual end of a weekday, the usual lunch spot, each with Save | Built, consumed by Specified planning |
| It is learning you | derives the same values from actions instead of asking for them | "walk × 1.14 · 3 observations before it replaces the default" | Specified, not instrumented |
| A floor on evidence | lets a learned value replace a default only after three observations | the same line, carrying the count | Specified |
| Learning is a default | a learned value pre-fills a parameter and no control is removed | every action still on screen | Built as a rule |

What is learned, and from what.

| Learned | Read from | Becomes |
|---|---|---|
| How fast you actually walk | the gap when you say you are late | earlier departures, ×1.00 up to ×1.28 |
| When your day ends | ending the day, and how often you stay longer | the default end for that weekday |
| Which places you go | which option you pick when you change a block | ranked first, marked as a place you go often |
| Whether you attend a class | skipping it | that hour treated as free for you alone |
| When you eat | choosing now or choosing after the block | the meal window |

The rule that keeps this honest is that what is learned is always a default and
never a control taken away. Attendance stays local, is never aggregated, and is
never used to rank anyone.

## What is not built

Stated once, so that no row above is read as further than it goes.

| Not built | Where it would land |
|---|---|
| The planner behind the reveal | section 9 of [`algorithm.md`](algorithm.md). The stub fills one study stop per gap |
| The repair ladder, the identity test, the neighbour check | sections 4 and 5 of the algorithm. Deviation is one fixed edge shift |
| The four metrics | not instrumented, so every number about repair is an illustration |
| Booking anything | 一键预约 is a label with no service behind it |
| Learning | the parameters are editable and nothing derives them yet |
| A real timetable feed | `mock-school-api` simulates it, the University endpoint is not connected |
| A shared plan | the plan lives in one browser tab, so the desktop and the phone do not agree |

The design target is one gesture a day. Every feature above is either a step
toward that or a piece of it that is not there yet.
