<h1 align="center">Following It Is All You Need.</h1>

<p align="center">
  <b>NextStop</b><br>
  <b>下课了，时间是你的。</b><br>
  它在你还未走到楼梯口之前，就把下一站定好了。
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
  <img alt="32 个测试" src="https://img.shields.io/badge/tests-32%20passing-1C1A17">
  <img alt="规划器仍是桩" src="https://img.shields.io/badge/planner-still%20a%20stub-C8452D">
</p>

<p align="center">
  <img src="product/day.png" width="340" alt="一张卡，一叠块">
</p>

<p align="center"><sub>一张卡，一个时间，一个去向。这一天剩下的部分在下面。</sub></p>

---

周四 09:47。KB 的课刚下，下一节 11:00 在 CPD，中间 73 分钟，走路吃掉十一分钟。

你会先犹豫一阵子，然后走去 CPD，在教室外面再坐四十分钟。因为你确定自己能坐的地方，只管收留你。

那一小时白白流走。它整段悬在 KB 和 CPD 之间，等一个决定。

NextStop 把它决定掉。

---

## 背景

- **国际生**，这学期刚落地，九十分钟还不知道能坐在哪。
- **刚考完高考的第一年**，再没有人告诉你去哪接着卷。
- **电梯口那位**，在"按几楼"这件事上已经想了十一分钟。
- **15:40 才吃上午饭那位**，中午那个决定太大了。
- **五个人都说"随便"**，然后二十分钟哪儿也没去。

这五个人手里的手机，都知道哪些教室空着。

---

## ⚡ 极度便捷

**你打开它，今天已经在那里了。一张卡片，一个时间，一个填好的答案。**

你本来要用的空教室查询，要你填**四个空**：哪栋楼，哪一天，从几点，到几点，而这四个空的答案它全都知道，因为它手里握着你的课表。在这里跟完一天，操作数是一条只收缩的线：**3 步 → 2 步 → 1 步**，目标是 0，而 0 够得着，因为阅读这一眼就把操作做完了。

排课只在揭开的那一刻算一次。计划发布之后保持只读，再打开一次，是把同一份计划从内存里照着画出来，第二次打开和第一次打开是同一张卡片。发布之后唯一会写计划的是修复。表单在这里失去了位置，原因就在这里：表单是答案已经存在之后才去问问题的地方，一天在揭开时就算完了，一整天里每个问题都已经在那一刻问完了。

> 代价是前一天晚上。算术必须在你打开它的那一秒里跑完，在手机上，所以一轮新的往返花掉了计划手里的全部时间。可用性只能按时间窗一次取回，一间一间去问会花掉远超一秒的时间。

*已实现：揭开一次，计划只读。3、2、1、0 是设计序列，没有做过测量。*

---

## 📈 自主学习

**它自己读出你走多快。**

它学**四个量**，每一个都从你本来就会做的动作里读出来，答案全在你原有的操作里。步行速度，看你按「我晚了」之前留了多大的空隙。一天什么时候结束，看你把「结束今天」按在哪个钟点。你实际在哪儿吃饭，看几个食堂都开着的时候你选了哪一个。你出勤的情况，看「不去」钮上的每一次按压。步行系数在设计上从 ×1.00 走到 ×1.28。

一个量要**满三次观测**才动，这道闸门就是全部规矩。第三次到来之前，默认值站着，屏幕在旁边写「还在了解你」。到第三次，参数取观测到的值，做过平滑，某个奇怪的周二只是数据点，规矩跟着三次的总和走。动的只有参数，算法保持原样，所以一个学到的值，读一个数字就能审计。这些值按设计摆在「我的」页上，可见，可改，旁边各挂一个计数器，学到第几次一眼可见，手动改一个，那个计数器清零重来。

> 代价是学习拧的就是事先摆好的那几个旋钮。它调这四个量的读数，规则表保持原样，代价阶梯保持原样，动作集保持原样，这四个量就是全部词汇。出勤率留在设备里，保存形式是一条条打卡记录。

*已设计，未实现。原型跑固定默认值，同时挂着「还在了解你」。*

---

## 🧠 高度智能

**从一张课表和一轮偏好开始，它把一整天算出来。**

每段空隙先扣掉步行时间和缓冲。剩下的每一段都有一个目的，一个地点。吃饭拿到一个最晚出发时刻，时长这一栏由它顶上。再剩下的都归自习，每一段空隙都派了用场。这些你拿着课表走一遍就能对出来，而通用助手的可靠停在口头，只留下一句保证；这个框把核对的机会递到你手里。

一句话只能按已经存在的按钮，所以这个框只有四个落点。句子进来先出预览：受影响的块，新的结束时间，还有最要紧的一行，其余各块照旧。应用时写下的撤销记录，和按钮按下去写的是同一条。超出四个落点的句子会把失败说出来，计划保持原样，静默的空操作只会喂大你对这个框的盲目信任。每个能力上岗前都要过**删除测试**：把输入框删掉，剩下的必须还是一个完整的 app。日子破了也一样，走修复路线，重算要改五块，修复改**一块**，一个停靠点只要目的、建筑、开始时间都还成立，就原样保留。

> 代价是措辞有天花板。需要一种新操作的请求会出声地失败，即兴发挥被这个框关在门外，所以这个框比一个聊天窗口拒绝得更多。

*已实现：四个动作和它们的撤销记录。预览只对一个写好的例句存在，背后没有解析器。*

---

## 界面

<p align="center">
  <img src="product/triptych.png" width="620" alt="一天、拖堂 20 分钟后修好、规划页">
</p>

<p align="center"><sub>左：一天一叠块 · 中：拖堂 20 分钟，修好，只动一块 · 右：一周，一天，一句话。</sub></p>

