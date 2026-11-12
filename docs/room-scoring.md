# Room scoring

**English** · [简体中文](room-scoring.zh-CN.md) · [繁體中文](room-scoring.zh-TW.md)

How one room is chosen for one window of the day. The question is narrow on purpose: given a gap
between two classes, which room earns the walk.

Sections follow the order the numbers are computed. Step 0 builds a table that sits on the device.
Step 1 removes rooms that cannot serve the window at all. Step 2 scores the two facts that change
with the hour. Step 3 blends the two scores with a weight that follows the length of the window.
Step 4 takes the first room and keeps three more for the swap action.

Two parts of this document carry different status. The static score is implemented and reads the
review table on the server. The rejection model, the dynamic score and the fusion are specified with
design values and no shipped code.

## 01 Pipeline

```
STEP 0   static table S        one row per room, written on a preference change or a rejection
STEP 1   hard filters          free for the whole window, allowed to enter, not the room just left, reachable
STEP 2   dynamic score D       walk time, contiguous minutes free
STEP 3   fusion F              weight slides with the window length
STEP 4   first place           the pick, plus three alternatives for the swap action
```

Each step narrows the set or adds one number. Nothing in the pipeline reads the clock twice, so a
rerun on the same inputs returns the same room.

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

## 02 Static score S

Seven factors, each read from the point of view of one student sitting down in one room. The weights
sum to 1.00.

| Factor | Question it answers | Weight |
|---|---|---|
| Familiarity | have I been in this building before, and how often | 0.20 |
| Quiet | is it loud in here | 0.20 |
| Desk and seat | does a laptop and a book fit side by side | 0.15 |
| Sockets | is there power at the seat | 0.15 |
| Air conditioning | can a Hong Kong summer be survived here | 0.10 |
| Space | 40 to 80 seats reads comfortable, both ends read wrong | 0.10 |
| Daylight | is there an outside window, so the lights stay off at noon | 0.10 |

```
S = Σ w_i × f_i
```

Familiarity and building history are one fact, so they carry one weight. The visit count caps at
ten, which stops one building from absorbing the whole score.

**Three factors stay out of the table.** Purpose fit is answered by quiet, since a student looking
for a study room wants a quiet seat. Capacity enters through space, where 40 to 80 seats scores
best. Floor enters through travel time, since a floor changes the lift wait.

### How the six reviewable factors are measured

A student rates six of the seven factors on a whole number from 1 to 10 when writing a review. The
server stores each factor in its own column, so a rating is never parsed out of prose.

```
f_i = mean(reviews on factor i) / 10        missing factor, weight redistributed
```

The overall score in a review is kept for display and does not feed the ranking, because a single
number carries no information about which quality moved. Familiarity is dropped on the server side,
since it belongs to one account and a shared endpoint has no account to read.

Six weights renormalise to 0.80, and `GET /api/rooms/overview` reports the result:

| Factor | Table weight | Renormalised |
|---|---|---|
| Quiet | 0.20 | 0.25 |
| Desk and seat | 0.15 | 0.1875 |
| Sockets | 0.15 | 0.1875 |
| Air conditioning | 0.10 | 0.125 |
| Space | 0.10 | 0.125 |
| Daylight | 0.10 | 0.125 |

The endpoint prints the result on the ten point scale, which is ten times `S`. A room with no
reviews reports a null score, and the interface prints a dash. Printed as zero it would sink to the
bottom of a list it never entered.

### Where the table lives

200 rows, about twenty KB, in IndexedDB, keyed by a preference hash and a room revision number.
Three events write to it, and nothing else does.

- A preference change recomputes the whole table, one pass over 200 rows, and leaves the rejection
  counts untouched.
- A rejection writes one row.
- A room attribute update recomputes the rows that changed.

Decay is lazy. A read computes `n_eff_now = n_eff × e^(−(now − nat) / 21 days)`, and the value goes
back to disk at the next save. No timer runs in the background.

A published plan keeps the room it already chose. The table updates at once and the plan reads the
new values at the next reveal, which keeps the read-only rule in [`algorithm.md`](algorithm.md).

## 03 Rejection

A rejection is a long-term discount on the static score, applied when the score is read.

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

Multiplication gives three properties that matter in use. Each rejection lowers the score a little,
which is what a student means by the gesture. A room rejected many times sinks without reaching
zero, so a renovated room can return. An undo lifts the score one step, so a change of mind costs no
history reset.

A tap on **Dislike** counts 1.0, moving to another room counts 0.5, arriving then leaving soon after
counts 0.3. Repeats inside thirty days accumulate on their own, so no extra multiplier is needed.

## 04 Dynamic score D

Two factors, weights summing to 1.00, both read the window and the two anchors.

### Travel time

`t_prev` is the walk from the previous anchor to the room, `t_next` the walk from the room to the
next anchor. A missing anchor contributes 0.

```
slack = W.t0 − p.end − 2 minutes          the buffer comes off the top
total = t_prev + t_next
κ     = clamp(0.6 × slack, 3, 12)
T     = exp(−total / κ)
```

Slack sets the price of a walking minute. A student arriving with five minutes to spare sits at
`κ = 3`, so three minutes of walking scores 0.37. With forty minutes of slack `κ` reaches 12, so
five minutes of walking still scores 0.66. Time tolerance lives inside this factor, which is why the
table carries no separate term for it.

Two tests gate feasibility before any score is computed. The walk in has to leave five minutes to
sit, `t_prev ≤ window minutes − 5`, and the walk out has to fit, `t_next ≤ next.start − t1 − 2`. A
candidate that fails either test leaves the set at once.

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

