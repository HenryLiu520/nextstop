<h1 align="center">Following It Is All You Need.</h1>

<p align="center">
  <b>NextStop</b><br>
  你有 73 分鐘，沒有計劃。<br>
  它替你寫一張，一天動了，它修。
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
  <img src="product/day.png" width="340" alt="一張卡，一疊塊">
</p>

<p align="center"><sub>一張卡，一個時間，一個去向。這一天剩下的部分在下面。</sub></p>

---

週四 09:47。KB 的課剛下，下一節 11:00 在 CPD，中間 73 分鐘，走路吃掉十一分鐘。

你會先花一陣子不決定，然後走去 CPD，在教室外面再坐四十分鐘。因為你確定自己能坐的地方，什麼都不要求你。

那一小時不是被故意浪費的。它只是從來沒被決定。

NextStop 把它決定掉。

---

## 這事總落在誰頭上

- **國際生**，這學期剛落地，九十分鐘還不知道能坐在哪。
- **剛考完高考的第一年**，再沒有人告訴你去哪接著卷。
- **電梯口那位**，在"按幾樓"這件事上已經想了十一分鐘。
- **15:40 才吃上午飯那位**，中午那個決定太大了。
- **五個人都說"隨便"**，然後二十分鐘哪兒也沒去。

這五個人手裡的手機，都知道哪些教室空著。📱

---

## ⚡ 極度方便

找空教室的應用給你一張列表，再讓你填四個它其實知道答案的框。它有你的課表。空檔 73 分鐘它知道，走路十一分鐘它也知道。NextStop 什麼都不問。開啟就是答案，一張卡，一個時間，一個去向。KB 227，到 12:00，步行 5 分鐘。

一天要做的運算元，是這裡唯一要數的數，它只能往下走。第一版是 3，選教室、選時間、確認一天。時間能從空檔裡直接算出來，降到 2。開啟應用的那一刻，那一天就已經算好，降到 1，剩下的工作只有開啟。目標是 0，讀到的那天就是對的。每個新功能都得在卡里面付賬，不許用新螢幕、新問題來付。

算術全部在手機上跑，走廊裡沒訊號，不算它失敗的場合。計劃只在開啟那一刻算一次，之後只讀。再開啟，渲染的是手裡已經有的同一天，下午不會趁你看它的時候自己重排。功能都長在計劃層裡，到螢幕上的，始終只有一張卡。

---

## 📈 越用越聰明

NextStop 學四個數，每個都來自你本來就在做的動作，從不來自一張表單。點"我遲到了"，計劃和你點選之間的差，就是你真實走路的速度。點"結束今天"，那個鐘點就是你一天真正的收工時間。改一餐時選的那個選項，就是你真的去吃的地方。在某節課上點"不去"，那節課你就是不去。

每個數是一個引數加一個計數器。沒有模型檔案，沒有訓練，做計劃時也沒有一次推理。觀察不滿三次，預設值站著不動，介面寫著"還在學你"。電梯壞了晚到一次，不會讓你一學期都揹著走路慢的名聲。學習永遠不改演算法。走路係數 1.28 只乘步行矩陣，別的什麼都不碰，所以審計它只需要讀一個數。

每個值都在 Mine 頁上，帶著自己的計數器，用的是計劃層的單位。看得到，也改得掉。改掉儲存，計數器清零，接下來三次觀察會在你給的數上繼續打磨，而不是跟它吵架。

*現狀：這些值今天就在 Mine 頁上，可以直接改。從動作推出它們的推導層還在設計裡，沒有跑。*

---

## 🧠 極度智慧

NextStop 拿一張課表和一輪偏好，算出一整天。每個空檔先減掉步行和緩衝，再往裡放東西。每個空檔拿到一個目的、一個地點，沒有空的空檔。一頓飯不塞猜出來的時長，它拿到的是一個最晚出發時間，和兩個選項。

一句話改計劃。"明天下午留白自習，三點前回實驗室"會拆成一串已經存在的動作，先預覽，再應用，隨時撤銷。語言是同一排按鈕的第二扇門，不是特權通道。一句話落不進已有動作，它就失敗，並且明說，不發明能力。把輸入框刪掉，剩下的還是一個完整應用，這是每個能力見到語言之前要過的測試。

