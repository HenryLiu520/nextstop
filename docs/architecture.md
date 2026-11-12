# Architecture

**English** · [简体中文](architecture.zh-CN.md) · [繁體中文](architecture.zh-TW.md)

NextStop answers one question: *where should this person be, and when should they move.*
The system that answers it is split in two halves that never trade places.

The server is a **data layer**. It knows the timetable and the rooms and answers "what is free".
The clients are a **plan layer**. They decide what to do with the free time, keep that decision,
and repair it when the day moves. The server stores no plan and has no opinion about one.

## 1. Overview

```
+----------------------------+      +----------------------------+
| desktop client   :5173     |      | phone client   :5174       |
| Vue 3, three roles         |      | Vue 3, student view        |
+--------------+-------------+      +--------------+-------------+
               |                                   |
               +-----------------+-----------------+
                                 |
                                 v
+--------------------------------------------------------------------+
| PLAN LAYER   in the browser tab, one per client                    |
| state machine: planning / executing / deviated / repairing         |
| owns the plan, slot filling, repair, undo, persists nothing        |
+--------------------------------------------------------------------+
                                 |  REST + JSON, /api, Bearer token
                                 v
+--------------------------------------------------------------------+
| DATA LAYER   Spring Boot 4.1.1 on Java 21, port 8080               |
| timetable, rooms, availability, reviews, room updates,             |
| audit log, accounts, roles user < admin < superadmin               |
| stores no plan, decides nothing about where you go                 |
+--------------------------------------------------------------------+
                                 |  JDBC, Flyway migrations V1 .. V20
                                 v
+--------------------------------------------------------------------+
| DATABASE   PostgreSQL 17, port 5433                                |
+--------------------------------------------------------------------+
                                 ^  seeded and imported by hand
+--------------------------------------------------------------------+
| SCHOOL FEED   mock-school-api, simulated timetable                 |
+--------------------------------------------------------------------+
```

NextStop is a phone app. The client is `product/demo/index.html`, one file carrying four views, Day,
Plan, Find and Mine, in that order along the bottom bar. The Find view is the only one that asks the service a
question of its own, and it asks `GET /api/availability` for the rooms that are free across a whole
interval, so what it lists and what the day screen trusts come from the same arithmetic.

## 2. Why the split is where it is

The split follows one test: **is this fact shared, or is it personal?** A plan belongs to one
person and lives for a few hours. The timetable is shared by thousands of people, lives for a
semester, and is written by an office nobody here controls. Different problems, different homes.

| Property | The plan | The timetable |
|---|---|---|
| Owner | one student | the institution |
| Lifetime | one day | one semester |
| Change rate | many edits per session | fixed once published |
| Truth | a proposal, always negotiable | final, never questioned |
| Read/write | read-write, locally | read-only from our side |
| Needed offline | yes, the corridor has no signal | no, a cache is enough |

**Computation goes to the client** because the plan is short lived, personal and volatile. Every
edit would otherwise be a round trip and a server row, and every change to the planning rules a
migration. Repair is cheap in CPU and expensive in coordination: it compares candidate stops
against a plan that only the client holds.

**Data stays on the server** because the timetable must not differ between two people looking at
the same room. Clients on two versions would otherwise disagree about whether a room is free,
and nothing could settle the argument. The server is the only place where "2A is occupied 10:00
to 11:00" means one thing.

The server is smaller than it looks: it answers "can I go here" and gets out of the way.

## 3. The data layer

Spring Boot 4.1.1 on Java 21, talking to PostgreSQL 17 on port 5433. Flyway manages the schema,
V1 through V20. The service is covered by 32 automated tests that run against a real database.

