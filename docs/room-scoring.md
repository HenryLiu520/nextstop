# Room scoring

**English** · [简体中文](room-scoring.zh-CN.md) · [繁體中文](room-scoring.zh-TW.md)

How a room is chosen for one window in the day. This is a design document. The planner in the
prototype is still a stub. Every number below is a design value or an illustration from the worked
example.

## Introduction

Room scoring answers one question, which room for this window and this student. The planning layer
decides what the window holds and how long it lasts. Scoring reads the window, the walk from the
stop before it and the walk to the stop after it.

**Inputs.**

| Symbol | Input | Content |
|---|---|---|
| `R` | candidate set | every study room, about 200, held on the device |
| `W` | window | `t0` and `t1`, in minutes |
| `p` | previous anchor | end time and building, empty when no class precedes |
| `n` | next anchor | start time and building, empty when no class follows |
| `u` | profile | preferences, visit history, rejection log |
| snapshot | availability | free intervals per room inside `W`, plus a fetch timestamp |

**Outputs.** One chosen room, the next three with one reason each, and a per-factor score for every
candidate. The card text and the debug view both read the per-factor score.

### Filters

Four tests run before any score is computed, and the filter stops there.

```
free(r, W)         free for the whole window
allowed(u, r)      this account may enter
r != last_room     a different room from the one just left
reachable(r, W)    the walk fits, see Travel time
```

Seat count stays outside the filter, because one student always finds a seat somewhere in the
building. The open question is whether this room earns the walk.

## Static table

Seven factors, each read from the point of view of one person sitting down in one room. Their
weights sum to 1.00.

| Factor | Measures | Score | Weight |
|---|---|---|---|
| Familiarity | the building and the rooms this student keeps returning to | `min(visits to the building / 10, 1)` | 0.20 |
| Quiet | how loud the room is on its own | silent zone 1.0, open floor 0.65, discussion area 0.35, then lowered by live occupancy | 0.20 |
| Desk and seat | whether a laptop and a book fit side by side | desk 90 cm wide or more scores 1, smaller sizes step down | 0.15 |
| Sockets | power at the seat | a socket at every seat 1.0, sockets along the wall 0.5, none 0 | 0.15 |
| Air conditioning | a Hong Kong summer | present 1.0, absent 0 | 0.10 |
| Spaciousness | crowded rooms tire, empty rooms unsettle | 40 to 80 seats scores 1.0, half at either end | 0.10 |
| Daylight | a window, so the lights stay off at noon | external window 1.0, window onto a light well 0.5, no window 0 | 0.10 |

```
S = Σ_i w_i × f_i
```

Familiarity and building history are the same fact, so they merge into a single factor. The visit
count caps at ten, which stops one building from absorbing the whole score.

### Three factors outside the table

**Purpose fit.** A student looking for a study room wants a quiet place to sit. The quiet factor
answers that question already.

**Capacity fit.** One person does not check whether a room holds enough seats. Capacity enters as a
proxy for roominess inside the spaciousness factor.

**Floor preference.** A floor changes the lift wait, and the lift wait is walking time. Travel time
prices it with more accuracy than a separate preference would.

### The table stays on the device

200 rows, about twenty KB, in IndexedDB, keyed by a preference hash and a room revision number.
Three events write to it, and nothing else does.

- A preference change recomputes `base` across the whole table and leaves `n_eff` untouched, which
  costs one `O(200)` pass.
- A rejection writes one row. `n_eff += w_kind`, `nat = now`.
- A room attribute update recomputes the rows that changed.

```js
staticTable = {
  prefHash: "9f2c…",
  roomVersion: 14,
  rows: {
    kb227: { base: 0.58, visits: 7, n_eff: 1.8, nat: 1793 },
    lib3f: { base: 0.82, visits: 3, n_eff: 0.0, nat: 0 }
  }
}
```

Decay is lazy. A read computes `n_eff_now = n_eff × e^(−(now − nat) / 21 days)`. The value goes
back to disk at the next save. No timer runs in the background.

A published plan keeps the room it already chose. The table updates at once. The plan reads the new
values at the next reveal, which keeps the read-only rule in [`algorithm.md`](algorithm.md).

## Rejection

Rejection sits outside the factor table as a long-term discount on the static score. The discount
applies when the score is read.

```
n_eff = Σ_j exp(−Δt_j / 21 days)      each rejection counts once, 21 days decays it to 37%
S'    = S × (1 − 0.12)^n_eff          each effective rejection multiplies by 0.88
```

| Effective rejections | Score kept |
|---|---|
| 1 | 88% |
| 2 | 77% |
| 3 | 68% |
| 5 | 52% |
| 8 | 36% |

The multiplication gives three properties, and each one carries weight in use. Each rejection
lowers the score a little, which is what a student means by the gesture. A room rejected many times
sinks without reaching zero, so a renovated room can return. An undo lifts the score one step, so a
change of mind costs no history reset.

A tap on **Dislike** counts 1.0 and moving to another room counts 0.5. Arriving then leaving soon
after counts 0.3. Repeats inside thirty days accumulate on their own, so no extra multiplier is
needed.

## Dynamic score

Two factors, with weights that sum to 1.00, and both read the window and the two anchors.

### Travel time

`t_prev` is the walk from the previous anchor to the room. `t_next` is the walk from the room to the
next anchor. A missing anchor contributes 0.

```
total = t_prev + t_next
slack = t0 − previous anchor end − 2 minutes buffer
κ     = clamp(0.6 × slack, 3, 12)
T     = exp(−total / κ)
```

