# 架構

[English](architecture.md) · [簡體中文](architecture.zh-CN.md) · **繁體中文**

NextStop 只回答一個問題：*這個人此刻應該在哪裡，什麼時候該動身。*
回答這個問題的系統分成兩半，兩半的位置從不互換。

伺服器是**資料層**。它掌握課表和教室，回答"哪裡是空的"。
客戶端是**計劃層**。它們決定空閒時間拿來做什麼，把那個決定留住，
日子一變就把它修回來。伺服器不存任何計劃，對計劃也沒有任何意見。

## 1. 概覽

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

## 2. 分界線為什麼在這裡

分界只跟一條判據走：**這個事實是共享的，還是個人的？** 一份計劃屬於一個人，
只活幾個小時。課表由上千人共享，活一個學期，由一個我們控制不了的辦公室寫入。
不同的問題，不同的家。

| 屬性 | 計劃 | 課表 |
|---|---|---|
| 歸屬 | 一個學生 | 學校 |
| 生命週期 | 一天 | 一個學期 |
| 變化頻率 | 每次會話多次編輯 | 釋出後固定 |
| 真值 | 一個提案，永遠可商量 | 終局，從不受質疑 |
| 讀寫 | 本地讀寫 | 我們這邊只讀 |
| 需要離線 | 需要，走廊裡沒有訊號 | 不需要，快取足夠 |

**計算放在客戶端**，因為計劃短命、私人、易變。否則每一次編輯都是一次往返加一行伺服器記錄，
每改一條計劃規則就是一次遷移。修補在 CPU 上便宜，在協調上昂貴：它拿候選停靠點
與一份只有客戶端持有的計劃對比。

**資料留在伺服器**，因為兩個人看同一間教室時，課表不能給出兩個答案。否則兩個不同版本的客戶端
會對"這間教室空不空"產生分歧，而且沒有什麼能裁決這場爭論。只有伺服器這一處，
"2A 在 10:00 到 11:00 被佔用"才只有一個意思。

伺服器比它看起來要小：它回答"我能去這裡嗎"，然後讓開。

## 3. 資料層

Spring Boot 4.1.1 跑在 Java 21 上，連埠 5433 的 PostgreSQL 17。Flyway 管理 schema，
V1 到 V20。服務由 32 個自動化測試覆蓋，這些測試跑在真實資料庫上。

| 方法 | 路徑 | 誰 | 用途 |
|---|---|---|---|
| POST | `/api/auth/login` | 任何人 | 用憑據換一個 token |
| POST | `/api/auth/register` | 任何人 | 建立一個 `user` 帳號 |
| GET | `/api/auth/me` | token | 當前身份和角色 |
| GET | `/api/availability?date=&building=&from=&to=` | token | 某個視窗內空閒的教室 |
| GET | `/api/rooms`, `/api/buildings` | token | 帶屬性的教室，樓棟 |
| GET | `/api/rooms/{code}/timeline` | token | 一間教室一天的佔用區塊 |
| PATCH | `/api/rooms/{code}` | admin 及以上 | 教室後設資料 |
| GET, POST | `/api/rooms/{code}/reviews` | token | 讀或寫一條評價 |
| GET | `/api/reviews/summary` | token | 彙總評分 |
| POST | `/api/updates` | admin 及以上 | 佔用變更，帶原因和到期 |
| GET | `/api/updates`, `/api/updates/mine` | token | 生效中的變更，自己的變更 |
| DELETE | `/api/updates/{id}` | admin 及以上 | 撤回一條變更 |
| GET | `/api/audit` | admin 及以上 | 特權操作的審計日誌 |
| GET, POST, PATCH, DELETE | `/api/accounts` | admin 和 superadmin | 帳號管理 |
| GET | `/api/about` | 任何人 | 構建和服務後設資料 |

角色順序是 `user < admin < superadmin`。這個順序在伺服器上強制一次，
客戶端只是把 token 用不了的東西藏起來。

### 唯一真正的計算

`GET /api/availability` 不是查表。它是：

```
free(room, date) = blocks(room, date) - blocks taken by a class
                 - blocks taken by an active room update
                 - 10 minutes either side of every occupied block
```

