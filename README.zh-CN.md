<h1 align="center">Following It Is All You Need.</h1>

<p align="center">
  <b>NextStop</b><br>
  替一个学生决定两节课之间该去哪，<br>
  并且在一天被打乱时守住这个决定。
</p>

<p align="center">
  <a href="https://henryliu520.github.io/nextstop/"><b>▶ 试一下原型</b></a> &nbsp;·&nbsp;
  <a href="docs/why.zh-CN.md">为什么有这个项目</a> &nbsp;·&nbsp;
  <a href="docs/algorithm.zh-CN.md">真正难的部分</a> &nbsp;·&nbsp;
  <a href="product/NextStop.pdf">一页 PDF</a>
</p>

<p align="center">
  <a href="README.md">English</a> · <a href="README.zh-CN.md">简体中文</a> · <a href="README.zh-TW.md">繁體中文</a>
</p>

<p align="center">
  <img alt="Vue 3" src="https://img.shields.io/badge/Vue-3-42b883?logo=vuedotjs&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white">
  <img alt="PostgreSQL 17" src="https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white">
  <img alt="27 个测试" src="https://img.shields.io/badge/tests-27%20passing-1C1A17">
  <img alt="原型" src="https://img.shields.io/badge/status-prototype-C8452D">
</p>

<p align="center">
  <img src="product/web-day.png" width="230" alt="一张卡，一叠块">
</p>

<p align="center"><sub>一张卡、一个时间、一个去向。剩下的一天在下面。</sub></p>

---

查找器告诉你哪些房间空着，日历存着你已经决定的事。两者都不回答一个学生 09:47 站在走廊里真正的问题：**现在干什么**。

NextStop 回答这个，而且在一天动起来的时候，改得尽量少。

---

## 这事发生在谁身上

- **高考完的第一年**，再没人告诉你该去哪接着卷。
- **交换生**，两节课之间两小时，找不到一个能正大光明打盹的地方。
- **站在电梯口那位**，已经"决定"了十一分钟。
- **下午三点四十才吃午饭那位**，因为中午那个决定太大了。
- **五个人都说"随便"**，然后二十分钟哪也没去。

他们都不缺信息。都缺下一步。

---

## 它给什么

<p align="center">
  <img src="product/day.png" width="240" alt="一天">
  <img src="product/repair.png" width="240" alt="拖堂 20 分钟后的修复">
  <img src="product/plan.png" width="240" alt="规划页">
</p>

<p align="center"><sub>左：一天是一叠块。中：拖堂 20 分钟只改一块，不是五块。右：一周、一天、一句话。</sub></p>

三个 tab，没有工具栏。块自己带着动作，而且没有空着的时间。

---

## 真正难的部分

不是决定一天，是决定**打乱之后最少改多少**。

```
RECOMPUTE EVERYTHING            REPAIR
────────────────────────        ────────────────────────
11:00–12:00  Study   →  11:00–11:40 Study      ← changed
12:10–12:40  Lunch   →  11:50–12:20 Lunch      ← changed
14:00–15:50  Class   →  14:00–15:50 Class        same
16:00–16:50  Meeting →  15:40–16:30 Meeting    ← changed
17:00–18:20  Study   →  16:40–18:00 Study      ← changed

5 blocks moved                  1 block moved
```

一个每次被打断就整体重写的计划，跟没有计划无法区分。两个停靠点算同一站，条件是目的、楼栋、开始时间都还对得上。
👉 **[algorithm.zh-CN.md](docs/algorithm.zh-CN.md)**

---

## 为什么后端不再是产品

```
web/            Vue 3 + Vite. One build, four containers:
                browser · PWA · Android shell · iOS shell
     |
     |  REST + JSON
     v
backend/        Spring Boot + PostgreSQL  (timetable, rooms, availability, audit)
     |
     v
mock-school-api/ a simulated school feed
```

后端回答的是**我能去哪**，那是学生不知道的三件事之一，也是最容易的那件。计划在客户端，所以走廊里没信号也能用，也所以换一台设备看不到你的计划。两个后果都如实写在这里。

---

## 文档，每份三语

| | English | 简体 | 繁體 |
|---|---|---|---|
| **为什么有这个项目** | [`why.md`](docs/why.md) | [`zh-CN`](docs/why.zh-CN.md) | [`zh-TW`](docs/why.zh-TW.md) |
| **别人做不到的地方** | [`highlights.md`](docs/highlights.md) | [`zh-CN`](docs/highlights.zh-CN.md) | [`zh-TW`](docs/highlights.zh-TW.md) |
| **按旅程列的功能** | [`features.md`](docs/features.md) | [`zh-CN`](docs/features.zh-CN.md) | [`zh-TW`](docs/features.zh-TW.md) |
| **技术架构** | [`architecture.md`](docs/architecture.md) | [`zh-CN`](docs/architecture.zh-CN.md) | [`zh-TW`](docs/architecture.zh-TW.md) |
| **真正难的部分** | [`algorithm.md`](docs/algorithm.md) | [`zh-CN`](docs/algorithm.zh-CN.md) | [`zh-TW`](docs/algorithm.zh-TW.md) |
| **界面与规则** | [`screens.md`](docs/screens.md) | [`zh-CN`](docs/screens.zh-CN.md) | [`zh-TW`](docs/screens.zh-TW.md) |
| **决策记录** | [`adr/`](docs/adr/README.md) | [`zh-CN`](docs/adr/README.zh-CN.md) | [`zh-TW`](docs/adr/README.zh-TW.md) |
| **全部索引** | [`docs/README.md`](docs/README.md) | [`zh-CN`](docs/README.zh-CN.md) | [`zh-TW`](docs/README.zh-TW.md) |

---

## 跑起来

```bash
git clone https://github.com/HenryLiu520/nextstop
cd nextstop

./scripts/start-all.sh                  # PostgreSQL 5433 → Spring Boot 8080
cd web && npm install && npm run dev    # http://127.0.0.1:5175
```

`web/` 把 `/api` 代理到 8080，所以你看到的一天是用真实可用性数据拼出来的。背后的规划器仍是桩，界面不掩饰。

不想构建？打开 [`product/demo/index.html`](product/demo/index.html)，单文件、不用服务、能离线，右上角切三种语言。

---

## 做了、设计了、没做

| 部分 | 状态 |
|---|---|
| 可用性引擎、规则、角色、审计、27 个测试 | **已实现并测试** |
| Web 界面、三个 tab、三语、真实可用性 | **已实现** |
| 单文件原型，可点完整流程 | **已实现** |
| 规划器与修复阶梯 | **已设计，未实现** |
| 学习层 | **已设计，未实现** |
| PWA manifest、Android 与 iOS 壳 | **已选定，未构建** |

课表是样例数据。"空闲"不等于"没人"。"改一块 vs 改五块"是示例，不是实测。鉴权是原型级。

---

<p align="center"><sub>
COMP1110 第 08 组 · 香港大学 · LIU Haoran (u3686264)<br>
名字是拿一篇讲 attention 的论文开的玩笑。那篇讲该注意什么，这个也讲该注意什么。
</sub></p>
