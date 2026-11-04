# 0004. One UI, four delivery targets

**English** · [简体中文](0004-one-ui-four-targets.zh-CN.md) · [繁體中文](0004-one-ui-four-targets.zh-TW.md)

- Status: Accepted
- Date: 2026-11-04
- Scope: `web/`, `capacitor/`, `docs/architecture.md`

## Context

NextStop is reached from four places. A browser at a desk, a phone in a corridor, an Android
phone for a demo, an iPhone for the same demo. The interface behind all four is the same
interface: one card holding the next thing to do, a stack of blocks below it, three tabs, four
actions. One card, one time, one destination.

That interface is ordinary. Lists, cards, forms, tables, a timeline bar. It reads a JSON answer
from the data layer and it draws a plan. Nothing in it needs native drawing, native gestures or a
frame budget that a browser engine cannot hold. It is not a game and it is not a camera.

It is also already written. Vue 3 and Vite, one component tree, one set of styles, one client
copy of the planning rules. The cost of a second implementation is not the first screen. It is
every screen, twice, forever, in two languages, with two answers to the same question.

The physical facts decide the rest. The corridor has no signal, so the phone must open with no
network. A student should not have to type a URL, so the phone build must install to the home
screen. Two mobile platforms exist and a course demo must run on both.

Section 6 of [`architecture.md`](../architecture.md) describes two Vite builds, a desktop one and
a phone one, sharing one contract. That split is about layout, not about platform. This record
settles how many implementations exist and how many containers load them.

## Decision

One Vue 3 and Vite frontend. `vite build` writes `dist/`. That directory is the product.

Four delivery targets load it:

- **Browser.** `dist/` served as static files. The baseline, and the shape everything else
  falls back to.
- **PWA.** The same `dist/`, plus a manifest and a service worker. Installable to the home
  screen, and it opens with no network.
- **Android shell.** Capacitor copies `dist/` into a native project and produces an APK.
- **iOS shell.** The same Capacitor project, a second platform folder, and an IPA.

Capacitor wraps the build in a native container. It does not rewrite the UI. It gives the
webview a full screen, a package name, an app icon and a bridge to the few native calls we
need, such as status bar colour and back button handling. The four targets run the same
JavaScript, the same components and the same CSS.

What section 6 describes as two clients becomes one build with two layouts selected by
breakpoint. Nothing about permissions moves into the client. The data layer still decides who
may change room use time, and the interface can only offer what it is allowed to do.

There is one implementation of every control. A screen cannot differ between targets by
omission. If a button exists on the desktop it exists in the same code on the phone, and a
difference is either a CSS rule or a capability check, never a fork of the screen.

## Consequences

One fix reaches four targets. A corrected sign in form is corrected in the browser, in the PWA
and in both shells at once, because they load one artifact.

Every target ships together. The version string in `/api/about`, the service worker cache name
and the two shell builds all come from one `package.json`, so they cannot disagree.

Offline is inherited, not added. Because the plan lives in the client
([ADR 0002](0002-plan-lives-in-the-client.md)), the day is already local state. The service
worker caches the shell and the last availability answer, and the corridor case works. No
separate offline feature was written for the PWA.

The controls are not native, and two behaviours have to be handled in CSS. Scrolling inertia
and keyboard behaviour are the browser's, not the platform's. `overscroll-behavior` stops the
page bouncing, and the visual viewport is watched so a focused input is not hidden behind the
keyboard.

The safe areas need explicit padding. A notch, a rounded corner and a bottom gesture bar sit
over the webview. The layout pads itself with `env(safe-area-inset-top)`,
`env(safe-area-inset-bottom)` and their left and right siblings, and the viewport meta carries
`viewport-fit=cover` so those values are non zero.

iOS distribution is the expensive corner. A real device needs a Mac, Xcode and a paid developer
account, so the course demo runs in the simulator. Android has no such wall and the APK is
sideloaded.

The webview is a shared runtime, so a bug in the engine is a bug on both phone platforms. A
heavy screen costs the same on an old Android phone as it does in a browser, and nothing here
makes it cheaper.

The bridge is a new surface. Every plugin is native code per platform, so a native capability
is a small twice written fragment even when the UI is written once.

## Alternatives considered

**React Native or Expo.** Real native controls, a large ecosystem, over the air updates.
Rejected. The rendering layer is different, so the existing Vue components and styles would be
rewritten as React, and the new implementation would answer to a different set of rules than
the one already tested. It also replaces the browser target rather than sitting beside it.

**Flutter.** One codebase, one renderer, a consistent picture on both phones and in the browser.
Rejected. It needs Dart. Every screen and every shared planning rule moves to a second language,
and the web target becomes a canvas rather than a document, which costs the accessibility and
the text selection we get for free.

**Tauri 2 mobile.** Small binaries, a Rust core, a webview front end that would have kept our
components. Rejected. The mobile half is young, the plugin ecosystem is thin, and the course
needs two store ready packages rather than an experiment.

**Kotlin Multiplatform with Compose Multiplatform.** Shared logic and shared UI, a strong story
for Android. Rejected. It needs Kotlin and a Compose rewrite, and it does not give the browser
target that the desktop demo depends on.

**Two hand written native applications.** Best possible feel on each platform. Rejected. Three
implementations of one interface, three places for a plan bug to hide, and a schedule that does
not allow it.