**10 分鐘換場餘量。** 一節 11:00 結束的課，就可用性而言，把教室佔到 11:10。
餘量在每個被佔用區塊的兩邊都生效，所以 10:55 開始的查詢會返回空。這個值是伺服器配置，
預設 10，從來不是客戶端設定：兩個客戶端必須對同一間教室給出同一個答案。

**課表永遠贏。** 一條房間更新可以讓一間教室停用，帶原因和到期時間，由管理員釋出。它不能把
教室從一節課裡拿出來。與課程區塊重疊的變更會被拒絕，返回 HTTP 400 和程式碼
`TIMETABLE_PRIORITY`：課表是基準事實，管理員備註只是一個批註。

更新帶原因和到期時間，所以讀起來像關於世界的一個主張，而不是一次永久修改。到期之後那個區塊
就不再計數，因此後面不需要清理任何東西。

### 房間屬性

公共房間資料來自伺服器，各客戶端結合本機個人歷史計算，因此個人排名可以不同。固態分在裝置上用這些數值和 [`room-scoring.md`](room-scoring.md) 裡的權重算出。

```
GET /v1/rooms/attributes
{ "rev": 14,
  "updatedAt": "2026-11-11T09:00:00Z",
  "rooms": [ { "code": "KB227", "quiet": 4, "desk": 3, "sockets": 3,
               "ac": 5, "spaciousness": 4, "daylight": 3 } ] }
```

每間房六個維度，每條是 0 到 5 的整數：`quiet`、`desk`、`sockets`、`ac`、`spaciousness`、`daylight`。0、3、5 三檔的錨點寫在 [`adr/0005-room-attributes-are-scored-on-the-server.zh-TW.md`](adr/0005-room-attributes-are-scored-on-the-server.zh-TW.md)。

**快取語義。** `rev` 就是快取鍵。客戶端把文件連同到達時的修訂號存進 IndexedDB，啟動時比對本地修訂號，服務端報的修訂號不同才重拉。修訂號一致時只有一次條件請求，沒有下載。

**更新節奏。** 每學期做一次全量審計。裝修、換桌子、一層樓加插座條，在記錄變更的當天遞增 `rev`。`updatedAt` 帶日期，客戶端據此判斷本地那份有多舊。

**降級。** 拉不到就用 IndexedDB 裡那份。裝置上也沒有，每個維度按 0.5 中性分算，受影響的卡片標「未核實」。沒有屬性記錄的房間同樣按 0.5 算。

## 4. 計劃層

計劃層是客戶端程式碼。在原型裡它是一個單獨的 HTML 檔案，它背後的規劃器還是個樁。真實的是它
管理的狀態。

一份計劃是一天的停靠點序列，每個停靠點有開始時間、時長、地點和目的。

| 狀態 | 什麼時候進入 | 使用者看到什麼 | 去向 |
|---|---|---|---|
| `planning` | 一天被攤開，或者做了一次編輯 | 這一天是一疊區塊 | `executing` |
| `executing` | 使用者開始跟著計劃走 | 當前停靠點，一次一張卡 | `deviated`, `planning` |
| `deviated` | 現實偏離：一節課拖堂，一個停靠點被跳過 | 一條提示和一個修法 | `repairing`, `planning` |
| `repairing` | 使用者接受那個修法 | 改動停靠點最少的計劃 | `executing`, `planning` |

停靠點是有型別的，型別決定修補有多大自由度：

| 停靠點型別 | 可移動 | 可刪除 | 說明 |
|---|---|---|---|
| `class` | 不可 | 只有使用者能刪 | 一個錨點，從課表複製而來 |
| `study` | 可以，在它的空檔內 | 可以 | 預設的填充物 |
| `meal` | 可以，在一個視窗內 | 不可 | 綁在一個用餐時段上 |
| `meeting` | 可以 | 不可 | 裡面有別人 |
| `custom` | 可以 | 可以 | 手動加的，什麼都能放 |

系統從不碰 `class` 停靠點。跳過一個是用自己的決定，而且可逆。

**為什麼伺服器上不存計劃。** 一張 plans 表需要使用者、版本、同步規則和衝突處理，它還是會輸給
一臺沒儲存就關掉的手機。計劃只有一天長，重建它的成本是一次往返，不需要協調。
沒有端點接受計劃，沒有列持有計劃，停靠點改形狀時也沒有遷移要寫。

