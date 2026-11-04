# 文档

[English](README.md) · **简体中文** · [繁體中文](README.zh-TW.md)

整个项目的一张地图。文件名自己表明了语言，下面列出的每一份都有对应的中文版。

如果你只读一样东西，那就读短的那个版本：
**[`why.md`](why.zh-CN.md)** 讲问题，**[`features.md`](features.zh-CN.md)** 讲界面针对它做了什么。

---

## 从这里开始

| 文档 | 回答什么 |
|---|---|
| [`why.md`](why.zh-CN.md) | 这东西为什么存在。那个时刻，一个学生不知道的东西，为什么找空教室的工具和日历都盖不住它，以及这个项目针对的那个缺口。 |
| [`highlights.md`](highlights.zh-CN.md) | 它做了四件显而易见的那几个替代品做不到的事，以及每一件是怎么做到的。 |
| [`features.md`](features.zh-CN.md) | 按用户旅程组织的每个功能。它做什么，你看到什么，它依据什么。每一行都标了 Built、Data layer 或 Specified。 |

## 它是怎么工作的

| 文档 | 回答什么 |
|---|---|
| [`architecture.md`](architecture.zh-CN.md) | 数据层和计划层之间的那条分界落在哪里，两层怎么接上，以及交付目标。 |
| [`algorithm.md`](algorithm.zh-CN.md) | 四个层，相同的判据，修复的流程，成本的次序，复杂度，和一个走通了的例子。 |
| [`screens.md`](screens.zh-CN.md) | 三个标签页，块的规格，动作矩阵，设计 token，和三条界面规则。 |

## 决定

架构上的决定，按做出来的顺序。每一份都记下选了什么，代价是什么，否掉了什么。

| 记录 | 主题 |
|---|---|
| [`adr/README.md`](adr/README.zh-CN.md) | 四份记录的索引 |
| [`adr/0001`](adr/0001-backend-is-the-data-layer.zh-CN.md) | 后端提供课表，它不持有计划。 |
| [`adr/0002`](adr/0002-plan-lives-in-the-client.zh-CN.md) | 计划是客户端状态。 |
| [`adr/0003`](adr/0003-repair-instead-of-recompute.zh-CN.md) | 一个偏离去修计划，而不是重算计划。 |
| [`adr/0004`](adr/0004-one-ui-four-targets.zh-CN.md) | 一次构建，四个交付目标：浏览器、PWA、Android 壳、iOS 壳。 |

## 跑起来

| 文档 | 回答什么 |
|---|---|
| [`DEPLOY.md`](DEPLOY.zh-CN.md) | 一步一步：数据库、服务、客户端、端口、账号、排查。也见 [English](DEPLOY.md) 和 [繁體中文](DEPLOY.zh-TW.md)。 |
| [`../product/demo/index.html`](../product/demo/index.html) | prototype。一个文件，不用构建，在浏览器里打开就行。 |

## 数据与参考

| 文档 | 回答什么 |
|---|---|
| [`ERD.md`](ERD.zh-CN.md) | 从跑着的数据库画出来的 schema，以及它故意没有的那三样东西。也见 [English](ERD.md) 和 [繁體中文](ERD.zh-TW.md)。 |
| [`screenshots.md`](screenshots.zh-CN.md) | 跑着的数据层和它服务的那些客户端的截图。 |

## 素材

上面引到的图放在 `../product/`（prototype 的截图和那一页 PDF）和
[`diagrams/`](diagrams/)（schema 图和架构图，由脚本生成）。

## 关于两个旧客户端

`../frontend/` 和 `../mobile/` 是界面搬到 [`../web/`](../web/) 之前做的桌面端和手机端。它们仍然跑在同一个数据层上，留着它们是因为可用性那部分工作和它的测试都在它们旁边。新的界面工作在 `web/` 里做。
