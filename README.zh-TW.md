<h1 align="center">Following It Is All You Need.</h1>

<p align="center">
  <b>NextStop</b><br>
  替一個學生決定兩節課之間該去哪，<br>
  並且在一天被打亂時守住這個決定。
</p>

<p align="center">
  <a href="https://henryliu520.github.io/nextstop/"><b>▶ 試一下原型</b></a> &nbsp;·&nbsp;
  <a href="docs/why.zh-TW.md">為什麼有這個專案</a> &nbsp;·&nbsp;
  <a href="docs/algorithm.zh-TW.md">真正難的部分</a> &nbsp;·&nbsp;
  <a href="product/NextStop.pdf">一頁 PDF</a>
</p>

<p align="center">
  <a href="README.md">English</a> · <a href="README.zh-TW.md">簡體中文</a> · <a href="README.zh-TW.md">繁體中文</a>
</p>

<p align="center">
  <img alt="Vue 3" src="https://img.shields.io/badge/Vue-3-42b883?logo=vuedotjs&logoColor=white">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white">
  <img alt="PostgreSQL 17" src="https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white">
  <img alt="27 個測試" src="https://img.shields.io/badge/tests-27%20passing-1C1A17">
  <img alt="原型" src="https://img.shields.io/badge/status-prototype-C8452D">
</p>

<p align="center">
  <img src="product/web-day.png" width="230" alt="一張卡，一疊塊">
</p>

---

## 週四 09:47

KB 的課提前下了。下一節 11:00 在 CPD。

```
09:47  KB      you are here, holding a laptop and seventy minutes
10:50          the next class starts, in a different building
        ?
```

你知道學校有空房間，你知道有圖書館，你知道自己欠著一篇報告。你不知道的是 **接下來這一小時，在這棟樓還是隔壁樓，該做哪一件事**。

於是你站在走廊裡自己決定。一天三次到五次，每天都這樣，你其實也習慣了。**而這就是沒人做的那部分。**

---

## 查詢器回答的，和你需要的

```
ROOM FINDER                     YOU
────────────────────────────    ────────────────────────────
"14:00 to 15:50, 12 rooms       "I have 70 minutes, my laptop
 free in this building."         is dying, and I have 2 hours
                                 of reading to do."
   ↑ an answer to a question       ↑ a decision about a day
     you did not ask
```

查詢器返回一個**集合**。而一天是一條**序列**，你現在做的選擇會改變後面能做什麼。這就是為什麼再多可用性資料也修不好這件事。

---

## 它給的是什麼

一張卡。一疊塊。塊與塊之間只寫走路時間。**沒有空著的時間。**

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

圖例就是全部：**實心**是已經解出來的，**虛線**是外部事實（比如已經訂到的房間），**點線**是還待訂。已經過去的塊壓成一條細線，因為它不該繼續搶注意力。

---

## 打亂時，它只改一塊，不是五塊

一節課拖堂二十分鐘，兩種反應：

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

一個每次被打斷就整體重寫的計劃，跟沒有計劃是無法區分的。所以修復問的不是"最好的一天長什麼樣"，而是一個更窄的問題：**哪幾站真的壞了**，然後只動那幾站。兩個停靠點算同一站，條件還是那三條：目的、樓棟、開始時間都還對得上。容忍度是引數，不是天條。

👉 判據、過程與代價順序都在 **[algorithm.zh-TW.md](docs/algorithm.zh-TW.md)**。

---

## 四個動作，沒有工具欄

| 你在哪 | 你能做什麼 | 你不能做什麼 |
|---|---|---|
| 一節課 | 不去 | 改時間、取消、重排 |
| 一段自習 | 換一個 | 拆成兩段 |
| 吃飯 | 換一個 | 替你點單 |
| 一個見面 | 預約、換地方 | 訂一間學校不允許你訂的房 |

最後一行很重要。**系統不假裝能分配它分配不了的房間。** 普通教室給一個可撤回的認領，真有預約流程的場地，它只說什麼時候該訂，然後把結果當成事實貼回計劃。

---

## 為什麼後端不再是產品

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

