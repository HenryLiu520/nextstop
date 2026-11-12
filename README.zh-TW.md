<h1 align="center">Following It Is All You Need.</h1>

<p align="center">
  <b>NextStop</b><br>
  <b>下課了，時間是你的。</b><br>
  它在你還未走到樓梯口之前，就把下一站定好了。
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
  <img alt="32 個測試" src="https://img.shields.io/badge/tests-32%20passing-1C1A17">
  <img alt="規劃器仍是樁" src="https://img.shields.io/badge/planner-still%20a%20stub-C8452D">
</p>

<p align="center">
  <img src="product/day.png" width="340" alt="一張卡，一疊塊">
</p>

<p align="center"><sub>一張卡，一個時間，一個去向。這一天剩下的部分在下面。</sub></p>

---

週四 09:47。KB 的課剛下，下一節 11:00 在 CPD，中間 73 分鐘，走路吃掉十一分鐘。

你會先猶豫一陣子，然後走去 CPD，在教室外面再坐四十分鐘。因為你確定自己能坐的地方，只管收留你。

那一小時白白流走。它整段懸在 KB 和 CPD 之間，等一個決定。

NextStop 把它決定掉。

---

## 背景

- **國際生**，這學期剛落地，九十分鐘還不知道能坐在哪。
- **剛考完高考的第一年**，再沒有人告訴你去哪接著卷。
- **電梯口那位**，在"按幾樓"這件事上已經想了十一分鐘。
- **15:40 才吃上午飯那位**，中午那個決定太大了。
- **五個人都說"隨便"**，然後二十分鐘哪兒也沒去。

這五個人手裡的手機，都知道哪些教室空著。

---

## ⚡ 極度便捷

**你開啟它，今天已經在那裡了。一張卡片，一個時間，一個填好的答案。**

你本來要用的空教室查詢，要你填**四個空**：哪棟樓，哪一天，從幾點，到幾點，而這四個空的答案它全都知道，因為它手裡握著你的課表。在這裡跟完一天，運算元是一條只收縮的線：**3 步 → 2 步 → 1 步**，目標是 0，而 0 夠得著，因為閱讀這一眼就把操作做完了。

排課只在揭開的那一刻算一次。計劃釋出之後保持只讀，再開啟一次，是把同一份計劃從記憶體裡照著畫出來，第二次開啟和第一次開啟是同一張卡片。釋出之後唯一會寫計劃的是修復。表單在這裡失去了位置，原因就在這裡：表單是答案已經存在之後才去問問題的地方，一天在揭開時就算完了，一整天裡每個問題都已經在那一刻問完了。

> 代價是前一天晚上。算術必須在你開啟它的那一秒裡跑完，在手機上，所以一輪新的往返花掉了計劃手裡的全部時間。可用性只能按時間窗一次取回，一間一間去問會花掉遠超一秒的時間。

*已實現：揭開一次，計劃只讀。3、2、1、0 是設計序列，沒有做過測量。*

---

## 📈 自主學習

**它自己讀出你走多快。**

它學**四個量**，每一個都從你本來就會做的動作裡讀出來，答案全在你原有的操作裡。步行速度，看你按「我晚了」之前留了多大的空隙。一天什麼時候結束，看你把「結束今天」按在哪個鐘點。你實際在哪兒吃飯，看幾個食堂都開著的時候你選了哪一個。你出勤的情況，看「不去」鈕上的每一次按壓。步行係數在設計上從 ×1.00 走到 ×1.28。

一個量要**滿三次觀測**才動，這道閘門就是全部規矩。第三次到來之前，預設值站著，螢幕在旁邊寫「還在瞭解你」。到第三次，引數取觀測到的值，做過平滑，某個奇怪的週二只是資料點，規矩跟著三次的總和走。動的只有引數，演算法保持原樣，所以一個學到的值，讀一個數字就能審計。這些值按設計擺在「我的」頁上，可見，可改，旁邊各掛一個計數器，學到第幾次一眼可見，手動改一個，那個計數器清零重來。

