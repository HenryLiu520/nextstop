# 架构

[English](architecture.md) · **简体中文** · [繁體中文](architecture.zh-TW.md)

NextStop 只回答一个问题：*这个人此刻应该在哪里，什么时候该动身。*
回答这个问题的系统分成两半，两半的位置从不互换。

服务器是**数据层**。它掌握课表和教室，回答"哪里是空的"。
客户端是**计划层**。它们决定空闲时间拿来做什么，把那个决定留住，
日子一变就把它修回来。服务器不存任何计划，对计划也没有任何意见。

## 1. 概览

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

## 2. 分界线为什么在这里

分界只跟一条判据走：**这个事实是共享的，还是个人的？** 一份计划属于一个人，
只活几个小时。课表由上千人共享，活一个学期，由一个我们控制不了的办公室写入。
不同的问题，不同的家。

| 属性 | 计划 | 课表 |
|---|---|---|
| 归属 | 一个学生 | 学校 |
| 生命周期 | 一天 | 一个学期 |
| 变化频率 | 每次会话多次编辑 | 发布后固定 |
| 真值 | 一个提案，永远可商量 | 终局，从不受质疑 |
| 读写 | 本地读写 | 我们这边只读 |
| 需要离线 | 需要，走廊里没有信号 | 不需要，缓存足够 |

**计算放在客户端**，因为计划短命、私人、易变。否则每一次编辑都是一次往返加一行服务器记录，
每改一条计划规则就是一次迁移。修补在 CPU 上便宜，在协调上昂贵：它拿候选停靠点
与一份只有客户端持有的计划对比。

**数据留在服务器**，因为两个人看同一间教室时，课表不能给出两个答案。否则两个不同版本的客户端
会对"这间教室空不空"产生分歧，而且没有什么能裁决这场争论。只有服务器这一处，
"2A 在 10:00 到 11:00 被占用"才只有一个意思。

服务器比它看起来要小：它回答"我能去这里吗"，然后让开。

## 3. 数据层

Spring Boot 4.1.1 跑在 Java 21 上，连端口 5433 的 PostgreSQL 17。Flyway 管理 schema，
V1 到 V20。服务由 32 个自动化测试覆盖，这些测试跑在真实数据库上。

| 方法 | 路径 | 谁 | 用途 |
|---|---|---|---|
| POST | `/api/auth/login` | 任何人 | 用凭据换一个 token |
| POST | `/api/auth/register` | 任何人 | 创建一个 `user` 账号 |
| GET | `/api/auth/me` | token | 当前身份和角色 |
| GET | `/api/availability?date=&building=&from=&to=` | token | 某个窗口内空闲的教室 |
| GET | `/api/rooms`, `/api/buildings` | token | 带属性的教室，楼栋 |
| GET | `/api/rooms/{code}/timeline` | token | 一间教室一天的占用区块 |
| PATCH | `/api/rooms/{code}` | admin 及以上 | 教室元数据 |
| GET, POST | `/api/rooms/{code}/reviews` | token | 读或写一条评价 |
| GET | `/api/reviews/summary` | token | 汇总评分 |
| POST | `/api/updates` | admin 及以上 | 占用变更，带原因和到期 |
| GET | `/api/updates`, `/api/updates/mine` | token | 生效中的变更，自己的变更 |
| DELETE | `/api/updates/{id}` | admin 及以上 | 撤回一条变更 |
| GET | `/api/audit` | admin 及以上 | 特权操作的审计日志 |
| GET, POST, PATCH, DELETE | `/api/accounts` | admin 和 superadmin | 账号管理 |
| GET | `/api/about` | 任何人 | 构建和服务元数据 |

角色顺序是 `user < admin < superadmin`。这个顺序在服务器上强制一次，
客户端只是把 token 用不了的东西藏起来。

### 唯一真正的计算

`GET /api/availability` 不是查表。它是：

```
free(room, date) = blocks(room, date) - blocks taken by a class
                 - blocks taken by an active room update
                 - 10 minutes either side of every occupied block
```