Slack sets the price of a minute of walking. With five minutes of slack, `κ = 3` and three minutes
of walking scores 0.37. With forty minutes of slack, `κ = 12` and five minutes of walking still
scores 0.66.

Time tolerance lives inside this factor. A tight window punishes the same walk harder, so a second
factor for it would repeat the first.

The next class only gates feasibility, through `t_next ≤ next_start − t1 − 2`. A candidate that
fails the test leaves the set at once.

### Contiguous availability

```
horizon = free minutes from t0 with no booking
C       = min(horizon / 120, 1)
```

120 free minutes score 1.0 and half an hour scores 0.25. A neighbouring window in the same room adds
0.1, capped at 1.0.

```
D = 0.65·T + 0.35·C
```

Walking carries the larger weight, because it is the one cost the student pays at that moment.
Contiguous availability decides whether sitting down pays off.

## Fusion

```
wD = clamp(0.40 + 0.25 × (1 − window minutes / 120), 0.40, 0.65)
wS = 1 − wD
F  = wS × S' + wD × D
```

| Window | wS (the room) | wD (the walk) |
|---|---|---|
| 20 minute break | 0.35 | 0.65 |
| 60 minutes | 0.475 | 0.525 |
| 120 minutes and above | 0.60 | 0.40 |

A short window settles reachability first. A long window starts to be picky about the room.

**Order.** Sort by `F` descending, break ties on `S'`, break remaining ties on room id ascending.
Identical input returns identical output, so a disagreement traces to the profile or to the
snapshot.

### The procedure

```
function pickStudyRoom(W, p, n, u, rooms, snapshot):
    table = ensureTable(u.prefHash, snapshot.roomVersion)
    cands = []
    for r in rooms:
        if not passesFilters(r, W, p, n, u, snapshot): continue
        row   = table.rows[r.id]
        S2    = row.base * pow(1 - 0.12, effReject(row, now))
        total = walk(p, r) + walk(r, n)          # a missing side counts 0
        k     = clamp(0.6 * slack(W, p), 3, 12)
        T     = exp(-total / k)
        C     = min(contiguousFree(r, W.t0) / 120, 1)
        D     = 0.65*T + 0.35*C
        wD    = clamp(0.40 + 0.25*(1 - len(W)/120), 0.40, 0.65)
        F     = (1-wD)*S2 + wD*D
        cands.push({r, F, S2, T, C, D})

    sort cands by F desc, S2 desc, id asc
    return cands[0], cands[1..3]
```

Each window costs `O(|R|)`, which is one pass over the candidate set. Eight windows over 200 rooms
is about 1600 evaluations, and that fits in a millisecond budget on a phone.

## Worked example

Window 11:00 to 12:00, with the previous class ending at 10:45 in KB 223. That leaves `slack = 13`
and `κ = 7.8`. The next class starts at 14:00 in CPD and only gates reachability.

| Room | S | Effective rejections | S' | total | T | C | D | F |
|---|---|---|---|---|---|---|---|---|
| KB 227 | 0.58 | 2.0 | 0.45 | 7 minutes | 0.41 | 0.50 | 0.44 | 0.46 |
| LIB 3F | 0.82 | 0 | 0.82 | 21 minutes | 0.07 | 0.67 | 0.28 | **0.54** |
| RHT 302 | 0.69 | 0 | 0.69 | 14 minutes | 0.17 | 0.50 | 0.29 | 0.48 |

LIB 3F has the best room and takes first place, at a price of 21 minutes of walking. KB 227 sits
downstairs, carries two rejections and offers an ordinary room, so it lands second.

That outcome is a trade between the room and the walk. With a long window, a good room beats a near
room. Shrink the window to 20 minutes and `wD` rises to 0.65 while `κ` falls to 3, so KB 227 moves
to first place. Same rooms, same student, two windows, two answers. The weighting exists to produce
exactly this.

## Parameters

| Parameter | Default | Range | Effect |
|---|---|---|---|
| Rejection discount | 0.88 per rejection | 0.82 to 0.94 | how far one rejection lowers the score |
| Rejection half-life | 21 days | 7 to 60 | how fast an old rejection fades |
| `κ` coefficient | 0.6 × slack | 3 to 12 | steepness of the walk penalty |
| `C` full score | 120 minutes | 60 to 180 | how long counts as long enough to sit |
| `wD` range | 0.40 to 0.65 | set by the window | split between the walk and the room |
| Familiarity cap | 10 visits | 5 to 20 | keeps a student from settling in one building |

Every row has the shape of one parameter plus one counter. A default is replaced after three
observations, and a manual edit clears the counter so learning restarts.

## Failure modes

**Lock-in on one building.** Familiarity caps at 10 visits and carries weight 0.20. Habit lifts one
room by at most 0.2 over the whole score. Location and quiet outweigh it.

**A rejection that buries a room.** The product stays above zero, and eight effective rejections
leave 36%. A renovated room can return to the shortlist.

**A window too short to fill.** Below 25 minutes the window produces no study stop. The planner
merges it into a neighbour or skips it.

**A stale snapshot.** `contiguousFree` reads the snapshot, which carries a fetch time. After ten
minutes without a refresh, `C` drops to 0.5 and the card marks the room unverified.

**Missing air conditioning or daylight data.** A missing value scores the neutral 0.5. That keeps a
new room free of a systematic penalty.

## Relationship to the planning layer

Scoring answers one question for one window, which room. The planning layer decides what the window
holds, how long it lasts and when the student leaves. A room change keeps the time, a time change
keeps the room, and each action carries its own undo record.

The static table belongs to the room. [`adr/0005`](adr/0005-room-attributes-are-scored-on-the-server.md)
places the six attributes it reads on the server and caches them on the device by revision.