一天動了，它修，不重算。拖堂二十分鐘，動一塊，全盤重算動五塊。一站能活過修復，靠它的目的、它的樓、它的開始時間仍然對得上。底下它同時稱量幾件事，你走多遠，你等多久，你吃沒吃飯，下一件事有多硬。

*現狀：這一段背後的規劃器今天是個樁。修復階梯和句子解析還在設計裡，沒有跑。*

---

## 它長什麼樣

<p align="center">
  <img src="product/triptych.png" width="620" alt="一天、拖堂 20 分鐘後修好、規劃頁">
</p>

<p align="center"><sub>左：一天一疊塊 · 中：拖堂 20 分鐘，修好，只動一塊 · 右：一週，一天，一句話。</sub></p>

課給"不去"，飯給"換一個"，會給"預約"。沒有空的空檔，空檔預設自習。

---

## 一天動起來的時候

一節課拖堂二十分鐘。兩種答法：

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

五塊齊動，是計劃失去信任的方式。修復問的比重算窄。它問哪幾站真的斷了，只動那幾站，兩站算同一站，靠的是目的、樓棟、開始時間都還對得上。

👉 判據、過程與代價順序：**[algorithm.zh-TW.md](docs/algorithm.zh-TW.md)**

---

## 它是怎麼搭的

```
web/     Vue 3 + Vite，一套構建，四個容器
         網頁 · PWA · Android 殼 · iOS 殼
   |
   |  REST + JSON
   v
backend  Spring Boot + PostgreSQL
         課表 · 房間 · 可用性 · 審計 · 帳號
```

計劃、修復、介面全在客戶端跑，走廊裡沒訊號照樣能用。底下的可用性服務是專案早期搭好的，它只把一個問題答準，哪些房間空著，所以計劃不會指向不存在的地方。

### 從課表到一個房間

五步，順序固定。

1. **讀**。課表從學校的只讀介面來，它被當作正確的，不校驗，不質疑。
2. **同步**。日曆訂閱並進來，只讀，不回寫。
3. **加**。你自己塞的一次性事情，看牙、取快遞。和課一樣的分量，區別是你能刪。
4. **並**。三個來源併成一份「已佔用」。重疊的合併，相鄰不到十五分鐘的也合併，因為十分鐘不是空檔，是走廊。
5. **扣**。每個空檔扣掉走路和緩衝，走路乘你自己的係數，還沒學會之前是 1.00。

剩下的是一串視窗。每個視窗給一個目的和一個地點，吃飯給的是「最晚幾點必須走」而不是猜出來的時長，剩下的全填自習，所以視窗不會空著。最後一件事是房間：先過硬條件（整段都空、座位夠、你能進、不是你剛離開的那棟），再用代價在活下來的幾間裡挑。

```
代價 = 1.0 × 步行分鐘
     + 0.3 × |樓層 − 你偏好的樓層|
     + 0.2 × (1 − 去過次數 / 10)
     + 0.5 × 噪音
```

任何一條硬條件不過，這間就出局。權重只負責在已經合格的房間裡挑一間。

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

`web/` 把 `/api` 代理到 8080，螢幕上這一天是用真實可用性答案拼出來的。

不想構建？開啟 [`product/demo/index.html`](product/demo/index.html)，單檔案，不用服務，能離線，右上角切三種語言。

---

## 哪些是真的，哪些不是

| 部分 | 狀態 |
|---|---|
| 可用性引擎、規則、角色、審計、27 個測試 | **已實現並測試** |
| Web 介面，三個 tab，三語，真實可用性 | **已實現** |
| 單檔案原型，可點完整流程 | **已實現** |
| Mine 頁與可編輯引數 | **已實現** |
| 規劃器 | **樁，每個空檔放一個自習塊** |
| 修復階梯與同一站判據 | **已設計，未實現** |
| 學習層 | **已設計，未實現** |
| 一句話預覽 | **原型裡只有一句演示句** |
| PWA manifest、Android 與 iOS 殼 | **已選定，未構建** |

課表是樣例資料。"空閒"不等於"沒人"。"動一塊 vs 動五塊"是示例，不是實測。鑑權是原型級。那些學到的數，是設計的方向，不是實測值。

我們用這張表代替路線圖。

---

<p align="center"><sub>
COMP1110 第 08 組 · 香港大學 · LIU Haoran (u3686264)<br>
名字是拿一篇講 attention 的論文開的玩笑。
</sub></p>
