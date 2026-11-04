# 0001. The backend is a data layer

**English** · [简体中文](0001-backend-is-the-data-layer.zh-CN.md) · [繁體中文](0001-backend-is-the-data-layer.zh-TW.md)

- Status: Accepted
- Date: 2026-11-04
- Scope: `backend/`, `db/`, both clients' API surface

## Context

NextStop answers one question: where should this person be, and when should they move.
That answer could be computed on the server or on the client. The choice decides what the
backend is for.

The facts the system needs are not the same kind of thing.

| Property | The plan | The timetable |
|---|---|---|
| Owner | one student | the institution |
| Lifetime | one day | one semester |
| Change rate | many edits per session | fixed once published |
| Truth | a proposal, always negotiable | final, never questioned |
| Read/write | read-write, locally | read-only from our side |

The timetable is shared by thousands of people, published by an office this project does not
control, and true for a whole semester. The plan belongs to one person and is dead by tomorrow.
These are different problems and they want different homes.

One fact must not differ between two people looking at the same room. If the desktop client and
the phone client disagree about whether 2A is occupied from 10:00 to 11:00, nothing in the system
can settle the argument. A single authority is required for the timetable, and no such authority
is required for a plan.

## Decision

The backend is a data layer. It answers "where can I go".

It serves the timetable, rooms, buildings, availability, admin room updates, reviews, accounts
and the audit log. It runs on Spring Boot 4.1.1 and Java 21, over PostgreSQL 17, with Flyway
migrations V1 through V16.

It stores no plan. There is no plans table, no endpoint that accepts a plan, and no column that
holds one. Nothing on the server has an opinion about where a student should be at 14:00.

The availability computation stays on the server, because it is the one answer both clients must
agree on:

```
free(room, date) = blocks(room, date)
                 - blocks taken by a class
                 - blocks taken by an active room update
                 - 10 minutes either side of every occupied block
```

The changeover margin is server configuration with a default of 10 minutes, never a client
setting. The timetable wins over an admin note: an update that overlaps a class block is rejected
with HTTP 400 and the code `TIMETABLE_PRIORITY`.

Roles are ordered `user < admin < superadmin` and enforced once, on the server. The clients only
hide what a token could not use.

## Consequences

The shared facts have one home. Two clients on two versions cannot disagree about a room, and no
conflict resolution is needed for data that is read-only from this side.

Planning rules can change without a migration. A new stop type, a new gap rule, a new repair rank
are client changes. The database schema does not track the shape of a plan, so it never has to
change when the plan does.

The service stays small and testable. Its surface is one question plus administration, and 27
automated tests against a real database cover it.

The cost is that the server cannot answer what a given user is doing at 14:00. Any feature that
needs that, from a shared plan to a reminder, has to be built on the client or needs a new
decision.

Multi device sync is separate work. No stored plan means no sync, no version column and no
last-write-wins rule. It also means no answer at all, which is the honest position until the
feature exists.

Plan quality is invisible to the server. The four success metrics are instrumented on the client
only, so central measurement would need an explicit reporting path.

Availability is a snapshot. The client reads it at reveal, the server does not poll, and a room
that fills afterwards stays in the plan until the user reports it. This is accepted, not solved.

## Alternatives considered

**The server owns the plan.** A plans table with endpoints to read and write it. Rejected. It
needs a user, a version, a sync rule and a conflict story, and it still loses to a phone closed
without saving. The cost is coordination, and the plan is too short lived to pay it.

**A thin backend and a client side timetable.** Ship the timetable once and let each client
compute availability. Rejected. Two implementations of one shared truth, on two release cycles,
with no arbiter. The changeover margin alone would drift between them.

**A backend for frontends, one endpoint set per client.** Rejected. Two clients sharing one
contract is cheaper, and a per-client surface hides divergence instead of preventing it.

**Keep the backend as the product.** Planning endpoints, saved days, sessions, server side
recommendations. Rejected. The product claim is that the system decides the day, and a server
cannot decide a day it cannot see the user inside. It would also put the interesting logic
behind a network boundary and a deployment.

**A cache of plans as a convenience.** Store the last plan per user so a second device can pick
it up. Rejected for now. It adds a write path on the server for a document with a one day life,
and the only user it serves is one who switched devices mid day.