| Method | Path | Who | Purpose |
|---|---|---|---|
| POST | `/api/auth/login` | anyone | exchange credentials for a token |
| POST | `/api/auth/register` | anyone | create a `user` account |
| GET | `/api/auth/me` | token | current identity and role |
| GET | `/api/availability?date=&building=&from=&to=` | token | rooms free in a window |
| GET | `/api/rooms`, `/api/buildings` | token | rooms with attributes, buildings |
| GET | `/api/rooms/{code}/timeline` | token | occupied blocks of one room, one day |
| PATCH | `/api/rooms/{code}` | admin and up | room metadata |
| GET, POST | `/api/rooms/{code}/reviews` | token | read or post a review |
| GET | `/api/reviews/summary` | token | aggregate ratings |
| POST | `/api/updates` | admin and up | occupancy change, with reason and expiry |
| GET | `/api/updates`, `/api/updates/mine` | token | active changes, own changes |
| DELETE | `/api/updates/{id}` | admin and up | withdraw a change |
| GET | `/api/audit` | admin and up | audit log of privileged actions |
| GET, POST, PATCH, DELETE | `/api/accounts` | admin and superadmin | account administration |
| GET | `/api/about` | anyone | build and service metadata |

Roles are ordered `user < admin < superadmin`. The order is enforced on the server, once, and
the clients only hide what the token could not use.

### The one real computation

`GET /api/availability` is not a table lookup. It is:

```
free(room, date) = blocks(room, date) - blocks taken by a class
                 - blocks taken by an active room update
                 - 10 minutes either side of every occupied block
```

**A 10 minute changeover margin.** A class that ends at 11:00 occupies the room until 11:10 as
far as availability is concerned. The margin applies on both edges of every occupied block, so
a request starting at 10:55 comes back empty. The value is server configuration with a default
of 10, never a client setting: both clients must agree about the same room.

**The timetable always wins.** A room update can take a room out of service, with a reason and
an expiry, posted by an admin. It cannot take a room out of a class. A change overlapping a
class block is rejected with HTTP 400 and the code `TIMETABLE_PRIORITY`: the schedule is the
ground truth, and an admin note is an annotation.

Updates carry a reason and an expiry so they read as a claim about the world rather than a
permanent edit. When the expiry passes the block stops counting, so nothing is cleaned up later.

### Room attributes

Room quality comes from one dataset on the server, so two clients rank the same room the same way.
The static score is computed on the device from these values and the weights in
[`room-scoring.md`](room-scoring.md).

```
GET /v1/rooms/attributes
{ "rev": 14,
  "updatedAt": "2026-11-11T09:00:00Z",
  "rooms": [ { "code": "KB227", "quiet": 4, "desk": 3, "sockets": 3,
               "ac": 5, "spaciousness": 4, "daylight": 3 } ] }
```

Six dimensions per room, each an integer from 0 to 5: `quiet`, `desk`, `sockets`, `ac`,
`spaciousness`, `daylight`. The anchors at 0, 3 and 5 sit in
[`adr/0005-room-attributes-are-scored-on-the-server.md`](adr/0005-room-attributes-are-scored-on-the-server.md).

**Cache semantics.** `rev` is the cache key. The client stores the document in IndexedDB under the
revision it arrived with, compares that revision at startup, and refetches when the server reports
a different one. A matching revision costs one conditional request and no download.

**Refresh cadence.** A full audit runs once a semester. A renovation, a replaced desk or a new
socket strip increments `rev` on the day the change is recorded. `updatedAt` carries the date, so a
client can age its copy.

**Degradation.** A failed fetch falls back to the copy in IndexedDB. A device that holds no copy
scores every dimension at 0.5, the neutral value, and marks the affected cards 未核实 (unverified).
A room with no record takes 0.5 the same way.

## 4. The plan layer

The plan layer is client code. In the prototype it is a single HTML file and the planner behind
it is still a stub. What is real is the state it manages.

A plan is a sequence of stops for one day, each with a start, a duration, a place and a purpose.