應該坐在 `repairing` 狀態背後的最小擾動修補演算法，寫在
[`algorithm.zh-TW.md`](algorithm.zh-TW.md)。它已經設計好，尚未實現。

## 5. 它們怎麼連

契約是 HTTP 上的 REST，JSON body，一個基礎路徑 `/api`。

- **認證。** `POST /api/auth/login` 返回一個 token 和一個角色。兩個客戶端每次呼叫都在
  `Authorization` 頭裡帶上它，字首是 `Bearer` scheme。token 存在記憶體裡，所以重新整理頁面就會
  把使用者登出。
- **查詢形狀。** 可用性總是以一個視窗來問，從來不是整表傾倒。客戶端發一個日期、一棟樓和它
  打算使用的時段，所以答案是一個很短的教室列表。
- **後設資料，不是課表。** 客戶端問*"這間教室現在空嗎"*，不是*"把所有課都給我"*。填一個空檔
  一個請求，修補一次計劃再多一個。沒有任何東西訂閱任何東西。
- **新鮮度。** 客戶端給每個可用性答案打上到達時間。圍繞某個停靠點修補之前，它檢查這個答案
  是否比最後一次計劃編輯更新，不是就重新拉取。建立在過期可用性上的計劃比沒有計劃更糟。
- **失敗與降級。** 資料層不可達時，計劃繼續渲染，因為它活在客戶端裡。停下來的是需要外部
  事實的東西：查詢失敗，計劃被標記為未驗證，修補拒絕把一個停靠點移進未確認的教室。
  客戶端從不為了介面繼續往下走而憑空造一間教室。

| 情況 | 客戶端行為 |
|---|---|
| 401 或 token 過期 | 登出，回到登入頁，什麼都不留 |
| 403，角色太低 | 把那個操作藏起來，不重試 |
| 400 `TIMETABLE_PRIORITY` | 顯示衝突的那節課，計劃保持原樣 |
| 可用性遇到網路錯誤 | 把受影響的停靠點標記為未驗證 |
| 可用性列表為空 | 推薦鄰樓，絕不推薦一間未知的教室 |

除了 token，伺服器上不存任何會話狀態，所以客戶端再問一次就能接著用。

## 6. 兩個客戶端，一份契約

兩個 Vue 3 應用：埠 5173 的桌面客戶端覆蓋全部三個角色，埠 5174 的手機客戶端覆蓋學生檢視。
它們是分開的構建，因為兩套佈局幾乎不相干。底下它們是同一個客戶端的兩種形狀。

| | 桌面，5173 | 手機，5174 |
|---|---|---|
| 角色 | user, admin, superadmin | user |
| 螢幕 | day, plan, rooms, reviews, admin, audit | day, plan, mine |
| API | `/api` | `/api`，同一個 |
| 認證 | 同樣的 token 格式 | 同樣的 token 格式 |
| 規則 | 在伺服器上強制 | 在伺服器上強制 |

**沒有一條規則寫兩遍。** 10 分鐘餘量、課表優先、角色順序和審計寫入都活在 Spring 服務裡。
兩個客戶端都不重實現它們，也都不被信任去實現。一條寫兩遍的規則最終會分叉，而使用者看到的會是
錯的那一版。改餘量只是改一個配置值，兩個客戶端下一個請求就拿到。

它們真正重複的是呈現：同一個答案，一個做成手機卡片，一個做成桌面行。

## 7. 部署形狀

三個程序和一個資料庫，開發時全在一臺機器上：

| 程序 | 埠 | 是什麼 |
|---|---|---|
| PostgreSQL 17 | 5433 | 資料庫，啟動時應用遷移 |
| Spring Boot 4.1.1 | 8080 | 資料層 |
| Vue 3 桌面 | 5173 | 一個 Vite dev server |
| Vue 3 手機 | 5174 | 一個 Vite dev server |

`scripts/start-all.sh` 按那個順序把它們拉起來：資料庫、服務，然後兩個客戶端。
`scripts/test.sh` 跑那 32 個自動化測試。第一次執行會應用 V1 到 V20，花幾分鐘。

Docker 是可選的。`docker-compose.yml` 把同樣的三個程序加資料庫打包起來，給一臺不願意裝
Java 和 Node 的機器用。佈局完全一樣：計劃層以靜態檔案提供，所以它不在乎資料跑在哪裡。

## 交付目標

