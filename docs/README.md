# Documentation

One map of the project. Everything below is English except where a file name says otherwise.

The short version, if you only read one thing:
**[`why.md`](why.md)** explains the problem, and **[`features.md`](features.md)** shows what the
interface does about it.

---

## Start here

| Document | Answers |
|---|---|
| [`why.md`](why.md) | Why this exists. The moment, what a student does not know, why room finders and calendars do not cover it, and the gap this project addresses. |
| [`highlights.md`](highlights.md) | The four things this does that the obvious alternatives do not, and how each one is achieved. |
| [`features.md`](features.md) | Every feature by user journey. What it does, what you see, what it depends on. Each row is tagged Built, Data layer or Specified. |

## How it works

| Document | Answers |
|---|---|
| [`architecture.md`](architecture.md) | Where the split between the data layer and the plan layer sits, how the two connect, and the delivery targets. |
| [`algorithm.md`](algorithm.md) | The four layers, the sameness criterion, the repair procedure, the cost order, the complexity and one worked example. |
| [`screens.md`](screens.md) | The three tabs, the block specification, the action matrix, the design tokens and the three interface rules. |

## Decisions

Architecture decisions, in the order they were made. Each one records what was chosen, what it
costs, and what was rejected.

| Record | Subject |
|---|---|
| [`adr/README.md`](adr/README.md) | Index of the four records |
| [`adr/0001`](adr/0001-backend-is-the-data-layer.md) | The backend serves the schedule. It does not hold a plan. |
| [`adr/0002`](adr/0002-plan-lives-in-the-client.md) | The plan is client state. |
| [`adr/0003`](adr/0003-repair-instead-of-recompute.md) | A deviation repairs the plan instead of recomputing it. |
| [`adr/0004`](adr/0004-one-ui-four-targets.md) | One build, four delivery targets: browser, PWA, Android shell, iOS shell. |

## Run it

| Document | Answers |
|---|---|
| [`DEPLOY.md`](DEPLOY.md) | Step by step: database, service, clients, ports, accounts, troubleshooting. Also in [简体中文](DEPLOY.zh-CN.md) and [繁體中文](DEPLOY.zh-TW.md). |
| [`../product/demo/index.html`](../product/demo/index.html) | The prototype. One file, no build. Open it in a browser. |

## Data and reference

| Document | Answers |
|---|---|
| [`ERD.md`](ERD.md) | The schema, drawn from the live database, and the three things it deliberately does not have. Also in [简体中文](ERD.zh-CN.md) and [繁體中文](ERD.zh-TW.md). |
| [`screenshots.md`](screenshots.md) | Screenshots of the running data layer and the clients it serves. |

## Assets

Figures referenced above live in `../product/` (prototype screenshots and the one page PDF) and in
[`diagrams/`](diagrams/) (the schema and architecture diagrams, generated from scripts).

## A note on the two older clients

`../frontend/` and `../mobile/` are the desktop and phone clients built before the interface moved
to [`../web/`](../web/). They still run against the same data layer, and they are kept because the
availability work and its tests live with them. New interface work happens in `web/`.