课给"不去"，饭给"换一个"，会给"预约"。没有空的空档，空档默认自习。

---

## 规划流程

五步，顺序固定。

1. **读**。课表从学校的只读接口来，系统照单全收，把它当作正确的数据。
2. **同步**。日历订阅并进来，数据单向流入系统。
3. **加**。你自己塞的一次性事情，看牙、取快递。和课一样的分量，区别是你能删。
4. **并**。三个来源并成一份「已占用」。重叠的合并，相邻间隔小于十五分钟的也合并，因为十分钟刚好够走一条走廊。
5. **扣**。每个空档扣掉走路和缓冲，走路乘你自己的系数，这个系数从 1.00 起步，学会之后由你来定。

剩下的是一串窗口。每个窗口给一个目的和一个地点，吃饭的时长来自「最晚几点必须走」这条截止线，剩下的全填自习，每个窗口都装满内容。最后一件事是房间：先过硬条件（整段都空、座位够、你能进、你刚离开的那栋直接出局），再用代价在活下来的几间里挑。

```
F = wS × S'  +  wD × D

S'  房间本身，七个因子，扣过拒绝折扣
D   走过去要多久，能坐多久
wD  0.40 到 0.65，窗口越长越看 S'
```

任何一间房撞上一条硬条件，它就出局。权重只负责在已经合格的房间里挑一间。

房间怎么选有一套自己的打分，七个固态因子加一个拒绝折扣，见 **[room-scoring.zh-CN.md](docs/room-scoring.zh-CN.md)**。在这之前的整条链路，从三个来源到并集，到缓冲的算术，到一个周四剩下的三个窗口，一直到发出去的那一版序列，一步一步走一遍，见 **[day.zh-CN.md](docs/day.zh-CN.md)**。

---

## 偏离与修复

一节课拖堂二十分钟。两种答法：

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

五块齐动，是计划失去信任的方式。修复问的比重算窄。它问哪几站真的断了，只动那几站，两站算同一站，靠的是目的、楼栋、开始时间都还对得上。

判据、过程与代价顺序：**[algorithm.zh-CN.md](docs/algorithm.zh-CN.md)**

---

## 架构

```
app      一个 HTML 文件，无需构建
(demo/)  product/demo/index.html · 浏览器 · PWA
         NextStop 是手机 app：一天 · 规划 · 查找 · 我的，四个视图，一个文件
   |
   |  REST + JSON
   v
backend  Spring Boot + PostgreSQL
         课表 · 房间 · 可用性 · 审计 · 账号
```

计划、修复、界面全在客户端跑，走廊里信号断了照样能用。底下的可用性服务是项目早期搭好的，它只把一个问题答准：哪些房间空着，计划给出的每个去处都对应一间此刻空着的真实房间。

---

## 包含内容

- 可点原型收在单文件里，自带三种语言，离线也能跑。
- 可用性服务用 Spring Boot 与 PostgreSQL 搭成，项目早前完成，它回答哪些房间空着，32 个测试在它身后把关。
- 规划器今天仍是桩，它交出一份有形状的一天，真正的求解排在路线图上。

## 文档

| | English | 简体 | 繁體 |
|---|---|---|---|
| **为什么有这个项目** | [`why.md`](docs/why.md) | [`zh-CN`](docs/why.zh-CN.md) | [`zh-TW`](docs/why.zh-TW.md) |
| **它凭什么不一样** | [`highlights.md`](docs/highlights.md) | [`zh-CN`](docs/highlights.zh-CN.md) | [`zh-TW`](docs/highlights.zh-TW.md) |
| **按旅程列的功能** | [`features.md`](docs/features.md) | [`zh-CN`](docs/features.zh-CN.md) | [`zh-TW`](docs/features.zh-TW.md) |
| **技术架构** | [`architecture.md`](docs/architecture.md) | [`zh-CN`](docs/architecture.zh-CN.md) | [`zh-TW`](docs/architecture.zh-TW.md) |
| **修复判据** | [`algorithm.md`](docs/algorithm.md) | [`zh-CN`](docs/algorithm.zh-CN.md) | [`zh-TW`](docs/algorithm.zh-TW.md) |
| **房间评分** | [`room-scoring.md`](docs/room-scoring.md) | [`zh-CN`](docs/room-scoring.zh-CN.md) | [`zh-TW`](docs/room-scoring.zh-TW.md) |
| **界面与规则** | [`screens.md`](docs/screens.md) | [`zh-CN`](docs/screens.zh-CN.md) | [`zh-TW`](docs/screens.zh-TW.md) |
| **决策记录** | [`adr/`](docs/adr/README.md) | [`zh-CN`](docs/adr/README.zh-CN.md) | [`zh-TW`](docs/adr/README.zh-TW.md) |
| **全部索引** | [`docs/README.md`](docs/README.md) | [`zh-CN`](docs/README.zh-CN.md) | [`zh-TW`](docs/README.zh-TW.md) |

---

## 本地运行

```bash
git clone https://github.com/HenryLiu520/nextstop
cd nextstop

./scripts/start-all.sh                  # PostgreSQL 5433 → Spring Boot 8080
cd web && npm install && npm run dev    # http://127.0.0.1:5175
```

`web/` 把 `/api` 代理到 8080，屏幕上这一天是用真实可用性答案拼出来的。

不想构建？打开 [`product/demo/index.html`](product/demo/index.html)，单文件，不用服务，能离线，右上角切三种语言。

---

## 路线图

- 让规划器真正填满一个空档。
- 把一句话接到那四个动作上。
- 发布 PWA 与两个手机壳。

---

<p align="center"><sub>
COMP1110 第 08 组 · 香港大学 · LIU Haoran (u3686264)<br>
名字是拿一篇讲 attention 的论文开的玩笑。
</sub></p>