一次構建，四個容器。`web/` 是現在正在構建的前端。它有一個 `vite.config.js`。
`vite build` 寫出 `dist/`，每個目標都載入同一個目錄。

今天存在的東西比計劃窄。`product/demo/index.html` 是一個單檔案原型，開啟檔案就能跑。
`web/` 還沒產出 `dist/`。倉庫裡沒有 manifest，沒有 service worker，也沒有 Capacitor 專案。
下表就是這麼說的。

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

| 目標 | 容器 | 增加了什麼 | 狀態 |
|---|---|---|---|
| browser | 標籤頁 | 什麼都不加 | 原型今天就能跑 |
| PWA | 標籤頁，已安裝 | manifest + service worker + 離線外殼 | 未構建 |
| Android | Capacitor 外殼 | APK | 未構建 |
| iOS | Capacitor 外殼 | IPA | 未構建 |

### 構建鏈

- 一份 Vite 配置，在 `web/` 裡。`vite build` 寫出 `dist/`。沒有哪個目標拿第二份配置。
- PWA 是一個外掛，不是第二次構建。`vite-plugin-pwa` 在 `vite build` 期間生成 manifest 和
  預快取列表。manifest 和 service worker 都是輸出物，從不手寫，所以進構建的檔案就是快取裡的
  檔案，沒人需要去改一張列表。
- Capacitor 拿 `dist/` 當輸入。`capacitor.config.json` 把 `webDir` 設成 `dist`，
  `npx cap sync` 把那個目錄複製進 `android/` 和 `ios/` 並重新整理原生外掛。這次複製就是把瀏覽器
  構建變成原生應用的那一步。
- 之後簽名包是一條平臺命令，不是第二個前端。`npx cap open android` 加一個 Gradle 構建出
  APK，Xcode archive 出 IPA。
- 後果：`dist/` 是唯一的產物。一個修復只要在那一份目錄裡，就落到四個容器裡，而目標之間的
  差別是一條 CSS 規則或一次能力檢測。

### 一個版本，四個地方

版本必須在四個不同時間構建的地方保持一致。它從一個地方讀取。

- `web/package.json` 持有 `version`，它是倉庫裡唯一的版本字串。
- Vite 用 `define` 在構建時把它注入 bundle，所以跑著的客戶端知道自己是哪一版。它不用請求
  就能到版本頁和診斷資訊裡。
- service worker 的快取名由同一個值構成。新版本就是新快取，舊的會在 activate 時丟掉。
  這就是不讓過期外殼活過一次部署的原因。
- Capacitor 在 `cap sync` 時讀同一個值，用於 Android 的 `versionName` 和 `versionCode`，
  以及 iOS 的 `CFBundleShortVersionString`。沒人往原生專案裡手打版本號。
- `GET /api/about` 本來也要報告它，這樣客戶端能比較自己跑的構建與伺服器編譯時對應的構建。
  還沒接上：`AboutController` 返回 project、course、group、author、uid、signature 和 year，
  沒有 `version` 欄位。
- 後果：升一次版本是一次編輯，比伺服器舊的客戶端是可檢測的，而不是靠猜。

### 安全區

劉海、圓角和底部手勢條壓在 webview 上面。佈局得自己留出內邊距，否則內容的第一行和最後一行
讀不了。

- `web/index.html` 的 viewport meta 裡已經帶了 `viewport-fit=cover`。這是前提：沒有它，
  每個 `env(safe-area-inset-*)` 都解析成零，內邊距靜默地什麼都不做。
- 內邊距只加在佈局碰到螢幕邊緣的地方，也只在那些地方加。頂欄拿
  `env(safe-area-inset-top)`。底部導航和每個 sheet 拿 `env(safe-area-inset-bottom)`。
  `web/src/components/BottomSheet.vue` 今天已經有底部那個。
- 左右內邊距在橫屏時有用。它們是同一批容器上的 padding，不是另一套佈局。
- 這些值在使用處由 CSS 讀取，所以新的邊緣元素自己留內邊距，而不是繼承一個固定數字。
  寫死的 `padding-bottom: 34px` 到下一臺裝置上就是錯的。
- 同一條規則覆蓋外殼和瀏覽器。在 Capacitor 外殼裡 webview 預設全屏，所以安全區是狀態列和
  第一行文字之間唯一的東西。