| State | Entered when | What the user sees | Leaves to |
|---|---|---|---|
| `planning` | the day is revealed, or an edit is made | the day as a stack of blocks | `executing` |
| `executing` | the user starts following the plan | the current stop, one card at a time | `deviated`, `planning` |
| `deviated` | reality diverges: a class overruns, a stop is skipped | a notice and a fix | `repairing`, `planning` |
| `repairing` | the user accepts the fix | the plan with the fewest changed stops | `executing`, `planning` |

Stops are typed, and the type decides how much freedom repair has:

| Stop type | Movable | Removable | Notes |
|---|---|---|---|
| `class` | no | only by the user | an anchor, copied from the timetable |
| `study` | yes, within its gap | yes | the default filler |
| `meal` | yes, within a window | no | fixed to a meal band |
| `meeting` | yes | no | other people are in it |
| `custom` | yes | yes | added by hand, for anything |

The system never touches a `class` stop. Skipping one is the user's decision, and reversible.

**Why no plan is stored on the server.** A plans table would need a user, a version, a sync rule
and a conflict story, and it would still lose to a phone closed without saving. The plan is one
day long, and rebuilding it costs one round trip and no coordination. There is no endpoint that
accepts a plan, no column that holds one, and no migration to write when a stop changes shape.

The minimum-disturbance repair algorithm that should sit behind the `repairing` state is
specified in [`algorithm.md`](algorithm.md). It is designed and not yet implemented.

## 5. How they connect

The contract is REST over HTTP with JSON bodies, on one base path, `/api`.

- **Auth.** `POST /api/auth/login` returns a token and a role. Both clients send it in the
  `Authorization` header on every call, prefixed with the `Bearer` scheme. The token lives in
  memory, so a reload signs the user out.
- **Query shape.** Availability is always asked as a window, never as a dump. The client sends a
  date, a building and the hours it plans into, so the answer is a short list of rooms.
- **Metadata, not the timetable.** Clients ask *"is this room free now"*, not *"give me every
  class"*. One request per gap being filled, one more when a plan is repaired. Nothing
  subscribes to anything.
- **Freshness.** The client stamps every availability answer with its arrival time. Before
  repairing around a stop it checks that the answer is newer than the last plan edit, and
  refetches if not. A plan built on stale availability is worse than no plan at all.
- **Failure and degradation.** If the data layer is unreachable the plan keeps rendering,
  because it lives in the client. What stops is anything needing a fact from outside: lookups
  fail, the plan is marked unverified, repair refuses to move a stop into an unconfirmed room.
  The client never invents a room to keep the interface moving.

| Condition | Client behaviour |
|---|---|
| 401 or expired token | sign out, return to login, keep nothing |
| 403, role too low | hide the action, no retry |
| 400 `TIMETABLE_PRIORITY` | show the conflicting class, keep the plan as it was |
| network error on availability | mark the affected stops unverified |
| empty availability list | offer a neighbouring building, never an unknown room |

No session state lives on the server beyond the token, so a client can resume by asking again.

## 6. Two clients, one contract

Two Vue 3 applications: a desktop client on port 5173 covering all three roles, and a phone
client on port 5174 covering the student view. They are separate builds because the layouts have
little in common. Underneath they are one client in two shapes.

| | Desktop, 5173 | Phone, 5174 |
|---|---|---|
| Roles | user, admin, superadmin | user |
| Screens | day, plan, rooms, reviews, admin, audit | day, plan, mine |
| API | `/api` | `/api`, the same one |
| Auth | the same token format | the same token format |
| Rules | enforced on the server | enforced on the server |

**No rule is written twice.** The 10 minute margin, the timetable priority, the role ordering and
the audit writes all live in the Spring service. Neither client re-implements them and neither is
trusted to. A rule written in both would eventually diverge, and the version users saw would be
the wrong one. Changing the margin is one configured value, and both clients get it next request.

What they do duplicate is presentation: one answer as a phone card, one as a desktop row.

## 7. Deployment shape

Three processes and one database, all on one machine for development:

| Process | Port | What it is |
|---|---|---|
| PostgreSQL 17 | 5433 | the database, migrations applied on boot |
| Spring Boot 4.1.1 | 8080 | the data layer |
| Vue 3 desktop | 5173 | a Vite dev server |
| Vue 3 phone | 5174 | a Vite dev server |

`scripts/start-all.sh` brings them up in that order: database, service, then the two clients.
`scripts/test.sh` runs the 32 automated tests. The first run applies V1 through V20 and takes a
few minutes.

Docker is optional. `docker-compose.yml` packages the same three processes plus the database for
a machine that would rather not install Java and Node. The layout is identical: the plan layer is
served as static files, so it does not care where the data runs.

## Delivery targets

One build, four containers. `web/` is the frontend being built now. It has one `vite.config.js`.
`vite build` writes `dist/`, and every target loads that same directory.

What exists today is narrower than the plan. `product/demo/index.html` is a single file prototype
and it runs by opening the file. `web/` has not produced `dist/` yet. There is no manifest, no
service worker and no Capacitor project in the repository. The table below says so.

```
web/  Vue 3 + Vite, one vite.config.js
  |
  |  vite build        (not run yet, dist/ does not exist)
  v
dist/  one artifact, four containers
  |
  +--> browser     a tab, static files over HTTP        prototype runs today
  +--> PWA         manifest + service worker            not built
  +--> Android     cap sync into android/, then APK     not built
  +--> iOS         cap sync into ios/, then IPA         not built
```

| Target | Container | What it adds | State |
|---|---|---|---|
| browser | tab | nothing | prototype runs today |
| PWA | tab, installed | manifest + service worker + offline shell | not built |
| Android | Capacitor shell | APK | not built |
| iOS | Capacitor shell | IPA | not built |

### The build chain

- One Vite config, in `web/`. `vite build` writes `dist/`. No target gets a second config.
- PWA is a plugin, not a second build. `vite-plugin-pwa` generates the manifest and the precache
  list during `vite build`. The manifest and the service worker are output, never hand written,
  so a file added to the build is a file in the cache without anyone editing a list.
- Capacitor takes `dist/` as input. `capacitor.config.json` sets `webDir` to `dist`, and
  `npx cap sync` copies that directory into `android/` and `ios/` and refreshes the native
  plugins. The copy is the step that makes a browser build a native app.
- A signed package is then a platform command, not a second frontend. `npx cap open android`
  and a Gradle build for the APK, Xcode archive for the IPA.
- Consequence: `dist/` is the only artifact. A fix lands in all four containers by being in that
  one directory, and a difference between targets is a CSS rule or a capability check.

### One version, four places

The version has to agree in four places that are built at four different times. It is read from
one place.

- `web/package.json` holds `version` and it is the only version string in the repository.
- Vite injects it into the bundle at build time with `define`, so a running client knows what it
  is. It reaches the colophon and the diagnostics without a fetch.
- The service worker cache name is built from the same value. A new version is a new cache, and
  the old one is dropped on activate. This is what stops a stale shell from surviving a deploy.
- Capacitor reads the same value at `cap sync` for `versionName` and `versionCode` on Android
  and for `CFBundleShortVersionString` on iOS. Nobody types a version into a native project.
- `GET /api/about` is meant to report it too, so a client can compare the build it is running
  with the build the server was compiled against. Not wired yet: `AboutController` returns
  project, course, group, author, uid, signature and year, and no `version` field.
- Consequence: a version bump is one edit, and a client that is older than the server is
  detectable instead of inferred.

### Safe areas

A notch, a rounded corner and a bottom gesture bar sit over the webview. The layout has to pad
itself or the first and last row of content are unreadable.

- `web/index.html` already carries `viewport-fit=cover` in the viewport meta. This is the
  precondition: without it every `env(safe-area-inset-*)` resolves to zero and the padding
  silently does nothing.
- Padding is applied where the layout touches a screen edge, and only there. The top bar takes
  `env(safe-area-inset-top)`. The bottom navigation and every sheet take
  `env(safe-area-inset-bottom)`. `web/src/components/BottomSheet.vue` has the bottom one today.
