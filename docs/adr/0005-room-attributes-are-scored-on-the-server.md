# 0005. Room attributes are scored on the server

**English** · [简体中文](0005-room-attributes-are-scored-on-the-server.zh-CN.md) · [繁體中文](0005-room-attributes-are-scored-on-the-server.zh-TW.md)

- Status: Accepted
- Date: 2026-11-11
- Scope: the room attributes endpoint, the scoring layer, `docs/room-scoring.md`

## Context

A study room recommendation ranks rooms inside one window. The rank has two parts. The dynamic part
reads the window, the walk from the stop before and the walk to the stop after. The static part
describes the room: how quiet it is, whether the desk holds a laptop and a book, whether there is
power at the seat.

The static part belongs to the room. It sits outside the plan and outside the availability answer.
Availability reports that 2A is free from 10:00 to 11:00. It reports nothing about the noise in 2A
or the sockets along its wall. Two rooms free in the same window differ by the whole decision.

A client reads availability and computes occupancy from it. The same client has no source for a
lift lobby beside a door, a 60 cm desk, or a window onto a light well. Those facts come from
walking the building, from a floor plan, or from the facilities office.

Ranking also needs one ruler. Two students in the same window compare the same 200 rooms. Each
phone that scores rooms for itself produces its own order, so a room ranks first on one device and
fourth on another, and the list stops being an answer.

## Decision

The backend serves the room attributes: six dimensions per room, each an integer from 0 to 5, in
one document with a revision number.

```
GET /v1/rooms/attributes
{ "rev": 14,
  "updatedAt": "2026-11-11T09:00:00Z",
  "rooms": [ { "code": "KB227", "quiet": 4, "desk": 3, "sockets": 3,
               "ac": 5, "spaciousness": 4, "daylight": 3 } ] }
```

Each dimension has an anchor at 0, at 3 and at 5. An auditor walks the room and writes the integer
that matches it.

| Dimension | Key | 0 | 3 | 5 |
|---|---|---|---|---|
| Quiet | `quiet` | a door onto a lift lobby, a canteen or a corridor with through traffic | an open floor shared with group work, conversation at the next table | a silent zone behind a door, away from circulation |
| Desk and seat | `desk` | armchairs or a bench with no writing surface | a shared table with room for a laptop and a book | an individual desk 90 cm wide or more, with a chair |
| Socket density | `sockets` | no usable socket | sockets along the wall | a socket at every seat |
| Air conditioning | `ac` | no air conditioning | air conditioning on a building schedule | air conditioning with a control inside the room |
| Spaciousness | `spaciousness` | 1.0 m² per seat or less, desks in continuous rows | about 2.0 m² per seat | 3.0 m² per seat or more, a free seat beside most desks |
| Daylight | `daylight` | no window | a window onto a light well or an internal court | an external window, daylight reaches the desks |

Values 1, 2 and 4 record a room that sits between two rows of the table. The auditor writes the
integer that matches the room, and 2 on `quiet` means quieter than a canteen and louder than an
open floor.

The client fetches the document when `rev` differs from the revision it holds, writes it to
IndexedDB, and computes the static score on the device. Each value divides by 5 into a factor from
0 to 1, the weights come from [`room-scoring.md`](../room-scoring.md), and the score serves every
window that day. One revision costs one fetch.

The dataset feeds ordering and the phrase on the card. A room card reads "quiet, sockets at the
seat". The raw value stays inside the client.

## Consequences

One ruler, one order. Two clients holding `rev` 14 rank the same rooms the same way for the same
window and the same profile. A disagreement traces to the profile or to the availability snapshot,
and both are inspectable.

The dataset needs an audit process. Each revision carries an author, a date and the list of changed
rooms. A value changes by editing one record and incrementing `rev`, and the revision number is the
handle for asking who changed a room and when.

The client gains a cache layer: one IndexedDB store, a revision comparison at startup, a refetch on
a mismatch, and a local recompute of the static table when a new revision lands.

A new room needs its six values before it enters the timetable. The audit is part of onboarding,
and a room without a row has no place in the candidate set.

Between audits the values describe the building as it was. A renovated floor keeps its old values
until the next pass, and `updatedAt` states the age of the description.

Each value is a judgement. It records what an auditor saw on a visit, and the revision number is
the only claim of freshness the data supports.

A wrong value costs one bad ranking. The student's own "change this one" action overrides it, and a
single value is not destructive.

## Alternatives considered

**Each client scores the rooms for itself.** Rejected. The same room ranks differently on two
phones, and the candidate order stops being comparable. N clients also duplicate one audit into N
judgements.

**The server serves the finished static score.** One float per room, ready to sort. Rejected. A
float answers nothing when the student asks why, and the weights would move to the server, so a
weight change becomes a deployment.

**Attributes ride along with every availability answer.** Rejected. Availability is asked once per
window, up to eight times a day, and the attributes change once a semester. Every call grows to
carry a document that moves four times a year.

**Quality inferred from the timetable.** A room used for seminars counts as quiet on the days it
is empty. Rejected. Occupancy over a semester says nothing about the desk, the sockets or the
daylight, and the inference fails hardest on the rooms nobody has visited.

**Quality inferred from reviews.** Rejected for now. Reviews arrive unevenly, the popular rooms
collect thirty and the rest collect none, and a room with no review would score the same as a room
with bad ones.