後端回答的是**我能去哪**。那是學生不知道的三件事之一，而且是最容易的那件。計劃在客戶端，所以走廊裡沒訊號也能用，也所以換一臺裝置看不到你的計劃。兩個後果都如實寫在這裡。

---

## 文件，每份三語

| | English | 簡體 | 繁體 |
|---|---|---|---|
| **為什麼有這個專案** | [`why.md`](docs/why.md) | [`zh-CN`](docs/why.zh-TW.md) | [`zh-TW`](docs/why.zh-TW.md) |
| **別人做不到的地方** | [`highlights.md`](docs/highlights.md) | [`zh-CN`](docs/highlights.zh-TW.md) | [`zh-TW`](docs/highlights.zh-TW.md) |
| **按旅程列的功能** | [`features.md`](docs/features.md) | [`zh-CN`](docs/features.zh-TW.md) | [`zh-TW`](docs/features.zh-TW.md) |
| **技術架構** | [`architecture.md`](docs/architecture.md) | [`zh-CN`](docs/architecture.zh-TW.md) | [`zh-TW`](docs/architecture.zh-TW.md) |
| **真正難的部分** | [`algorithm.md`](docs/algorithm.md) | [`zh-CN`](docs/algorithm.zh-TW.md) | [`zh-TW`](docs/algorithm.zh-TW.md) |
| **介面與規則** | [`screens.md`](docs/screens.md) | [`zh-CN`](docs/screens.zh-TW.md) | [`zh-TW`](docs/screens.zh-TW.md) |
| **決策記錄** | [`adr/`](docs/adr/README.md) | [`zh-CN`](docs/adr/README.zh-TW.md) | [`zh-TW`](docs/adr/README.zh-TW.md) |
| **全部索引** | [`docs/README.md`](docs/README.md) | [`zh-CN`](docs/README.zh-TW.md) | [`zh-TW`](docs/README.zh-TW.md) |

---

## 跑起來

```bash
git clone https://github.com/HenryLiu520/nextstop
cd nextstop

./scripts/start-all.sh                  # PostgreSQL 5433 → Spring Boot 8080
cd web && npm install && npm run dev    # http://127.0.0.1:5175
```

`web/` 把 `/api` 代理到 8080，所以你看到的一天是用真實可用性資料拼出來的。背後的規劃器仍是樁，介面不掩飾。

不想構建？開啟 [`product/demo/index.html`](product/demo/index.html)，單檔案、不用服務、能離線，右上角切 English / 簡體 / 繁體。

---

## 它永遠不會做的事

1. 問一個它自己看兩眼就能知道答案的問題。
2. 給你一塊空白，然後管它叫計劃。
3. 一改就改三塊，還不告訴你。
4. 假裝那間房是你的。
5. 評價你去沒去上課。

---

## 做了、設計了、沒做

| 部分 | 狀態 |
|---|---|
| 可用性引擎、規則、角色、審計、27 個測試 | **已實現並測試** |
| Web 介面、三個 tab、三語、真實可用性 | **已實現** |
| 單檔案原型，可點完整流程 | **已實現** |
| 規劃器與修復階梯 | **已設計，未實現** |
| 學習層 | **已設計，未實現** |
| PWA manifest、Android 與 iOS 殼 | **已選定，未構建** |

課表是樣例資料。"空閒"不等於"沒人"。"改一塊 vs 改五塊"是示例，不是實測結果。鑑權是原型級。這裡沒有一句撐不起的話。

---

## 倉庫結構

```
web/               介面：Vue 3 + Vite，一套程式碼兼顧手機與桌面
backend/           Spring Boot 服務：課表、可用性、帳號、審計
frontend/          早期桌面客戶端，保留因為可用性相關工作在這裡
mobile/            早期手機客戶端，原因同上
mock-school-api/   模擬學校課表源，含設計記錄
docs/              十二份文件，每份三語
product/           原型、截圖、一頁 PDF
scripts/           啟動 / 停止 / 測試
```

---

<p align="center"><sub>
COMP1110 第 08 組 · 香港大學 · LIU Haoran (u3686264)<br>
名字是拿一篇講 attention 的論文開的玩笑。那篇講該注意什麼，這個也講該注意什麼。
</sub></p>