**10 分钟换场余量。** 一节 11:00 结束的课，就可用性而言，把教室占到 11:10。
余量在每个被占用区块的两边都生效，所以 10:55 开始的查询会返回空。这个值是服务器配置，
默认 10，从来不是客户端设置：两个客户端必须对同一间教室给出同一个答案。

**课表永远赢。** 一条房间更新可以让一间教室停用，带原因和到期时间，由管理员发布。它不能把
教室从一节课里拿出来。与课程区块重叠的变更会被拒绝，返回 HTTP 400 和代码
`TIMETABLE_PRIORITY`：课表是基准事实，管理员备注只是一个批注。

更新带原因和到期时间，所以读起来像关于世界的一个主张，而不是一次永久修改。到期之后那个区块
就不再计数，因此后面不需要清理任何东西。

### 房间属性

房间质量来自服务端的一份数据集，两个客户端因此把同一间房排到同一个位置。固态分在设备上用这些数值和 [`room-scoring.md`](room-scoring.md) 里的权重算出。

```
GET /v1/rooms/attributes
{ "rev": 14,
  "updatedAt": "2026-11-11T09:00:00Z",
  "rooms": [ { "code": "KB227", "quiet": 4, "desk": 3, "sockets": 3,
               "ac": 5, "spaciousness": 4, "daylight": 3 } ] }
```

每间房六个维度，每条是 0 到 5 的整数：`quiet`、`desk`、`sockets`、`ac`、`spaciousness`、`daylight`。0、3、5 三档的锚点写在 [`adr/0005-room-attributes-are-scored-on-the-server.zh-CN.md`](adr/0005-room-attributes-are-scored-on-the-server.zh-CN.md)。

**缓存语义。** `rev` 就是缓存键。客户端把文档连同到达时的修订号存进 IndexedDB，启动时比对本地修订号，服务端报的修订号不同才重拉。修订号一致时只有一次条件请求，没有下载。

**刷新节奏。** 每学期做一次全量审计。装修、换桌子、一层楼加插座条，在记录变更的当天递增 `rev`。`updatedAt` 带日期，客户端据此判断本地那份有多旧。

**降级。** 拉不到就用 IndexedDB 里那份。设备上也没有，每个维度按 0.5 中性分算，受影响的卡片标「未核实」。没有属性记录的房间同样按 0.5 算。

## 4. 计划层

计划层是客户端代码。在原型里它是一个单独的 HTML 文件，它背后的规划器还是个桩。真实的是它
管理的状态。

一份计划是一天的停靠点序列，每个停靠点有开始时间、时长、地点和目的。

| 状态 | 什么时候进入 | 用户看到什么 | 去向 |
|---|---|---|---|
| `planning` | 一天被摊开，或者做了一次编辑 | 这一天是一叠区块 | `executing` |
| `executing` | 用户开始跟着计划走 | 当前停靠点，一次一张卡 | `deviated`, `planning` |
| `deviated` | 现实偏离：一节课拖堂，一个停靠点被跳过 | 一条提示和一个修法 | `repairing`, `planning` |
| `repairing` | 用户接受那个修法 | 改动停靠点最少的计划 | `executing`, `planning` |

停靠点是有类型的，类型决定修补有多大自由度：

| 停靠点类型 | 可移动 | 可删除 | 说明 |
|---|---|---|---|
| `class` | 不可 | 只有用户能删 | 一个锚点，从课表复制而来 |
| `study` | 可以，在它的空档内 | 可以 | 默认的填充物 |
| `meal` | 可以，在一个窗口内 | 不可 | 绑在一个用餐时段上 |
| `meeting` | 可以 | 不可 | 里面有别人 |
| `custom` | 可以 | 可以 | 手动加的，什么都能放 |

系统从不碰 `class` 停靠点。跳过一个是用自己的决定，而且可逆。

**为什么服务器上不存计划。** 一张 plans 表需要用户、版本、同步规则和冲突处理，它还是会输给
一台没保存就关掉的手机。计划只有一天长，重建它的成本是一次往返，不需要协调。
没有端点接受计划，没有列持有计划，停靠点改形状时也没有迁移要写。

