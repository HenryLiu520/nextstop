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

<p align="center"><sub>一張卡、一個時間、一個去向。剩下的一天在下面。</sub></p>

---

查詢器告訴你哪些房間空著，日曆存著你已經決定的事。兩者都不回答一個學生 09:47 站在走廊裡真正的問題：**現在幹什麼**。

NextStop 回答這個，而且在一天動起來的時候，改得儘量少。

---

## 這事發生在誰身上

- **高考完的第一年**，再沒人告訴你該去哪接著卷。
- **交換生**，兩節課之間兩小時，找不到一個能正大光明打盹的地方。
- **站在電梯口那位**，已經"決定"了十一分鐘。
- **下午三點四十才吃午飯那位**，因為中午那個決定太大了。
- **五個人都說"隨便"**，然後二十分鐘哪也沒去。

他們都不缺資訊。都缺下一步。

---

## 它給什麼

<p align="center">
  <img src="product/day.png" width="240" alt="一天">
  <img src="product/repair.png" width="240" alt="拖堂 20 分鐘後的修復">
  <img src="product/plan.png" width="240" alt="規劃頁">
</p>

<p align="center"><sub>左：一天是一疊塊。中：拖堂 20 分鐘只改一塊，不是五塊。右：一週、一天、一句話。</sub></p>

三個 tab，沒有工具欄。塊自己帶著動作，而且沒有空著的時間。

---

## 真正難的部分

不是決定一天，是決定**打亂之後最少改多少**。

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

一個每次被打斷就整體重寫的計劃，跟沒有計劃無法區分。兩個停靠點算同一站，條件是目的、樓棟、開始時間都還對得上。
👉 **[algorithm.zh-TW.md](docs/algorithm.zh-TW.md)**

---

## 為什麼後端不再是產品

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

後端回答的是**我能去哪**，那是學生不知道的三件事之一，也是最容易的那件。計劃在客戶端，所以走廊裡沒訊號也能用，也所以換一臺裝置看不到你的計劃。兩個後果都如實寫在這裡。

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

不想構建？開啟 [`product/demo/index.html`](product/demo/index.html)，單檔案、不用服務、能離線，右上角切三種語言。

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

課表是樣例資料。"空閒"不等於"沒人"。"改一塊 vs 改五塊"是示例，不是實測。鑑權是原型級。

---

<p align="center"><sub>
COMP1110 第 08 組 · 香港大學 · LIU Haoran (u3686264)<br>
名字是拿一篇講 attention 的論文開的玩笑。那篇講該注意什麼，這個也講該注意什麼。
</sub></p>
