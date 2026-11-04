<h1 align="center">Following It Is All You Need.</h1>

<p align="center">
  <b>NextStop</b><br>
  你有 73 分鐘，和一張空白。<br>
  它寫一張，並且在一天動起來的時候守住它。
</p>

<p align="center">
  <a href="https://henryliu520.github.io/nextstop/"><b>▶ 試一下原型</b></a> &nbsp;·&nbsp;
  <a href="docs/why.zh-TW.md">為什麼有這個專案</a> &nbsp;·&nbsp;
  <a href="docs/highlights.zh-TW.md">它憑什麼不一樣</a> &nbsp;·&nbsp;
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
  <img alt="規劃器仍是樁" src="https://img.shields.io/badge/planner-still%20a%20stub-C8452D">
</p>

<p align="center">
  <img src="product/web-day.png" width="230" alt="一張卡，一疊塊">
</p>

<p align="center"><sub>一張卡、一個時間、一個去向。剩下的一天在下面。</sub></p>

---

週四 09:47。KB 的課剛下，下一節 11:00 在 CPD。

你會先花十一分鐘不決定，然後走到 CPD，在教室外面再坐四十分鐘 —— 因為坐在一個你確定被允許坐的地方，不需要做任何決定。

那一個小時不是被浪費掉的，它只是從來沒被決定過。

NextStop 把它決定掉。

---

## 這事總在誰身上發生

- **國際生**，換了個國家讀書，還是搞不清九十分鐘該坐哪。
- **高考完的第一年**，再沒人告訴你該去哪接著卷。
- **電梯口那位**，已經在"按哪一層"上決定了十一分鐘。
- **下午三點四十才吃午飯那位**，因為中午那個決定太大了。
- **五個人都說"隨便"**，然後二十分鐘哪也沒去。

這五個人手裡那臺手機，都知道哪些房間空著。

---

## 它做到的、那些工具做不到的四件事

| | | |
|---|---|---|
| **它替你決定，而不是給你清單** | 查詢器回答一個集合，計劃回答一個序列 | [為什麼](docs/why.zh-TW.md) |
| **越用越聰明** | 學你實際走多快、你的一天實際幾點結束、你實際會去哪。同一個引數滿三次觀測，才替換預設值 | [閃光點](docs/highlights.zh-TW.md) |
| **極度便捷** | 一天一個動作，然後照著走。需要的運算元只會越來越少，不會越來越多 | [介面](docs/screens.zh-TW.md) |
| **它從不先開口** | 沒有推送、沒有對話氣泡、沒有"需要幫忙嗎"。真有事時，只說什麼變了，並給一個撤回 | [閃光點](docs/highlights.zh-TW.md) |

學到的永遠是預設值，不是被拿走的按鈕。按鈕一直留在原位。

---

## 它長什麼樣

<p align="center">
  <img src="product/day.png" width="240" alt="一天">
  <img src="product/repair.png" width="240" alt="拖堂 20 分鐘之後">
  <img src="product/plan.png" width="240" alt="規劃頁">
</p>

<p align="center"><sub>左：一天是一疊塊。中：拖堂 20 分鐘。右：一週、一天、一句話。</sub></p>

課給"不去"，吃飯給"換一個"，見面給"一鍵預約"。沒有空著的時間，空隙預設是自習。

---

## 一天動起來的時候

一節課拖堂二十分鐘，兩種答法：

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

一次改五塊，是計劃開始不被信任的方式。所以這裡問的不是"最好的一天長什麼樣"，而是一個更窄的問題：哪幾站真的壞了，然後只動那幾站。兩個停靠點算同一站，條件是目的、樓棟、開始時間都還對得上。

👉 判據、過程與代價順序：**[algorithm.zh-TW.md](docs/algorithm.zh-TW.md)**

---

## 它是怎麼搭起來的

```
web/     Vue 3 + Vite，一套構建，四個容器
         網頁 · PWA · Android 殼 · iOS 殼
   |
   |  REST + JSON
   v
backend  Spring Boot + PostgreSQL
         課表 · 房間 · 可用性 · 審計 · 帳號
```

規劃、修復、介面都在客戶端，所以走廊裡沒訊號也能用。底下那個服務是專案早前做完的，它把一件事答得很準：**哪些房間現在空著**，於是計劃不會指向一個不存在的地方。

---

## 文件，每份三語

| | English | 簡體 | 繁體 |
|---|---|---|---|
| **為什麼有這個專案** | [`why.md`](docs/why.md) | [`zh-CN`](docs/why.zh-TW.md) | [`zh-TW`](docs/why.zh-TW.md) |
| **它憑什麼不一樣** | [`highlights.md`](docs/highlights.md) | [`zh-CN`](docs/highlights.zh-TW.md) | [`zh-TW`](docs/highlights.zh-TW.md) |
| **按旅程列的功能** | [`features.md`](docs/features.md) | [`zh-CN`](docs/features.zh-TW.md) | [`zh-TW`](docs/features.zh-TW.md) |
| **技術架構** | [`architecture.md`](docs/architecture.md) | [`zh-CN`](docs/architecture.zh-TW.md) | [`zh-TW`](docs/architecture.zh-TW.md) |
| **修復判據** | [`algorithm.md`](docs/algorithm.md) | [`zh-CN`](docs/algorithm.zh-TW.md) | [`zh-TW`](docs/algorithm.zh-TW.md) |
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

`web/` 把 `/api` 代理到 8080，所以你看到的一天是用真實可用性資料拼出來的。

不想構建？開啟 [`product/demo/index.html`](product/demo/index.html)，單檔案、不用服務、能離線，右上角切三種語言。

---

## 哪些是真的，哪些不是

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