Walking carries the larger weight, because it is the cost the student pays at that moment.
Contiguous availability decides whether sitting down pays off at all.

## 05 Fusion

```
wD = clamp(0.40 + 0.25 × (1 − window minutes / 120), 0.40, 0.65)
wS = 1 − wD
F  = wS × S' + wD × D
```

| Window | wS, the room | wD, the walk | Reading |
|---|---|---|---|
| 20 minute break | 0.35 | 0.65 | settle reachability first |
| 60 minutes | 0.475 | 0.525 | an even split |
| 120 minutes and above | 0.60 | 0.40 | start being picky about the room |

**Order.** Sort by `F` descending, break ties on `S'`, break remaining ties on room id ascending.
Identical input returns identical output, so a disagreement traces to the profile or to the snapshot.

```js
function pickStudyRoom(W, p, n, u, rooms, snapshot) {
  const table = ensureTable(u.prefHash, snapshot.roomVersion)
  const cands = []
  for (const r of rooms) {
    if (!passesFilters(r, W, p, n, u, snapshot)) continue
    const row = table.rows[r.id]
    const S2 = row.base * Math.pow(1 - 0.12, effReject(row, now))
    const total = walk(p, r) + walk(r, n)              // a missing side counts 0
    const k = clamp(0.6 * slack(W, p), 3, 12)
    const T = Math.exp(-total / k)
    const C = Math.min(contiguousFree(r, W.t0) / 120, 1)
    const D = 0.65 * T + 0.35 * C
    const wD = clamp(0.40 + 0.25 * (1 - len(W) / 120), 0.40, 0.65)
    cands.push({ r, F: (1 - wD) * S2 + wD * D, S2, T, C, D })
  }
  cands.sort((a, b) => b.F - a.F || b.S2 - a.S2 || a.r.id - b.r.id)
  return { pick: cands[0], swaps: cands.slice(1, 4) }
}
```

One window costs `O(|R|)`, a single pass over the candidate set. Eight windows over 200 rooms is
about 1600 evaluations, which fits in a millisecond budget on a phone.

## 06 Worked example

Window 11:00 to 12:00. The previous class ends at 10:45 in KB 223, so slack is 13 minutes and `κ`
is 7.8. The next class starts at 14:00 in CPD and only gates reachability. The window is 60 minutes,
so `wS` is 0.475 and `wD` is 0.525.

| Room | S | Effective rejections | S' | Walk | T | C | D | F |
|---|---|---|---|---|---|---|---|---|
| KB 227 | 0.58 | 2.0 | 0.45 | 7 min | 0.41 | 0.50 | 0.44 | 0.45 |
| LIB 3F | 0.82 | 0 | 0.82 | 21 min | 0.07 | 0.67 | 0.28 | **0.54** |
| RHT 302 | 0.69 | 0 | 0.69 | 14 min | 0.17 | 0.50 | 0.29 | 0.48 |

LIB 3F holds the best room and takes first place at a price of 21 minutes of walking. KB 227 sits
downstairs, carries two rejections and offers an ordinary room, so it lands third.

The outcome is a trade between the room and the walk. Shrink the window to 20 minutes and `wD` rises
to 0.65 while slack falls, so `κ` drops to 3 and KB 227 moves to first place. Same rooms, same
student, two windows, two answers. The weighting exists to produce exactly this.

## 07 Parameters

| Parameter | Default | Range | Effect |
|---|---|---|---|
| Rejection discount | 0.88 per rejection | 0.82 to 0.94 | how far one rejection lowers the score |
| Rejection half-life | 21 days | 7 to 60 | how fast an old rejection fades |
| `κ` coefficient | 0.6 × slack | clamped to 3 and 12 | steepness of the walk penalty |
| `C` full score | 120 minutes | 60 to 180 | how long counts as long enough to sit |
| `wD` range | 0.40 to 0.65 | set by the window | split between the walk and the room |
| Familiarity cap | 10 visits | 5 to 20 | keeps a student from settling in one building |
| Observation gate | 3 ratings | 3 to 10 | when a review average replaces the default |

Every learning row has the shape of one parameter plus one counter. A default is replaced after
three observations, and a manual edit clears the counter so learning restarts.

The parameters are design values with no measurement behind them. The demo prints `S'`, `T`, `C`,
`D` and `F` so the ordering can be checked by hand.

## Failure modes

**Lock-in on one building.** Familiarity caps at ten visits and carries weight 0.20, so habit lifts
one room by at most 0.2 over the whole score. Location and quiet outweigh it.

**A rejection that buries a room.** The product stays above zero, and eight effective rejections
leave 36%. A renovated room returns to the shortlist.

**A window too short to fill.** Below 25 minutes the window produces no study stop. The planner
merges it into a neighbour or skips it.

**A stale snapshot.** `contiguousFree` reads the snapshot, which carries a fetch time. After ten
minutes without a refresh `C` halves and the card marks the room unverified.

**A factor with no data.** A factor with no reviews is left out and its weight is spread over the
rest. A room with no reviews reports a null score and shows a dash.

## Relationship to the planning layer

Scoring answers one question for one window, which room. The planning layer decides what the window
holds, how long it lasts and when the student walks out. A room change keeps the time, a time change
keeps the room, and each action carries its own undo record.

The static table belongs to the room. [`adr/0005`](adr/0005-room-attributes-are-scored-on-the-server.md)
places the six attributes it reads on the server and caches them on the device by revision.
