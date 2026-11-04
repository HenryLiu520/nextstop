# 0002. The plan lives in the client

- Status: Accepted
- Date: 2026-11-04
- Scope: `frontend/`, `mobile/`, `product/demo/index.html`

## Context

A plan is a sequence of stops for one day. Each stop has a start, a duration, a place and a
purpose. Four or five of them, never more, and the whole thing is worthless tomorrow.

A day is edited many times. The student reveals it in the morning, starts following it, reports a
late class, swaps a meal, skips a lecture and undoes a swap. Each of those is a change to one
sequence.

The plan is also the only object in the system that repair compares against. Repair asks whether
a candidate stop is the same thing as the committed stop, and the committed stop exists only in
the client that published it.

The physical situation settles a second question. The student is standing in a corridor, or
walking between buildings, when the plan matters. The corridor has no signal. A design that needs
a round trip before it can answer "what now" answers too late to be useful.

There are two clients on one service: the desktop build on port 5173 with three roles, and the
phone build on port 5174 with the student view.

## Decision

The plan is client state. It lives in memory in the browser tab and nowhere else.

The client holds the state machine. `planning` when the day is revealed or edited, `executing`
while the student follows it, `deviated` when reality diverges, `repairing` while the fix is
applied.

Editing is local. One gesture produces one state transition and one undo record, with no network
call. The undo stack is a local stack and it can be replayed in reverse.

The only plan related read is availability, taken once at reveal through
`GET /api/availability`. After that the snapshot is trusted for the rest of the day. The
timetable is copied into the plan as anchors and frozen, so anchors are walls even if the
network disappears.

Nothing about the plan is persisted. `localStorage` holds the auth token, the display name, the
role and the language. It does not hold stops.

Repair runs on the client, against the committed plan the student actually saw. The server is
not consulted and cannot observe the repair.

## Consequences

Every edit is immediate. There is no latency between a tap and a new plan, which matters because
the same tap may be repeated three times in a minute while a student changes their mind.

The server holds no session state. It has no idea a plan exists, so it cannot leak one, expire
one, or block on one.

The plan keeps working offline, which is the normal condition rather than an edge case. Once the
day is revealed, the corridor has no bearing on whether the plan can be read or repaired.

Undo is free. A single stack covers every action, including ones the planner performed, because
all of them are local state transitions.

The plan does not follow the student between devices. A day revealed on the phone is absent on
the desktop. This is accepted. The two clients are used in different places at different times,
and a student rarely holds the same day on both.

Closing the tab loses the day. Revealing it again costs one availability read and one fill pass,
so the loss is small but real. Persisting the plan to `localStorage` would cover this case, and
it is not done yet.

Plan quality cannot be measured centrally. Follow-through and changed stops per repair are
computed where the plan lives, so any aggregate needs an explicit reporting path.

The planning rules exist in two client codebases. `frontend/` and `mobile/` can drift, and a rule
fixed in one is not fixed in the other until it is copied. A shared module would help and does
not exist.

## Alternatives considered

**A plans table with optimistic concurrency.** A version column and a read-modify-write loop.
Rejected. It buys sync for a document that expires in a day, and the conflict story is worse than
the loss it prevents. Two devices editing one day is not a flow anyone asked for.

**The server computes the day and returns it.** The client asks for today and receives stops.
Rejected. Every edit becomes a round trip and a server row, and every change to a planning rule
becomes a deployment. It also breaks in the corridor, which is where the product is used.

**Server side planning with a local shadow copy.** Compute centrally, cache locally, reconcile on
reconnect. Rejected. Reconnection needs a merge rule, the merge rule needs a version, and the
result is a distributed system for one student's lunch break.

**Persist the plan to `localStorage`.** Cheap, and it covers a tab closed by accident. Not
adopted yet because it does not cover the device switch either, and it introduces a stale plan
that outlives its day. It is a small addition and not a reversal of this decision.

**An operation log with merge on reconnect.** Offline first with per-op reconciliation. Rejected.
The merge rules would be invented for a case that does not occur, and a wrong merge is harder to
notice than a missing plan.
