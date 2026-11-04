# NextStop · web

纸张风格的「一天」界面。**一套代码同时跑手机和桌面**：手机宽度铺满全屏，桌面宽度在页面中央画一个 390×844 的手机框（圆角 34px、投影）。只有手机框有阴影，其余全部无阴影。

```
web/
├─ index.html
├─ vite.config.js        # 端口 5175（strictPort），/api → localhost:8080
└─ src/
   ├─ main.js
   ├─ App.vue            # 舞台 / 手机框 / 顶栏 / 三个 tab 的容器
   ├─ store.js           # 全部状态与动作（sheet、toast、换一个、保存）
   ├─ api/index.js       # ← 唯一的数据注入点
   ├─ i18n/index.js      # EN / 简 / 繁
   ├─ styles/tokens.css  # 设计令牌
   ├─ styles/base.css    # 重置 + .btn 两种样式 + 数字
   ├─ components/
   │  ├─ BottomNav.vue   # 56 高，1px 上边框，选中 = 字下一个 4px 朱红点
   │  ├─ LangSwitch.vue  # 右上角 EN / 简 / 繁
   │  ├─ DayBlock.vue    # 一块：4px 类型竖条 + 时刻 + 类型·地点
   │  ├─ GapRow.vue      # 缝隙：细虚线 +「走 5 分钟」
   │  ├─ BottomSheet.vue # 底部 sheet（遮罩 + 顶部圆角 18）
   │  ├─ ToastBar.vue    # 圆角 12 + 撤回链接
   │  └─ WeekStrip.vue   # 一周五列，小横杠 = 忙碌度
   └─ views/
      ├─ DayView.vue     # 一天
      ├─ PlanView.vue    # 规划
      └─ MeView.vue      # 我的
```

## 跑起来

```bash
cd web
npm install
npm run dev      # http://127.0.0.1:5175
npm run build    # 产物在 dist/
```

`/api` 代理到 `http://localhost:8080`（后端不在也能跑：现在数据是前端写死的）。

## 接真接口

组件只认 `src/api/index.js` 导出的形状，不认 HTTP。把 `fetchDay()` 里那行注释换成 `fetch` 即可，**组件一行都不用改**：

```js
export async function fetchDay(date) {
  return (await fetch(`/api/day?date=${date}`)).json()
}
```

返回结构写在文件头的注释里：`{ date, weekday, month, day, now, stops[] }`，
每个 stop 是 `{ id, type, start, end, title, place, state, walkBefore, booked, pending }`。

## 设计令牌

| | |
|---|---|
| 底色 / 块 | `#FAF7F2` / `#FFFFFF` + 1px `#E2DCCF` |
| 主文字 / 次文字 | `#1C1A17` / `#6B655C` |
| 强调 | `#C8452D`（一屏只出现一次：当前块的点、导航的点） |
| 类型竖条 | 课 `#1C1A17` · 自习 `#2F5D9E` · 吃饭 `#B4763A` · 见面 `#6A4C93` |
| 字 | 中文 Songti SC，英文 Georgia，数字 tabular-nums lining |
| 圆角 | 块 12 · 圆钮 999 · sheet 18 |

`Georgia` 只有旧式数字（`0` 只有 x-height 高），排时刻会高低不齐。`base.css` 里用
`@font-face` + `unicode-range: U+0030-0039` 让**只有阿拉伯数字**走 Times，字母仍是 Georgia。