> 代價是學習擰的就是事先擺好的那幾個旋鈕。它調這四個量的讀數，規則表保持原樣，代價階梯保持原樣，動作集保持原樣，這四個量就是全部詞彙。出勤率留在裝置裡，儲存形式是一條條打卡記錄。

*已設計，未實現。原型跑固定預設值，同時掛著「還在瞭解你」。*

---

## 🧠 高度智慧

**從一張課表和一輪偏好開始，它把一整天算出來。**

每段空隙先扣掉步行時間和緩衝。剩下的每一段都有一個目的，一個地點。吃飯拿到一個最晚出發時刻，時長這一欄由它頂上。再剩下的都歸自習，每一段空隙都派了用場。這些你拿著課表走一遍就能對出來，而通用助手的可靠停在口頭，只留下一句保證；這個框把核對的機會遞到你手裡。

一句話只能按已經存在的按鈕，所以這個框只有四個落點。句子進來先出預覽：受影響的塊，新的結束時間，還有最要緊的一行，其餘各塊照舊。應用時寫下的撤銷記錄，和按鈕按下去寫的是同一條。超出四個落點的句子會把失敗說出來，計劃保持原樣，靜默的空操作只會喂大你對這個框的盲目信任。每個能力上崗前都要過**刪除測試**：把輸入框刪掉，剩下的必須還是一個完整的 app。日子破了也一樣，走修復路線，重算要改五塊，修復改**一塊**，一個停靠點只要目的、建築、開始時間都還成立，就原樣保留。

> 代價是措辭有天花板。需要一種新操作的請求會出聲地失敗，即興發揮被這個框關在門外，所以這個框比一個聊天視窗拒絕得更多。

*已實現：四個動作和它們的撤銷記錄。預覽只對一個寫好的例句存在，背後沒有解析器。*

---

## 介面

<p align="center">
  <img src="product/triptych.png" width="620" alt="一天、拖堂 20 分鐘後修好、規劃頁">
</p>

<p align="center"><sub>左：一天一疊塊 · 中：拖堂 20 分鐘，修好，只動一塊 · 右：一週，一天，一句話。</sub></p>

課給"不去"，飯給"換一個"，會給"預約"。沒有空的空檔，空檔預設自習。

---

## 規劃流程

五步，順序固定。

1. **讀**。課表從學校的只讀介面來，系統照單全收，把它當作正確的資料。
2. **同步**。日曆訂閱並進來，資料單向流入系統。
3. **加**。你自己塞的一次性事情，看牙、取快遞。和課一樣的分量，區別是你能刪。
4. **並**。三個來源併成一份「已佔用」。重疊的合併，相鄰間隔小於十五分鐘的也合併，因為十分鐘剛好夠走一條走廊。
5. **扣**。每個空檔扣掉走路和緩衝，走路乘你自己的係數，這個係數從 1.00 起步，學會之後由你來定。

剩下的是一串視窗。每個視窗給一個目的和一個地點，吃飯的時長來自「最晚幾點必須走」這條截止線，剩下的全填自習，每個視窗都裝滿內容。最後一件事是房間：先過硬條件（整段都空、座位夠、你能進、你剛離開的那棟直接出局），再用代價在活下來的幾間裡挑。

```
F = wS × S'  +  wD × D

S'  房间本身，七个因子，扣过拒绝折扣
D   走过去要多久，能坐多久
wD  0.40 到 0.65，窗口越长越看 S'
```

任何一間房撞上一條硬條件，它就出局。權重只負責在已經合格的房間裡挑一間。

房間怎麼選有一套自己的打分，七個固態因子加一個拒絕折扣，見 **[room-scoring.zh-TW.md](docs/room-scoring.zh-TW.md)**。在這之前的整條鏈路，從三個來源到併集，到緩衝的算術，到一個週四剩下的三個視窗，一直到發出去的那一版序列，一步一步走一遍，見 **[day.zh-TW.md](docs/day.zh-TW.md)**。

