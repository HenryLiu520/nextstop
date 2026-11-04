<p align="center">
  <img src="product/web-day.png" width="290" alt="一張卡，一疊塊">
</p>

<h1 align="center">Following It Is All You Need.</h1>

<p align="center">
  <b>NextStop</b><br>
  替一個學生決定兩節課之間該去哪，並且在一天被打亂時守住這個決定。
</p>

<p align="center">
  <a href="https://henryliu520.github.io/nextstop/"><b>▶ 試一下原型</b></a> &nbsp;·&nbsp;
  <a href="docs/why.zh-TW.md">為什麼有這個專案</a> &nbsp;·&nbsp;
  <a href="docs/architecture.zh-TW.md">技術架構</a> &nbsp;·&nbsp;
  <a href="docs/algorithm.zh-TW.md">核心演算法</a> &nbsp;·&nbsp;
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

---

<p align="center">
  <img src="product/web-desktop.png" width="620" alt="桌面寬度下，同一套介面裝在一個手機框裡">
</p>

<p align="center"><sub>一套介面。桌面寬度下它裝在一個手機框裡，因為它就是同一套介面。</sub></p>

學生上完一節課走出來，手上還有七十分鐘。校園裡到處是空房間。
他不知道的是**能去哪裡**。查詢器回答"哪間房空著"，日曆存"你已經決定了什麼"，兩者都不回答學生真正的問題：**現在幹什麼**。

NextStop 回答這個。一張卡、一個時間、一個去向。一天動起來的時候，它改得儘量少。

---

## 它是什麼

| | |
|---|---|
| **它回答** | 接下來這一小時做什麼，以及在哪 |
| **它不做** | 給你一張清單讓你自己挑 |
| **你要做的** | 一天開啟一次，然後照著走 |
| **它不是** | 查詢器，也不是日曆 |

兩節課之間，學生不知道三件事：**能去哪裡、被允許去哪裡、做什麼**。房間只是其中一小步。

---

## 它怎麼決定

```
1  固定    課表。權威、不可疑。
2  空出    承諾之間的空隙，扣掉走路和緩衝。
3  填      每段空隙放什麼、放哪，由可用性層給候選。
4  修復    現實偏離時，儘量少改。
```

真正難的不是決定一天，是決定**打亂之後最少改多少**。兩個停靠點算同一件事，必須目的、樓棟、開始時間都還對得上。修復按代價順序往上爬：挪幾分鐘、換同樓房間、換樓、取消一站，**最後才動出勤**，而出勤是人自己的決定。

> 查詢器回答一個集合。計劃回答一個序列。

---

## 介面

<p align="center">
  <img src="product/day.png" width="240" alt="一天">
  <img src="product/repair.png" width="240" alt="拖堂 20 分鐘後的修復">
  <img src="product/plan.png" width="240" alt="規劃頁">
</p>

<p align="center"><sub>左：一張卡，一天是一疊塊。中：拖堂 20 分鐘只改一塊，不是五塊。右：一週、一天、一句話。</sub></p>

三個 tab，三件事，沒有工具欄。塊自己帶著它的動作：課給"不去"，吃飯給"換一個"，見面給"一鍵預約"。**沒有空著的時間**，空隙預設是自習。

---

## 一張圖說清架構

```
web/            Vue 3 + Vite，一套構建，四個容器
                網頁 · PWA · Android 殼 · iOS 殼
     |
     |  REST + JSON（開發時由 Vite 代理 /api）
     v
backend/        Spring Boot 4.1.1、Java 21、PostgreSQL 17
                課表 · 房間 · 可用性 · 佔用變更 · 審計 · 帳號
     |
     v
mock-school-api/ 模擬學校課表源（真實介面尚未接入）
```

後端不再是產品。它回答"我能去哪"，存在的意義是保證計劃不會指向一個不存在的地方。計劃在客戶端，所以走廊裡沒訊號也能用。

---

## 文件

十二份文件，每份三語。從第一行開始讀。

| | English | 簡體 | 繁體 |
|---|---|---|---|
| **為什麼有這個專案** | [`why.md`](docs/why.md) | [`zh-CN`](docs/why.zh-TW.md) | [`zh-TW`](docs/why.zh-TW.md) |
| **別人做不到的地方** | [`highlights.md`](docs/highlights.md) | [`zh-CN`](docs/highlights.zh-TW.md) | [`zh-TW`](docs/highlights.zh-TW.md) |
| **按使用者旅程的功能** | [`features.md`](docs/features.md) | [`zh-CN`](docs/features.zh-TW.md) | [`zh-TW`](docs/features.zh-TW.md) |
| **技術架構** | [`architecture.md`](docs/architecture.md) | [`zh-CN`](docs/architecture.zh-TW.md) | [`zh-TW`](docs/architecture.zh-TW.md) |
| **核心演算法** | [`algorithm.md`](docs/algorithm.md) | [`zh-CN`](docs/algorithm.zh-TW.md) | [`zh-TW`](docs/algorithm.zh-TW.md) |
| **介面與規則** | [`screens.md`](docs/screens.md) | [`zh-CN`](docs/screens.zh-TW.md) | [`zh-TW`](docs/screens.zh-TW.md) |
| **決策記錄** | [`adr/`](docs/adr/README.md) | [`zh-CN`](docs/adr/README.zh-TW.md) | [`zh-TW`](docs/adr/README.zh-TW.md) |
| **全部索引** | [`docs/README.md`](docs/README.md) | [`zh-CN`](docs/README.zh-TW.md) | [`zh-TW`](docs/README.zh-TW.md) |

四條決策寫成了 ADR：後端是資料層、計劃在客戶端、偏離時修復而非重算、一套構建服務四個容器。

---

## 跑起來

```bash
git clone https://github.com/HenryLiu520/nextstop
cd nextstop

./scripts/start-all.sh                  # PostgreSQL 5433 → Spring Boot 8080
cd web && npm install && npm run dev    # http://127.0.0.1:5175
```

`web/` 把 `/api` 代理到 8080，所以你看到的一天是用真實可用性資料拼出來的。背後的規劃規則仍是樁，介面不掩飾這一點。

不想構建？開啟 [`product/demo/index.html`](product/demo/index.html)，單檔案，或者直接看線上版
<https://henryliu520.github.io/nextstop/>。

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

我們不聲稱更多。課表是樣例資料。"空閒"不等於"沒人"。"改一塊 vs 改五塊"是示例，不是實測結果。鑑權是原型級。

---

## 倉庫結構

```
web/               介面：Vue 3 + Vite，一套程式碼兼顧手機與桌面
backend/           Spring Boot 服務：課表、可用性、帳號、審計
frontend/          早期桌面客戶端，保留因為可用性相關工作在這裡
mobile/            早期手機客戶端，原因同上
mock-school-api/   模擬學校課表源，含設計記錄
docs/              十二份文件，從 docs/README.md 開始
product/           原型、截圖、一頁 PDF
scripts/           啟動 / 停止 / 測試
```

---

<p align="center"><sub>
COMP1110 第 08 組 · 香港大學 · LIU Haoran (u3686264)<br>
課程作業。第三方元件在 <a href="THIRD-PARTY.md">THIRD-PARTY.md</a> 裡逐項致謝。
</sub></p>