- Left and right insets matter in landscape. They are padding on the same containers, not a
  separate layout.
- The values are read in CSS at use site, so a new edge element pads itself rather than
  inheriting a fixed number. A hard coded `padding-bottom: 34px` would be wrong on the next
  device.
- The same rule covers the shells and the browser. In a Capacitor shell the webview is
  full screen by default, so the safe area is the only thing between the status bar and the
  first line of text.

### Two browser behaviours

Both are browser facts, so both apply in the tab and inside the shells, which run the same engine.

- **Rubber banding.** `overscroll-behavior: contain` stops the page bouncing past its end and
  stops pull to refresh from reloading a plan mid day. Already set in `web/src/styles/base.css`
  and `web/src/App.vue`, and on the scroll container rather than on `body`.
- **The keyboard.** The on screen keyboard does not resize the layout viewport on iOS. The
  layout believes it still has the full height, and the focused input sits behind the keyboard.
  The fix is `window.visualViewport`: listen for `resize` and `scroll`, read `height` and
  `offsetTop`, and scroll the focused field into that rectangle. Not written yet.

### What offline covers, and what it does not

Offline is inherited from the plan living in the client ([§4](#4-the-plan-layer)), not from a
separate offline feature. The service worker adds a shell, not a data store.

- Cached: the shell. The app opens with no network and renders.
- Cached: the last availability answer per query, kept with the time it was fetched.
- Not cached, because it cannot be known: a room update posted after the cached answer, a class
  moved by the office, a review written by someone else. The corridor has no signal, so the
  answer there is the one from the last fetch.
- The client says which it is. A cached answer is served with its fetch time, and stops that
  depend on it are marked unverified, following [§5](#5-how-they-connect). A cached answer is
  never dressed as a current one.
- A new plan is not built offline. Repair may move a stop only into a room confirmed by a fresh
  answer. The plan keeps rendering either way, because it is local.
- Writes fail. A review posted with no network returns an error and is not queued. There is no
  background sync and no outbox, because the plan is the only state that must survive, and it
  never leaves the device.

### Why Capacitor and not a hand written WebView

The interface is ordinary: cards, lists, a timeline, a form. Nothing in it needs native drawing.
So the native layer should be as small as possible.

- Capacitor provides exactly the container, the app icon, the package name, the signing path and
  a narrow bridge to native calls. It does not provide any UI, and it does not expect any.
- It does not rewrite the interface. The four containers run the same components, the same CSS
  and the same copy of the planning rules. There is no target with its own screens.
- The alternative, a `WebView` activity on Android and a `WKWebView` controller on iOS written
  by hand, produces the same shipped app. It also produces the file copying, the asset loader,
  the bridge and the version plumbing, twice, as our code to maintain.
- The bridge stays narrow on purpose: status bar colour, back button handling. Every plugin is
  native code per platform, so a new native capability is a small twice written fragment even
  when the UI is written once.

The decision and its costs are recorded in
[`adr/0004-one-ui-four-targets.md`](adr/0004-one-ui-four-targets.md).

## 8. What is not built yet

Stated plainly, so nothing above is read as a claim:

- **Server side planning.** None exists: no endpoint accepts or returns a plan.
- **Multi user sync.** Devices do not share a plan, so a plan made on the desktop is not on the phone.
- **The repair algorithm.** The prototype planner is a stub. The minimum-disturbance algorithm in
  [`algorithm.md`](algorithm.md) is specified and not implemented, so the one-block-versus-five-block
  behaviour in the prototype is an illustration, not a result.
- **A real school feed.** `mock-school-api` simulates the timetable and `V2__seed.sql` seeds a
  plausible one. The University's endpoint is not connected.
- **Production auth.** Tokens in memory, salted SHA-256 passwords, no HTTPS. Prototype grade.

The split described here is real and running. What sits on top of the plan layer, and what a
real timetable feed would look like, are the next pieces.
