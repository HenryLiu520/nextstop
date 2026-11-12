# Screens and Interaction Rules

**English** · [简体中文](screens.zh-CN.md) · [繁體中文](screens.zh-TW.md)

NextStop is one page with four tabs. A day is a column of blocks with walking
time in the seams between them. Every action lives inside the block it changes.

This is the contract that `product/demo/index.html` already obeys. Where the
prototype and this text disagree, the prototype wins and this file gets fixed.

## 1. Four tabs, nothing else

There is no sidebar, no toolbar, no floating action button, no search field and
no settings drawer. The header is a wordmark and one line of date. Under the day list sits one
line that takes a sentence, on Day and on Plan. Anything that is
not a tab, a block, a seam or a sheet is missing on purpose.

| # | Tab | Holds | The one job | Never contains |
|---|---|---|---|---|
| 1 | **Day** | Blocks, seams, block actions, End the day / Stay longer | 99% of opens. You look, you tap, you move | Forms, text inputs, chat history |
| 2 | **Plan** | Week strip with load bars, tap a day, one sentence, preview, apply | The only place the future can be changed | Month grid, auto-opening keyboard, transcript |
| 3 | **Find** | The older lookup page, embedded whole, with the free rooms of a moment | Look a room up without leaving the day | Filters, maps, favourites |
| 4 | **Me** | Learned defaults: walk speed, usual Thursday end, usual lunch spot | First run, and later when you want to correct it | Streaks, ratings, surveys |

The bottom bar is 56px tall, page colour, one 1px top border, no icons, no raised
tab. The active tab is ink `#1C1A17` with a 4px vermilion dot centred under the
label, 12px from the bottom. Tab labels are 11.5px with .1em letter spacing.

## 2. A day is blocks and seams

```
  NextStop                        Something changed? Say it
  Thursday · 30 Oct
  ------------------------------------------------
  | 09:00-10:50  Class    COMP1110 · KB             |   past, 40% opacity
       :  Walk 5 min
  +-----------------------------------------------+
  |# 11:00-12:00  Study         KB 227           . |   current, enlarged
  |  You're here · until 12:00                     |
  |  [ Change ]                                    |
  +-----------------------------------------------+
       :  Walk 10 min
  : 12:10-12:40  Lunch    Chong Yuet Ming          :   dotted, not booked
       :  Walk 10 min
  +-----------------------------------------------+
  |# 14:00-15:50  Class    CPD · LG05              |
  |  [ Skip ]                                      |
  +-----------------------------------------------+
       ...

       [ End the day ]   [ Stay longer ]
```

### 2.1 Block anatomy

| Element | Spec |
|---|---|
| Left bar | 4px wide, inset 11px from top and bottom, radius 2, colour by type |
| Type colours | Class `#1C1A17`, Study `#2F5D9E`, Lunch `#B4763A`, Meeting `#6A4C93` |
| Time | Georgia, 14.5px, `tabular-nums`, written `HH:MM-HH:MM` |
| Type label | 11px, `#6B655C`, 9px to the right of the time |
| Place | 13.5px, weight 600, pushed to the right edge |
| Box | White, 1px `#E2DCCF`, radius 12, no shadow, padding 12 / 14 / 12 / 18 |
| Action row | Inside the block, 10px below the last line, 9px gaps between buttons |

### 2.2 The three states

| State | Border | Fill | Means |
|---|---|---|---|
| Solid | 1px `#E2DCCF` | White | The planner solved it, so it is ours to move |
| Dashed | 1.5px `#C9C0AE` | White | An external fact: a class you cannot move, a room already booked |
| Dotted | 1.5px `#BDB4A4` | Transparent | Waiting to be booked, or waiting to be confirmed |

Two more appearances sit on top of those states.

A past block drops to 40% opacity and strikes through its time. It stays in the
column so the day keeps its shape, and tapping it answers "That block is already
gone" instead of opening a sheet.

The current block is the only enlarged one. Time goes to 26px, the left bar to
5px, padding grows by 3px, a 6px vermilion dot lands after the place, and a new
line reads "You're here · until 12:00". Exactly one block can hold that dot.

### 2.3 Seams

The only text a seam ever carries is "Walk N min". It is a 16px dotted vertical
rail at 1.5px `#C9C0AE`, the label 10.5px `#8C8478`, 9px of air above and below,
no card, no background, no radius. Width is next start minus previous end,
rounded to 5 minutes, with a floor of 5.

A seam never says "wait", never says "free", never says "nothing planned". Free
time is not a gap between blocks. Free time becomes a study block.

## 3. Action matrix