应该坐在 `repairing` 状态背后的最小扰动修补算法，写在
[`algorithm.zh-CN.md`](algorithm.zh-CN.md)。它已经设计好，尚未实现。

## 5. 它们怎么连

契约是 HTTP 上的 REST，JSON body，一个基础路径 `/api`。

- **认证。** `POST /api/auth/login` 返回一个 token 和一个角色。两个客户端每次调用都在
  `Authorization` 头里带上它，前缀是 `Bearer` scheme。token 存在内存里，所以刷新页面就会
  把用户登出。
- **查询形状。** 可用性总是以一个窗口来问，从来不是整表倾倒。客户端发一个日期、一栋楼和它
  打算使用的时段，所以答案是一个很短的教室列表。
- **元数据，不是课表。** 客户端问*"这间教室现在空吗"*，不是*"把所有课都给我"*。填一个空档
  一个请求，修补一次计划再多一个。没有任何东西订阅任何东西。
- **新鲜度。** 客户端给每个可用性答案打上到达时间。围绕某个停靠点修补之前，它检查这个答案
  是否比最后一次计划编辑更新，不是就重新拉取。建立在过期可用性上的计划比没有计划更糟。
- **失败与降级。** 数据层不可达时，计划继续渲染，因为它活在客户端里。停下来的是需要外部
  事实的东西：查询失败，计划被标记为未验证，修补拒绝把一个停靠点移进未确认的教室。
  客户端从不为了界面继续往下走而凭空造一间教室。

| 情况 | 客户端行为 |
|---|---|
| 401 或 token 过期 | 登出，回到登录页，什么都不留 |
| 403，角色太低 | 把那个操作藏起来，不重试 |
| 400 `TIMETABLE_PRIORITY` | 显示冲突的那节课，计划保持原样 |
| 可用性遇到网络错误 | 把受影响的停靠点标记为未验证 |
| 可用性列表为空 | 推荐邻楼，绝不推荐一间未知的教室 |

除了 token，服务器上不存任何会话状态，所以客户端再问一次就能接着用。

## 6. 两个客户端，一份契约

两个 Vue 3 应用：端口 5173 的桌面客户端覆盖全部三个角色，端口 5174 的手机客户端覆盖学生视图。
它们是分开的构建，因为两套布局几乎不相干。底下它们是同一个客户端的两种形状。

| | 桌面，5173 | 手机，5174 |
|---|---|---|
| 角色 | user, admin, superadmin | user |
| 屏幕 | day, plan, rooms, reviews, admin, audit | day, plan, mine |
| API | `/api` | `/api`，同一个 |
| 认证 | 同样的 token 格式 | 同样的 token 格式 |
| 规则 | 在服务器上强制 | 在服务器上强制 |

**没有一条规则写两遍。** 10 分钟余量、课表优先、角色顺序和审计写入都活在 Spring 服务里。
两个客户端都不重实现它们，也都不被信任去实现。一条写两遍的规则最终会分叉，而用户看到的会是
错的那一版。改余量只是改一个配置值，两个客户端下一个请求就拿到。

它们真正重复的是呈现：同一个答案，一个做成手机卡片，一个做成桌面行。

## 7. 部署形状

三个进程和一个数据库，开发时全在一台机器上：

| 进程 | 端口 | 是什么 |
|---|---|---|
| PostgreSQL 17 | 5433 | 数据库，启动时应用迁移 |
| Spring Boot 4.1.1 | 8080 | 数据层 |
| Vue 3 桌面 | 5173 | 一个 Vite dev server |
| Vue 3 手机 | 5174 | 一个 Vite dev server |

`scripts/start-all.sh` 按那个顺序把它们拉起来：数据库、服务，然后两个客户端。
`scripts/test.sh` 跑那 32 个自动化测试。第一次运行会应用 V1 到 V20，花几分钟。

Docker 是可选的。`docker-compose.yml` 把同样的三个进程加数据库打包起来，给一台不愿意装
Java 和 Node 的机器用。布局完全一样：计划层以静态文件提供，所以它不在乎数据跑在哪里。