### 兩個瀏覽器行為

兩條都是瀏覽器事實，所以在標籤頁裡和在外殼裡都成立，它們跑同一個引擎。

- **回彈。** `overscroll-behavior: contain` 阻止頁面越過末端彈動，也阻止下拉重新整理在一天當中
  過載計劃。已經在 `web/src/styles/base.css` 和 `web/src/App.vue` 裡設好，且設在滾動容器上
  而不是 `body` 上。
- **鍵盤。** 在 iOS 上，螢幕鍵盤不改變佈局視口的大小。佈局相信自己還有整個高度，被聚焦的
  輸入框就落在鍵盤後面。修法是 `window.visualViewport`：監聽 `resize` 和 `scroll`，讀
  `height` 和 `offsetTop`，把聚焦的欄位滾進那個矩形。還沒寫。

### 離線覆蓋什麼，不覆蓋什麼

離線是從計劃活在客戶端這件事繼承來的（[§4](#4-the-plan-layer)），不是來自一個單獨的離線功能。
service worker 加的是一個外殼，不是一個資料儲存。

- 快取：外殼。沒有網路也能開啟應用並渲染。
- 快取：每個查詢最後一次的可用性答案，連同拉取時間一起留著。
- 不快取，因為不可能知道：快取答案之後釋出的房間更新、辦公室調走的課、別人寫的評價。
  走廊裡沒有訊號，所以那裡的答案就是最後一次拉取的那個。
- 客戶端會說清是哪種。快取的答案帶著它的拉取時間提供，依賴它的停靠點被標記為未驗證，
  依照 [§5](#5-how-they-connect)。快取的答案從不打扮成當前的答案。
- 新計劃不在離線時構建。修補只能把一個停靠點移進新答案確認過的教室。計劃兩種情況都繼續渲染，
  因為它是本地的。
- 寫入失敗。沒有網路時發一條評價會返回錯誤，不入隊。沒有後臺同步，也沒有 outbox，因為計劃是
  唯一必須活下來的狀態，而它從不離開裝置。

### 為什麼用 Capacitor 而不是手寫 WebView

介面很普通：卡片、列表、時間線、一個表單。裡面沒有需要原生繪製的東西。
所以原生層應該儘量小。

- Capacitor 恰好提供容器、應用圖示、包名、簽名路徑和一條通往原生呼叫的窄橋。它不提供任何
  介面，也不指望有。
- 它不重寫介面。四個容器跑同樣的元件、同樣的 CSS 和同一份計劃規則。沒有哪個目標有自己的螢幕。
- 另一條路，在 Android 上手寫一個 `WebView` activity，在 iOS 上手寫一個 `WKWebView`
  controller，產出的應用一樣。它還會產出檔案複製、資源載入器、橋和版本管道，寫兩遍，作為
  我們要維護的程式碼。
- 橋故意保持窄：狀態列顏色、返回鍵處理。每個外掛都是各平臺的原生程式碼，所以一個新的原生能力
  就是一小段寫兩遍的程式碼，即使介面只寫一遍。

這個決定和它的代價記在
[`adr/0004-one-ui-four-targets.zh-TW.md`](adr/0004-one-ui-four-targets.zh-TW.md)。

## 8. 還沒構建的東西

說明白，免得上面任何一句被讀成斷言：

- **服務端規劃。** 完全沒有：沒有端點接受或返回計劃。
- **多使用者同步。** 裝置之間不共享計劃，所以在桌面做的計劃不在手機上。
- **修補演算法。** 原型規劃器是個樁。[`algorithm.zh-TW.md`](algorithm.zh-TW.md) 裡的最小擾動
  演算法已經寫成規格，沒有實現，所以原型裡"動一塊還是動五塊"的行為是示意，不是結果。
- **真實的學校源。** `mock-school-api` 模擬課表，`V2__seed.sql` 播種一份像樣的課表。
  服務每三十分鐘從這個介面拉一次，落進 `school_slots`，再由它重建 `class_slots`。學校的端點沒有接。
- **生產級認證。** token 在記憶體裡，加鹽 SHA-256 密碼，沒有 HTTPS。原型級別。

這裡描述的分裂是真實且跑著的。坐在計劃層上面的東西，以及真實的課表源會是什麼樣子，
是接下來的幾塊。