---

## 偏離與修復

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

判據、過程與代價順序：**[algorithm.zh-TW.md](docs/algorithm.zh-TW.md)**

---

## 架構

```
app      一個 HTML 檔，無需構建
(demo/)  product/demo/index.html · 瀏覽器 · PWA
         NextStop 是手機 app：一天 · 規劃 · 查找 · 我的，四個視圖，一個檔
   |
   |  REST + JSON
   v
backend  Spring Boot + PostgreSQL
         課表 · 房間 · 可用性 · 審計 · 帳號
```

計劃、修復、介面全在客戶端跑，走廊裡訊號斷了照樣能用。底下的可用性服務是專案早期搭好的，它只把一個問題答準：哪些房間空著，計劃給出的每個去處都對應一間此刻空著的真實房間。

---

## 包含內容

- 可點原型收在單檔案裡，自帶三種語言，離線也能跑。
- 可用性服務用 Spring Boot 與 PostgreSQL 搭成，專案早前完成，它回答哪些房間空著，32 個測試在它身後把關。
- 規劃器今天仍是樁，它交出一份有形狀的一天，真正的求解排在路線圖上。

## 文件

| | English | 簡體 | 繁體 |
|---|---|---|---|
| **為什麼有這個專案** | [`why.md`](docs/why.md) | [`zh-CN`](docs/why.zh-TW.md) | [`zh-TW`](docs/why.zh-TW.md) |
| **它憑什麼不一樣** | [`highlights.md`](docs/highlights.md) | [`zh-CN`](docs/highlights.zh-TW.md) | [`zh-TW`](docs/highlights.zh-TW.md) |
| **按旅程列的功能** | [`features.md`](docs/features.md) | [`zh-CN`](docs/features.zh-TW.md) | [`zh-TW`](docs/features.zh-TW.md) |
| **技術架構** | [`architecture.md`](docs/architecture.md) | [`zh-CN`](docs/architecture.zh-TW.md) | [`zh-TW`](docs/architecture.zh-TW.md) |
| **修復判據** | [`algorithm.md`](docs/algorithm.md) | [`zh-CN`](docs/algorithm.zh-TW.md) | [`zh-TW`](docs/algorithm.zh-TW.md) |
| **房間評分** | [`room-scoring.md`](docs/room-scoring.md) | [`zh-CN`](docs/room-scoring.zh-CN.md) | [`zh-TW`](docs/room-scoring.zh-TW.md) |
| **介面與規則** | [`screens.md`](docs/screens.md) | [`zh-CN`](docs/screens.zh-TW.md) | [`zh-TW`](docs/screens.zh-TW.md) |
| **決策記錄** | [`adr/`](docs/adr/README.md) | [`zh-CN`](docs/adr/README.zh-TW.md) | [`zh-TW`](docs/adr/README.zh-TW.md) |
| **全部索引** | [`docs/README.md`](docs/README.md) | [`zh-CN`](docs/README.zh-TW.md) | [`zh-TW`](docs/README.zh-TW.md) |

---

## 本地運行

```bash
git clone https://github.com/HenryLiu520/nextstop
cd nextstop

./scripts/start-all.sh                  # PostgreSQL 5433 → Spring Boot 8080
cd web && npm install && npm run dev    # http://127.0.0.1:5175
```

`web/` 把 `/api` 代理到 8080，螢幕上這一天是用真實可用性答案拼出來的。

不想構建？開啟 [`product/demo/index.html`](product/demo/index.html)，單檔案，不用服務，能離線，右上角切三種語言。

---

## 路線圖

- 讓規劃器真正填滿一個空檔。
- 把一句話接到那四個動作上。
- 釋出 PWA 與兩個手機殼。

---

<p align="center"><sub>
COMP1110 第 08 組 · 香港大學 · LIU Haoran (u3686264)<br>
名字是拿一篇講 attention 的論文開的玩笑。
</sub></p>