## 交付目标

一次构建，四个容器。`web/` 是现在正在构建的前端。它有一个 `vite.config.js`。
`vite build` 写出 `dist/`，每个目标都加载同一个目录。

今天存在的东西比计划窄。`product/demo/index.html` 是一个单文件原型，打开文件就能跑。
`web/` 还没产出 `dist/`。仓库里没有 manifest，没有 service worker，也没有 Capacitor 项目。
下表就是这么说的。

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

| 目标 | 容器 | 增加了什么 | 状态 |
|---|---|---|---|
| browser | 标签页 | 什么都不加 | 原型今天就能跑 |
| PWA | 标签页，已安装 | manifest + service worker + 离线外壳 | 未构建 |
| Android | Capacitor 外壳 | APK | 未构建 |
| iOS | Capacitor 外壳 | IPA | 未构建 |

### 构建链

- 一份 Vite 配置，在 `web/` 里。`vite build` 写出 `dist/`。没有哪个目标拿第二份配置。
- PWA 是一个插件，不是第二次构建。`vite-plugin-pwa` 在 `vite build` 期间生成 manifest 和
  预缓存列表。manifest 和 service worker 都是输出物，从不手写，所以进构建的文件就是缓存里的
  文件，没人需要去改一张列表。
- Capacitor 拿 `dist/` 当输入。`capacitor.config.json` 把 `webDir` 设成 `dist`，
  `npx cap sync` 把那个目录复制进 `android/` 和 `ios/` 并刷新原生插件。这次复制就是把浏览器
  构建变成原生应用的那一步。
- 之后签名包是一条平台命令，不是第二个前端。`npx cap open android` 加一个 Gradle 构建出
  APK，Xcode archive 出 IPA。
- 后果：`dist/` 是唯一的产物。一个修复只要在那一份目录里，就落到四个容器里，而目标之间的
  差别是一条 CSS 规则或一次能力检测。

### 一个版本，四个地方

版本必须在四个不同时间构建的地方保持一致。它从一个地方读取。

- `web/package.json` 持有 `version`，它是仓库里唯一的版本字符串。
- Vite 用 `define` 在构建时把它注入 bundle，所以跑着的客户端知道自己是哪一版。它不用请求
  就能到版本页和诊断信息里。
- service worker 的缓存名由同一个值构成。新版本就是新缓存，旧的会在 activate 时丢掉。
  这就是不让过期外壳活过一次部署的原因。
- Capacitor 在 `cap sync` 时读同一个值，用于 Android 的 `versionName` 和 `versionCode`，
  以及 iOS 的 `CFBundleShortVersionString`。没人往原生项目里手打版本号。
- `GET /api/about` 本来也要报告它，这样客户端能比较自己跑的构建与服务器编译时对应的构建。
  还没接上：`AboutController` 返回 project、course、group、author、uid、signature 和 year，
  没有 `version` 字段。
- 后果：升一次版本是一次编辑，比服务器旧的客户端是可检测的，而不是靠猜。

### 安全区

刘海、圆角和底部手势条压在 webview 上面。布局得自己留出内边距，否则内容的第一行和最后一行
读不了。

- `web/index.html` 的 viewport meta 里已经带了 `viewport-fit=cover`。这是前提：没有它，
  每个 `env(safe-area-inset-*)` 都解析成零，内边距静默地什么都不做。
- 内边距只加在布局碰到屏幕边缘的地方，也只在那些地方加。顶栏拿
  `env(safe-area-inset-top)`。底部导航和每个 sheet 拿 `env(safe-area-inset-bottom)`。
  `web/src/components/BottomSheet.vue` 今天已经有底部那个。
- 左右内边距在横屏时有用。它们是同一批容器上的 padding，不是另一套布局。
- 这些值在使用处由 CSS 读取，所以新的边缘元素自己留内边距，而不是继承一个固定数字。
  写死的 `padding-bottom: 34px` 到下一台设备上就是错的。
