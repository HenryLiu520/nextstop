# 文件

[English](README.md) · [簡體中文](README.zh-CN.md) · **繁體中文**

整個專案的一張地圖。檔名自己表明了語言，下面列出的每一份都有對應的中文版。

如果你只讀一樣東西，那就讀短的那個版本：
---

## 從這裡開始

| 文件 | 回答什麼 |
|---|---|
| [`why.md`](why.zh-TW.md) | 這東西為什麼存在。那個時刻，一個學生不知道的東西，為什麼找空教室的工具和日曆都蓋不住它，以及這個專案針對的那個缺口。 |
| [`highlights.md`](highlights.zh-TW.md) | 它做了四件顯而易見的那幾個替代品做不到的事，以及每一件是怎麼做到的。 |
| [`features.md`](features.zh-TW.md) | 按使用者旅程組織的每個功能。它做什麼，你看到什麼，它依據什麼。每一行都標了 Built、Data layer 或 Specified。 |

## 它是怎麼工作的

| 文件 | 回答什麼 |
|---|---|
| [`architecture.md`](architecture.zh-TW.md) | 資料層和計劃層之間的那條分界落在哪裡，兩層怎麼接上，以及交付目標。 |
| [`algorithm.md`](algorithm.zh-TW.md) | 四個層，相同的判據，修復的流程，成本的次序，複雜度，和一個走通了的例子。 |
| [`room-scoring.md`](room-scoring.zh-TW.md) | 固態分和動態分怎麼合成一個選房決定，含七個因子、拒絕折扣、融合權重和一個走通了的例子。也見 [English](room-scoring.md) 和 [簡體中文](room-scoring.zh-CN.md)。 |
| [`day.md`](day.zh-TW.md) | 一天走到底：三個輸入來源、併集、走路與緩衝的算術、一個走通的週四剩下的三個視窗、選房的四道硬過濾、發布、修復，和四個學到的量。也見 [English](day.md) 和 [簡體中文](day.zh-CN.md)。 |
| [`screens.md`](screens.zh-TW.md) | 三個標籤頁，塊的規格，動作矩陣，設計 token，和三條介面規則。 |

## 決定

架構上的決定，按做出來的順序。每一份都記下選了什麼，代價是什麼，否掉了什麼。

| 記錄 | 主題 |
|---|---|
| [`adr/README.md`](adr/README.zh-TW.md) | 五份記錄的索引 |
| [`adr/0001`](adr/0001-backend-is-the-data-layer.zh-TW.md) | 後端提供課表，它不持有計劃。 |
| [`adr/0002`](adr/0002-plan-lives-in-the-client.zh-TW.md) | 計劃是客戶端狀態。 |
| [`adr/0003`](adr/0003-repair-instead-of-recompute.zh-TW.md) | 一個偏離去修計劃，而不是重算計劃。 |
| [`adr/0004`](adr/0004-one-ui-four-targets.zh-TW.md) | 一次構建，四個交付目標：瀏覽器、PWA、Android 殼、iOS 殼。 |
| [`adr/0005`](adr/0005-room-attributes-are-scored-on-the-server.zh-TW.md) | 房間屬性在服務端打分，客戶端按修訂號快取。 |

## 跑起來

| 文件 | 回答什麼 |
|---|---|
| [`DEPLOY.md`](DEPLOY.zh-TW.md) | 一步一步：資料庫、服務、客戶端、埠、帳號、排查。也見 [English](DEPLOY.md) 和 [繁體中文](DEPLOY.zh-TW.md)。 |
| [`../product/demo/index.html`](../product/demo/index.html) | prototype。一個檔案，不用構建，在瀏覽器裡開啟就行。 |

## 資料與參考

| 文件 | 回答什麼 |
|---|---|
| [`ERD.md`](ERD.zh-TW.md) | 從跑著的資料庫畫出來的 schema，以及它故意沒有的那三樣東西。也見 [English](ERD.md) 和 [繁體中文](ERD.zh-TW.md)。 |
| [`screenshots.md`](screenshots.zh-TW.md) | 跑著的資料層和它服務的那些客戶端的截圖。 |

## 素材

上面引到的圖放在 `../product/`（prototype 的截圖和那一頁 PDF）和
[`diagrams/`](diagrams/)（schema 圖和架構圖，由指令碼生成）。

## 關於兩個舊客戶端

`../frontend/` 和 `../mobile/` 是介面搬到 [`../web/`](../web/) 之前做的桌面端和手機端。它們仍然跑在同一個資料層上，留著它們是因為可用性那部分工作和它的測試都在它們旁邊。新的介面工作在 `web/` 裡做。
