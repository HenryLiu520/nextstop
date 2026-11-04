# Why NextStop Exists

**English** · [简体中文](why.zh-CN.md) · [繁體中文](why.zh-TW.md)

> Following It Is All You Need.

This document explains the problem the project answers and why the tools that
already exist do not answer it. It is not a feature list. The features live in
[`features.md`](features.md). What follows is the argument
for the thing existing at all.

## The moment

It is 09:47 on a Thursday. A lecture in KB has just ended. The next class is at
11:00 in CPD, so there are 73 minutes between the two, and the walk between the
buildings takes a real bite out of them.

The student walks out of the lecture theatre with the rest of the cohort, and
then the crowd thins and they are standing in a corridor holding a phone. They
are not late. They are not early in any useful sense. They have slightly more
than an hour and no plan for it.

They open a room finder. It shows a list of classrooms that are empty right
now, and it is a long list, and every item on it looks the same. Room 203. Room
311. Room 105. The student scrolls once, does not recognise any of the numbers,
and closes the app. Then they sit on the bench outside the lecture theatre and
look at nothing for eleven minutes, because sitting down is the only option that
does not require a decision.

At 10:02 they get up and walk to CPD anyway, and sit outside the next classroom
for 40 minutes, because at least that is a place they are sure they are allowed
to be. Nothing about that hour was decided. It simply passed.

What the student needed at 09:47 was not more information. It was one sentence
of the form go to this building, sit in this room, do this until 10:45, and
leave at 10:45. That sentence does not exist anywhere in their phone. Every app
they have will hand them a menu and wait, and waiting is the expensive part,
because the gap is finite and it does not pause while they decide.

That is the moment this project is about. Not a missing room. A missing
decision.

## What the student does not know

Three things are missing, and they are not the same thing.

The first is where they can go. An empty classroom is not automatically
available to them. Some rooms are booked for a tutorial in ten minutes. Some
are labs with card access. Some are reading rooms where a conversation is not
allowed and some are common areas where a conversation is expected. The phone
in their hand knows which rooms are empty. It does not know which of them this
particular student is entitled to sit in, and it cannot tell them, so they fall
back on the corridor they are already standing in.

The second is where they should go. Permission and fit are different questions.
A quiet room on the eighth floor is a bad answer if the student needs to print
something, and a room next to the canteen is a bad answer if they need to read
forty pages. The gap is 73 minutes, which is long enough for a real task and too
short for a long walk plus a real task. A list of empty rooms does not rank
itself against a length of time, because a list has no idea what it is being
used for.

The third is what to do. This is the one that is hardest to admit, because it
sounds like a problem a person should be able to solve alone. In practice the
choice between starting an assignment, reviewing notes, eating early, or
answering messages is exactly the choice that gets deferred until the hour is
gone. The student does not lack options. They lack a first move.

Room choice is the visible part of the problem and the smallest part of it. It
is also the only part anyone has built a tool for.

The three are worth separating, because they are usually merged
into one vague complaint about campus life. The first is an access question and
it has a correct answer that the university already holds in some office
database. The second is a fit question that depends on the length of the gap and
on what the person is carrying. The third is a commitment question, and it is
the only one that survives after the first two are answered. A system that
answers only the first two produces a very well informed student who is still
sitting on a bench.

## Why existing answers do not cover it

Two families of tool sit closest to this problem. Each is good at its own half.

### Room finders

A room finder answers one question, which rooms are free, and it answers it by
subtracting bookings from a public timetable. That makes its output a derived
value. It has no author, it does not change between two people looking at it,
and it describes no individual. It is the same list for the student who needs
to print, the student who needs silence, the student who has 20 minutes and the
student who has 90. It presents a set and expects a person to reduce the set to
a decision, and the reduction is the work. Handing back the set means handing
back the problem, slightly better formatted. This is why the app gets closed
after one scroll. Nothing in that list is about the person reading it.

### Calendars

A calendar is the other half of the same gap. It stores decisions that have
already been made, and it stores them faithfully. It will tell you, correctly,
that 09:47 to 11:00 is empty. Emptiness is what it is for. It does not produce
the thing it stores. A calendar with nothing in it is an accurate description of
a person who has not decided anything, and it will stay accurate for the whole
73 minutes. It also has no concept of place, so even a fully booked day says
nothing about where the body attached to it is supposed to be standing.

Between the two of them, the finder knows about rooms and the calendar knows
about commitments. Neither holds the join. Neither says this person, at this
time, in this place, doing this.

## The question one level up

The question a student actually has is not which room is free. It is where this
person should be, and when they should move.

That sentence is the research gap, stated once, in a form that can be quoted:

> Existing tools answer which room is free, or what has already been decided.
> Neither answers where a given person should be inside an uncommitted window,
> nor when that person should start moving toward it.

The room is inside that question, not above it. So is the calendar entry, which
becomes an anchor rather than an answer. Answering the higher question requires
holding a timetable, a map, a notion of what a gap can hold, and a notion of
what a person is entitled to do in a building, and then producing a sequence
rather than a set. The sequence is the deliverable. A list is not.

The reason this is a project and not just a restatement of the problem is that
the higher question is answerable with data a university already has. Timetables
exist. Room inventories exist. Walking distances can be measured once and
stored. Permission rules can be written down as a small number of categories
instead of a per-room policy. What was missing was not data. It was the layer
that turns the data into a decision and then defends that decision for the rest
of the day.

## What this project does about it

NextStop builds a day rather than reporting a state. It takes the timetable as
fixed anchors, computes the real gaps between them after walking time and a
buffer, and fills each gap with a place, a purpose and a start time, so the
student opens the app once and reads a day instead of searching a list. When
reality deviates, because a lecture overran or a friend appeared or a class was
skipped, it repairs the smallest possible part of the plan and leaves the rest
standing, on the argument that a plan which rewrites itself constantly is no
different from having no plan. The user's one job is to follow it. The design
target is that a student reads the day once in the morning and then only looks
again when something has moved.

## What we do not claim

Three limits are worth stating plainly, because the argument above is stronger
when they are not hidden.

The timetable is sample data. The anchors the system plans around come from a
fixture, not from a live university feed, so the pipeline is real and the
particular Thursday is illustrative.

The repair behaviour is a design position, not a measurement. The claim that
minimal repair preserves a plan better than rebuilding it is argued from the
cost ordering and shown on worked cases, and it has not been compared against
alternatives on real student days.

The learning side is not implemented. The document treats what to do as part of
the problem, and the system's current answer is a scheduled block with a
purpose, not a study tool that knows whether the reading happened.

What is claimed is narrower and should hold. A student standing in a corridor
with 73 minutes, two buildings apart, is not short of rooms. They are short of a
decision, and a decision is a thing software can make and keep making when the
day moves.
