<h1 align="center">Following It Is All You Need.</h1>

<p align="center">
  <b>NextStop</b><br>
  替一个学生决定两节课之间该去哪，并且在一天被打乱时守住这个决定。
</p>

<p align="center">
  <a href="https://henryliu520.github.io/nextstop/"><b>▶ 试一下原型</b></a> &nbsp;·&nbsp;
  <a href="docs/why.zh-CN.md">为什么有这个项目</a> &nbsp;·&nbsp;
  <a href="docs/architecture.zh-CN.md">技术架构</a> &nbsp;·&nbsp;
  <a href="docs/algorithm.zh-CN.md">核心算法</a> &nbsp;·&nbsp;
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

<p align="center">
  <img src="product/web-desktop.png" width="620" alt="桌面宽度下，同一套界面装在一个手机框里">
</p>

<p align="center"><sub>一套界面。桌面宽度下它装在一个手机框里，因为它就是同一套界面。</sub></p>

学生上完一节课走出来，手上还有七十分钟。校园里到处是空房间。
他不知道的是**能去哪里**。查找器回答"哪间房空着"，日历存"你已经决定了什么"，两者都不回答学生真正的问题：**现在干什么**。

NextStop 回答这个。一张卡、一个时间、一个去向。一天动起来的时候，它改得尽量少。

---

## 它是什么

| | |
|---|---|
| **它回答** | 接下来这一小时做什么，以及在哪 |
| **它不做** | 给你一张清单让你自己挑 |
| **你要做的** | 一天打开一次，然后照着走 |
| **它不是** | 查找器，也不是日历 |

两节课之间，学生不知道三件事：**能去哪里、被允许去哪里、做什么**。房间只是其中一小步。

---

## 它怎么决定

```
1  固定    课表。权威、不可疑。
2  空出    承诺之间的空隙，扣掉走路和缓冲。
3  填      每段空隙放什么、放哪，由可用性层给候选。
4  修复    现实偏离时，尽量少改。
```

真正难的不是决定一天，是决定**打乱之后最少改多少**。两个停靠点算同一件事，必须目的、楼栋、开始时间都还对得上。修复按代价顺序往上爬：挪几分钟、换同楼房间、换楼、取消一站，**最后才动出勤**，而出勤是人自己的决定。

> 查找器回答一个集合。计划回答一个序列。

---

## 界面

<p align="center">
  <img src="product/day.png" width="240" alt="一天">
  <img src="product/repair.png" width="240" alt="拖堂 20 分钟后的修复">
  <img src="product/plan.png" width="240" alt="规划页">
</p>

<p align="center"><sub>左：一张卡，一天是一叠块。中：拖堂 20 分钟只改一块，不是五块。右：一周、一天、一句话。</sub></p>

三个 tab，三件事，没有工具栏。块自己带着它的动作：课给"不去"，吃饭给"换一个"，见面给"一键预约"。**没有空着的时间**，空隙默认是自习。

---

## 一张图说清架构

```
web/            Vue 3 + Vite，一套构建，四个容器
                网页 · PWA · Android 壳 · iOS 壳
     |
     |  REST + JSON（开发时由 Vite 代理 /api）
     v
backend/        Spring Boot 4.1.1、Java 21、PostgreSQL 17
                课表 · 房间 · 可用性 · 占用变更 · 审计 · 账号
     |
     v
mock-school-api/ 模拟学校课表源（真实接口尚未接入）
```

后端不再是产品。它回答"我能去哪"，存在的意义是保证计划不会指向一个不存在的地方。计划在客户端，所以走廊里没信号也能用。

---

## 文档

十二份文档，每份三语。从第一行开始读。

| | English | 简体 | 繁體 |
|---|---|---|---|
| **为什么有这个项目** | [`why.md`](docs/why.md) | [`zh-CN`](docs/why.zh-CN.md) | [`zh-TW`](docs/why.zh-TW.md) |
| **别人做不到的地方** | [`highlights.md`](docs/highlights.md) | [`zh-CN`](docs/highlights.zh-CN.md) | [`zh-TW`](docs/highlights.zh-TW.md) |
| **按用户旅程的功能** | [`features.md`](docs/features.md) | [`zh-CN`](docs/features.zh-CN.md) | [`zh-TW`](docs/features.zh-TW.md) |
| **技术架构** | [`architecture.md`](docs/architecture.md) | [`zh-CN`](docs/architecture.zh-CN.md) | [`zh-TW`](docs/architecture.zh-TW.md) |
| **核心算法** | [`algorithm.md`](docs/algorithm.md) | [`zh-CN`](docs/algorithm.zh-CN.md) | [`zh-TW`](docs/algorithm.zh-TW.md) |
| **界面与规则** | [`screens.md`](docs/screens.md) | [`zh-CN`](docs/screens.zh-CN.md) | [`zh-TW`](docs/screens.zh-TW.md) |
| **决策记录** | [`adr/`](docs/adr/README.md) | [`zh-CN`](docs/adr/README.zh-CN.md) | [`zh-TW`](docs/adr/README.zh-TW.md) |
| **全部索引** | [`docs/README.md`](docs/README.md) | [`zh-CN`](docs/README.zh-CN.md) | [`zh-TW`](docs/README.zh-TW.md) |

四条决策写成了 ADR：后端是数据层、计划在客户端、偏离时修复而非重算、一套构建服务四个容器。

---

## 跑起来

```bash
git clone https://github.com/HenryLiu520/nextstop
cd nextstop

./scripts/start-all.sh                  # PostgreSQL 5433 → Spring Boot 8080
cd web && npm install && npm run dev    # http://127.0.0.1:5175
```

`web/` 把 `/api` 代理到 8080，所以你看到的一天是用真实可用性数据拼出来的。背后的规划规则仍是桩，界面不掩饰这一点。

不想构建？打开 [`product/demo/index.html`](product/demo/index.html)，单文件，或者直接看线上版
<https://henryliu520.github.io/nextstop/>。

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

我们不声称更多。课表是样例数据。"空闲"不等于"没人"。"改一块 vs 改五块"是示例，不是实测结果。鉴权是原型级。

---

## 仓库结构

```
web/               界面：Vue 3 + Vite，一套代码兼顾手机与桌面
backend/           Spring Boot 服务：课表、可用性、账号、审计
frontend/          早期桌面客户端，保留因为可用性相关工作在这里
mobile/            早期手机客户端，原因同上
mock-school-api/   模拟学校课表源，含设计记录
docs/              十二份文档，从 docs/README.md 开始
product/           原型、截图、一页 PDF
scripts/           启动 / 停止 / 测试
```

---

<p align="center"><sub>
COMP1110 第 08 组 · 香港大学 · LIU Haoran (u3686264)<br>
课程作业。第三方组件在 <a href="THIRD-PARTY.md">THIRD-PARTY.md</a> 里逐项致谢。
</sub></p>