Rows are block types. Columns are the actions reachable from that block. `block`
means the button sits on the block itself. `sheet` means it is a row inside the
sheet that opens when you tap the block. `no` means it is never offered.

| Block | Skip | Change place | Book it | Nudge time | Remove |
|---|---|---|---|---|---|
| **Class** | `block` | `no` | `no` | `sheet` (±15, ±30) | `sheet` |
| **Study** | `no` | `block` | `no` | `no` | `sheet` |
| **Lunch** | `no` | `block` | `no` | `sheet` (±15, +30) | `sheet` |
| **Meeting** | `no` | `block` | `block` | `sheet` (±15) | `sheet` |
| **End of day** | `no` | `no` | `no` | `no` | `no` |

The last row replaces the whole set with two buttons under the final block:
`End the day` and `Stay longer`. Both are 1px ink outline pills. Neither uses
vermilion, because the dot on the current block already claimed it.

A class block shows exactly one button, `Skip`, which reads as "I'm not going".
Skipping does not delete the hour. The hour turns into study time and the seams
recompute around it.

Lateness is not a page level action. "Something changed?" opens a sheet with two
chips, "the class ran 20 min late" and "I am 10 min behind". Both shorten the
block you are standing in and offer one extra link, "see full recompute", which
exists only so a local repair can be compared against a global one.

## 4. Sheet and undo bar

| Part | Spec |
|---|---|
| Sheet | Rises from the bottom, top corners radius 18, page colour `#FAF7F2`, 20px sides, 26px bottom, max height 76%, scrolls inside |
| Veil | `rgba(28,26,23,.28)` across the page, tap anywhere to close |
| Title | 13px weight 600, reads `14:00-15:50 · Class` |
| Rows | 13px, 11px vertical padding, times in Georgia, the value a row would produce sits right |
| Picker | Exactly 3 candidates plus one `Change` for another batch, never 10, never a repeat |
| Undo bar | Above the nav (bottom 66px), 16px side inset, white, 1px `#E2DCCF`, radius 12, one sentence, vermilion `Undo` pushed right |

| Action | Undo bar copy | Undo restores |
|---|---|---|
| Change place | `Moved to Chi Wah B1 · only this block changed` | Previous place |
| Skip class | `Skipped · that time is now study` | The class |
| Book | `Booked LIB 3/F Room 2 · 16:00-16:50` | The unbooked block |
| Remove | `Remove this item` | The block |
| Repair | `One block changed · the rest untouched (ends 11:40)` | Previous end time |
| End day | `That is the day · Thursdays usually end at 20:00` | The day |

The bar lives for 6 seconds and then leaves. One bar at a time, so a second
action replaces the copy rather than stacking a second bar. Undo is a link, never
a button, and it is the only vermilion on the screen while it is visible.

## 5. Design tokens

| Use | Value |
|---|---|
| Page | `#FAF7F2` |
| Block | `#FFFFFF` with 1px `#E2DCCF`, no shadow |
| Ink | `#1C1A17` |
| Secondary | `#6B655C` |
| Muted | `#8C8478` |
| Accent | `#C8452D` vermilion, once per screen |
| Type colours | Class `#1C1A17`, Study `#2F5D9E`, Lunch `#B4763A`, Meeting `#6A4C93` |
| Latin type | Georgia, serif |
| Chinese type | Songti SC, falling back to Georgia |
| Numerals | `tabular-nums` everywhere a clock time appears |
| Button | 34px tall, radius 999, 1px `#1C1A17`, 12.5px label, .05em spacing |
| Radius | Block 12, button 999, sheet 18 |
| Grid | 20px page margin, 8px rhythm |

## 6. Three rules

**Actions belong to the block, not to the page.** A button is never parked at the
bottom of the screen hoping to apply to whatever you last touched. If you cannot
see the thing it will change from where the button sits, the button is wrong.

**Vermilion appears once per screen.** It marks the one live thing: the dot on the
current block, the active tab dot, the undo link, or a single warning. Two red
marks on one screen means the design has stopped answering "where am I now".

**There is no such thing as empty time.** The planner does not leave holes and
the interface never draws one. Unbooked hours become study, and study blocks
stretch to fill the seam. Allowing holes would turn the product back into a
timetable viewer.

## 7. What the two screenshots prove

The Day tab carries four different block states in one column, and the seams
carry nothing but walking time between them:

![Day tab, four block states and walking seams](../product/day.png)

The repair state shows the undo bar above the nav with one sentence and one
vermilion link, while only the block you are standing in has changed:

![Repair state, undo bar above the nav](../product/repair.png)

Together they show that a state change is legible without colour coding every
block, and that the undo affordance costs one line of text rather than a modal.