- 同一条规则覆盖外壳和浏览器。在 Capacitor 外壳里 webview 默认全屏，所以安全区是状态栏和
  第一行文字之间唯一的东西。

### 两个浏览器行为

两条都是浏览器事实，所以在标签页里和在外壳里都成立，它们跑同一个引擎。

- **回弹。** `overscroll-behavior: contain` 阻止页面越过末端弹动，也阻止下拉刷新在一天当中
  重载计划。已经在 `web/src/styles/base.css` 和 `web/src/App.vue` 里设好，且设在滚动容器上
  而不是 `body` 上。
- **键盘。** 在 iOS 上，屏幕键盘不改变布局视口的大小。布局相信自己还有整个高度，被聚焦的
  输入框就落在键盘后面。修法是 `window.visualViewport`：监听 `resize` 和 `scroll`，读
  `height` 和 `offsetTop`，把聚焦的字段滚进那个矩形。还没写。

### 离线覆盖什么，不覆盖什么

离线是从计划活在客户端这件事继承来的（[§4](#4-the-plan-layer)），不是来自一个单独的离线功能。
service worker 加的是一个外壳，不是一个数据存储。

- 缓存：外壳。没有网络也能打开应用并渲染。
- 缓存：每个查询最后一次的可用性答案，连同拉取时间一起留着。
- 不缓存，因为不可能知道：缓存答案之后发布的房间更新、办公室调走的课、别人写的评价。
  走廊里没有信号，所以那里的答案就是最后一次拉取的那个。
- 客户端会说清是哪种。缓存的答案带着它的拉取时间提供，依赖它的停靠点被标记为未验证，
  依照 [§5](#5-how-they-connect)。缓存的答案从不打扮成当前的答案。
- 新计划不在离线时构建。修补只能把一个停靠点移进新答案确认过的教室。计划两种情况都继续渲染，
  因为它是本地的。
- 写入失败。没有网络时发一条评价会返回错误，不入队。没有后台同步，也没有 outbox，因为计划是
  唯一必须活下来的状态，而它从不离开设备。

### 为什么用 Capacitor 而不是手写 WebView

界面很普通：卡片、列表、时间线、一个表单。里面没有需要原生绘制的东西。
所以原生层应该尽量小。

- Capacitor 恰好提供容器、应用图标、包名、签名路径和一条通往原生调用的窄桥。它不提供任何
  界面，也不指望有。
- 它不重写界面。四个容器跑同样的组件、同样的 CSS 和同一份计划规则。没有哪个目标有自己的屏幕。
- 另一条路，在 Android 上手写一个 `WebView` activity，在 iOS 上手写一个 `WKWebView`
  controller，产出的应用一样。它还会产出文件复制、资源加载器、桥和版本管道，写两遍，作为
  我们要维护的代码。
- 桥故意保持窄：状态栏颜色、返回键处理。每个插件都是各平台的原生代码，所以一个新的原生能力
  就是一小段写两遍的代码，即使界面只写一遍。

这个决定和它的代价记在
[`adr/0004-one-ui-four-targets.zh-CN.md`](adr/0004-one-ui-four-targets.zh-CN.md)。

## 8. 还没构建的东西

说明白，免得上面任何一句被读成断言：

- **服务端规划。** 完全没有：没有端点接受或返回计划。
- **多用户同步。** 设备之间不共享计划，所以在桌面做的计划不在手机上。
- **修补算法。** 原型规划器是个桩。[`algorithm.zh-CN.md`](algorithm.zh-CN.md) 里的最小扰动
  算法已经写成规格，没有实现，所以原型里"动一块还是动五块"的行为是示意，不是结果。
- **真实的学校源。** `mock-school-api` 模拟课表，`V2__seed.sql` 播种一份像样的课表。
  服务每三十分钟从这个接口拉一次，落进 `school_slots`，再由它重建 `class_slots`。学校的端点没有接。
- **生产级认证。** token 在内存里，加盐 SHA-256 密码，没有 HTTPS。原型级别。

这里描述的分裂是真实且跑着的。坐在计划层上面的东西，以及真实的课表源会是什么样子，
是接下来的几块。
