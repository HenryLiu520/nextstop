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

---

## 周四 09:47

KB 的课提前下了。下一节 11:00 在 CPD。

```
09:47  KB      you are here, holding a laptop and seventy minutes
10:50          the next class starts, in a different building
        ?
```

你知道学校有空房间，你知道有图书馆，你知道自己欠着一篇报告。你不知道的是 **接下来这一小时，在这栋楼还是隔壁楼，该做哪一件事**。

于是你站在走廊里自己决定。一天三次到五次，每天都这样，你其实也习惯了。**而这就是没人做的那部分。**

---

## 查找器回答的，和你需要的

```
ROOM FINDER                     YOU
────────────────────────────    ────────────────────────────
"14:00 to 15:50, 12 rooms       "I have 70 minutes, my laptop
 free in this building."         is dying, and I have 2 hours
                                 of reading to do."
   ↑ an answer to a question       ↑ a decision about a day
     you did not ask
```

查找器返回一个**集合**。而一天是一条**序列**，你现在做的选择会改变后面能做什么。这就是为什么再多可用性数据也修不好这件事。

---

## 它给的是什么

一张卡。一叠块。块与块之间只写走路时间。**没有空着的时间。**

```
┌────────────────────────────────┐
│  ✓ 09:00–10:50  Class   COMP1110 · KB      │   past, folded flat
│      ┊ walk 5 min                          │
│  ▎ 11:00–12:00  Study   KB 227        ●    │   now, enlarged
│      ┊ walk 10 min                         │
│  ┆ 12:10–12:40  Lunch   CYM G/F      ┆     │   dotted: not booked yet
│      ┊ walk 10 min                         │
│  ▎ 14:00–15:50  Class   CPD · LG05         │
└────────────────────────────────┘
     End the day      Stay a bit longer
```

图例就是全部：**实心**是已经解出来的，**虚线**是外部事实（比如已经订到的房间），**点线**是还待订。已经过去的块压成一条细线，因为它不该继续抢注意力。

---

## 打乱时，它只改一块，不是五块

一节课拖堂二十分钟，两种反应：

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

一个每次被打断就整体重写的计划，跟没有计划是无法区分的。所以修复问的不是"最好的一天长什么样"，而是一个更窄的问题：**哪几站真的坏了**，然后只动那几站。两个停靠点算同一站，条件还是那三条：目的、楼栋、开始时间都还对得上。容忍度是参数，不是天条。

👉 判据、过程与代价顺序都在 **[algorithm.zh-CN.md](docs/algorithm.zh-CN.md)**。

---

## 四个动作，没有工具栏

| 你在哪 | 你能做什么 | 你不能做什么 |
|---|---|---|
| 一节课 | 不去 | 改时间、取消、重排 |
| 一段自习 | 换一个 | 拆成两段 |
| 吃饭 | 换一个 | 替你点单 |
| 一个见面 | 预约、换地方 | 订一间学校不允许你订的房 |

最后一行很重要。**系统不假装能分配它分配不了的房间。** 普通教室给一个可撤回的认领，真有预约流程的场地，它只说什么时候该订，然后把结果当成事实贴回计划。

---

## 为什么后端不再是产品

```
web/            Vue 3 + Vite. One build, four containers:
                browser · PWA · Android shell · iOS shell
     |
     |  REST + JSON
     v
backend/        Spring Boot + PostgreSQL
                timetable · rooms · availability · room updates · audit · accounts
     |
     v
mock-school-api/ a simulated school feed (the real endpoint is not connected)
```

后端回答的是**我能去哪**。那是学生不知道的三件事之一，而且是最容易的那件。计划在客户端，所以走廊里没信号也能用，也所以换一台设备看不到你的计划。两个后果都如实写在这里。

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

不想构建？打开 [`product/demo/index.html`](product/demo/index.html)，单文件、不用服务、能离线，右上角切 English / 简体 / 繁體。

---

## 它永远不会做的事

1. 问一个它自己看两眼就能知道答案的问题。
2. 给你一块空白，然后管它叫计划。
3. 一改就改三块，还不告诉你。
4. 假装那间房是你的。
5. 评价你去没去上课。

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

课表是样例数据。"空闲"不等于"没人"。"改一块 vs 改五块"是示例，不是实测结果。鉴权是原型级。这里没有一句撑不起的话。

---

## 仓库结构

```
web/               界面：Vue 3 + Vite，一套代码兼顾手机与桌面
backend/           Spring Boot 服务：课表、可用性、账号、审计
frontend/          早期桌面客户端，保留因为可用性相关工作在这里
mobile/            早期手机客户端，原因同上
mock-school-api/   模拟学校课表源，含设计记录
docs/              十二份文档，每份三语
product/           原型、截图、一页 PDF
scripts/           启动 / 停止 / 测试
```

---

<p align="center"><sub>
COMP1110 第 08 组 · 香港大学 · LIU Haoran (u3686264)<br>
名字是拿一篇讲 attention 的论文开的玩笑。那篇讲该注意什么，这个也讲该注意什么。
</sub></p>
