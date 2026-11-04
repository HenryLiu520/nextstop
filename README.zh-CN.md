<h1 align="center">Following It Is All You Need.</h1>

<p align="center">
  <b>NextStop</b><br>
  你有 73 分钟，和一张空白。<br>
  它写一张，并且在一天动起来的时候守住它。
</p>

<p align="center">
  <a href="https://henryliu520.github.io/nextstop/"><b>▶ 试一下原型</b></a> &nbsp;·&nbsp;
  <a href="docs/why.zh-CN.md">为什么有这个项目</a> &nbsp;·&nbsp;
  <a href="docs/highlights.zh-CN.md">它凭什么不一样</a> &nbsp;·&nbsp;
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
  <img alt="规划器仍是桩" src="https://img.shields.io/badge/planner-still%20a%20stub-C8452D">
</p>

<p align="center">
  <img src="product/web-day.png" width="230" alt="一张卡，一叠块">
</p>

<p align="center"><sub>一张卡、一个时间、一个去向。剩下的一天在下面。</sub></p>

---

周四 09:47。KB 的课刚下，下一节 11:00 在 CPD。

你会先花十一分钟不决定，然后走到 CPD，在教室外面再坐四十分钟 —— 因为坐在一个你确定被允许坐的地方，不需要做任何决定。

那一个小时不是被浪费掉的，它只是从来没被决定过。

NextStop 把它决定掉。

---

## 这事总在谁身上发生

- **国际生**，换了个国家读书，还是搞不清九十分钟该坐哪。
- **高考完的第一年**，再没人告诉你该去哪接着卷。
- **电梯口那位**，已经在"按哪一层"上决定了十一分钟。
- **下午三点四十才吃午饭那位**，因为中午那个决定太大了。
- **五个人都说"随便"**，然后二十分钟哪也没去。

这五个人手里那台手机，都知道哪些房间空着。

---

## 它做到的、那些工具做不到的四件事

| | | |
|---|---|---|
| **它替你决定，而不是给你清单** | 查找器回答一个集合，计划回答一个序列 | [为什么](docs/why.zh-CN.md) |
| **越用越聪明** | 学你实际走多快、你的一天实际几点结束、你实际会去哪。同一个参数满三次观测，才替换默认值 | [闪光点](docs/highlights.zh-CN.md) |
| **极度便捷** | 一天一个动作，然后照着走。需要的操作数只会越来越少，不会越来越多 | [界面](docs/screens.zh-CN.md) |
| **它从不先开口** | 没有推送、没有对话气泡、没有"需要帮忙吗"。真有事时，只说什么变了，并给一个撤回 | [闪光点](docs/highlights.zh-CN.md) |

学到的永远是默认值，不是被拿走的按钮。按钮一直留在原位。

---

## 它长什么样

<p align="center">
  <img src="product/day.png" width="240" alt="一天">
  <img src="product/repair.png" width="240" alt="拖堂 20 分钟之后">
  <img src="product/plan.png" width="240" alt="规划页">
</p>

<p align="center"><sub>左：一天是一叠块。中：拖堂 20 分钟。右：一周、一天、一句话。</sub></p>

课给"不去"，吃饭给"换一个"，见面给"一键预约"。没有空着的时间，空隙默认是自习。

---

## 一天动起来的时候

一节课拖堂二十分钟，两种答法：

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

一次改五块，是计划开始不被信任的方式。所以这里问的不是"最好的一天长什么样"，而是一个更窄的问题：哪几站真的坏了，然后只动那几站。两个停靠点算同一站，条件是目的、楼栋、开始时间都还对得上。

👉 判据、过程与代价顺序：**[algorithm.zh-CN.md](docs/algorithm.zh-CN.md)**

---

## 它是怎么搭起来的

```
web/     Vue 3 + Vite，一套构建，四个容器
         网页 · PWA · Android 壳 · iOS 壳
   |
   |  REST + JSON
   v
backend  Spring Boot + PostgreSQL
         课表 · 房间 · 可用性 · 审计 · 账号
```

规划、修复、界面都在客户端，所以走廊里没信号也能用。底下那个服务是项目早前做完的，它把一件事答得很准：**哪些房间现在空着**，于是计划不会指向一个不存在的地方。

---

## 文档，每份三语

| | English | 简体 | 繁體 |
|---|---|---|---|
| **为什么有这个项目** | [`why.md`](docs/why.md) | [`zh-CN`](docs/why.zh-CN.md) | [`zh-TW`](docs/why.zh-TW.md) |
| **它凭什么不一样** | [`highlights.md`](docs/highlights.md) | [`zh-CN`](docs/highlights.zh-CN.md) | [`zh-TW`](docs/highlights.zh-TW.md) |
| **按旅程列的功能** | [`features.md`](docs/features.md) | [`zh-CN`](docs/features.zh-CN.md) | [`zh-TW`](docs/features.zh-TW.md) |
| **技术架构** | [`architecture.md`](docs/architecture.md) | [`zh-CN`](docs/architecture.zh-CN.md) | [`zh-TW`](docs/architecture.zh-TW.md) |
| **修复判据** | [`algorithm.md`](docs/algorithm.md) | [`zh-CN`](docs/algorithm.zh-CN.md) | [`zh-TW`](docs/algorithm.zh-TW.md) |
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

`web/` 把 `/api` 代理到 8080，所以你看到的一天是用真实可用性数据拼出来的。

不想构建？打开 [`product/demo/index.html`](product/demo/index.html)，单文件、不用服务、能离线，右上角切三种语言。

---

## 哪些是真的，哪些不是

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
