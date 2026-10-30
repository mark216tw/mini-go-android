# 開發紀錄

最新紀錄放最上面。每次開發工作都要更新，不要事後補寫。

---

## 2026-10-30 - v1.0.0-prerelease（versionCode 12）

### 目標

建立完整專案文件、加入 MIT License、發布公開 GitHub 儲存庫，並建置使用 R8 與 Debug 簽章的 `app-prerelease.apk`，上傳為 GitHub Pre-release。

### 變更

- 新增 `LICENSE`（MIT License，Copyright 2026 mark216tw）。
- 改寫 `README.md`：專案說明、功能、安裝、建置、文件索引、隱私與授權。
- 新增 `docs/USAGE.md`：使用指南與常見問題。
- 新增 `docs/ARCHITECTURE.md`：技術選型、分層、規則引擎、AI、存檔、導覽、建置與測試。
- 新增 `docs/DESIGN.md`：產品定位、使用情境、畫面流程、狀態機、功能規格與後續方向。
- 新增 `docs/BUILD_AND_RELEASE.md`：Debug／Prerelease／Release 建置、驗證與 GitHub Release 流程。
- 新增 `docs/PRIVACY.md`：離線、資料儲存、權限與系統備份說明。
- 更新 `RELEASE_NOTES.md` 與 `CHANGELOG.md`，標明此版為 Pre-release。
- 版本依發行需求設為 `1.0.0`（versionCode 12），prerelease 輸出為 `1.0.0-prerelease`。
- 建置 `assemblePrerelease`，確認 R8、資源縮減與 Debug 簽章。

### 問題與解法

- 先前開發版本為 1.2.1，但本次發行需求指定 `1.0.0-prerelease`；因此將 defaultConfig 設為 1.0.0、versionCode 12，並在 CHANGELOG 說明版號差異。
- `prerelease` 使用 release 的 minify 與 shrink 設定，R8 首次建置需確認 kotlinx.serialization 與 Navigation 型別安全路由的 keep rule；建置成功並產出 mapping 檔。
- `stripPrereleaseDebugSymbols` 對部分 AndroidX native 函式庫顯示無法 strip 的提示，但不影響 APK 產出與簽章。

### 待辦／已知限制

- 本機仍無連接裝置與模擬器映像，尚未做實機操作驗證。
- Pre-release 使用 Debug 金鑰簽署，不可作為正式上架版本。
- 既有功能限制不變（無 19 路、無線上對戰、死子需手動標記、悔棋最多 80 手、AI 非職棋等級）。

---

## 2026-10-30 - v1.2.1（versionCode 11）

### 目標

整理主畫面：將設定與關於收進右上角「更多」選單；縮小規則教學頁的示意棋盤。

### 變更

- `HomeScreen.kt`：TopAppBar 新增 `MoreVert` 選單，內含設定與關於；移除主畫面的設定／關於卡片。
- `strings.xml`：移除不再使用的 `home_settings`、`home_settings_desc`、`home_about`、`home_about_desc`。
- `TutorialScreen.kt`：示意棋盤改為最多 78% 寬度、最大 280dp，並置中顯示。
- 版本更新為 1.2.1（versionCode 11）。

### 問題與解法

- 主畫面原本使用單純的 TopAppBar，沒有 actions；補上 `MoreVert`、`DropdownMenu` 與選單項目。
- 教學頁的棋盤使用 `fillMaxWidth`，在平板上過大；以 `fillMaxWidth(0.78f)` 加 `widthIn(max = 280.dp)` 限制並置中。

### 待辦／已知限制

- 本機仍無連接裝置與模擬器映像，尚未做實機操作驗證。
- 既有已知限制不變（無 19 路、無線上對戰、死子需手動標記、悔棋最多 80 手、AI 非職棋等級）。

---

## 2026-10-30 - v1.2.0（versionCode 10）

### 目標

執行介面與功能批次 A＋B：對局選單、座標提示、計分明細、終局檢視、電腦虛手提示、預設設定、續玩管理、快速開始與首次教學提示；並擴充規則教學（棋盤、虛手、打吃、勝負判定）。

### 變更

- `GameScreen.kt`：更多選單新增重新開始與規則教學；提示／最後一手顯示座標；計分新增清除標記、子地明細與說明對話框；終局結果可關閉查看棋盤並可重新開啟；電腦虛手提示。
- `GameViewModel.kt`：新增 `clearDeadMarks()` 與 `AiPassed` 事件。
- `ui/common/GoCoordinates.kt`：新增座標轉換（跳過 I，從左下起算）。
- `HomeViewModel.kt`／`HomeScreen.kt`：續玩卡顯示模式與難度、可捨棄存檔；新增快速開始與首次教學提示；快速開始若已有存檔會先確認。
- `MiniGoApp.kt`／`MainActivity.kt`：串接快速開始、教學導覽與「已看過教學」狀態。
- `SettingsRepository.kt`／`AppSettings`：新增 `hasSeenTutorial`。
- `SettingsViewModel.kt`／`SettingsScreen.kt`：新增預設模式與預設執子設定。
- `TutorialScreen.kt`／`strings.xml`：教學重整為 8 頁，新增棋盤、虛手、打吃、勝負判定內容。
- 新增座標轉換測試，單元測試共 23 項。
- 版本更新為 1.2.0（versionCode 10）。

### 問題與解法

- 首頁新增快速開始時，若已有續玩存檔會直接覆蓋；加入二次確認。
- 結果對話框原本不可關閉，玩家無法檢視終局盤面；改為可關閉並以「查看結果」重新開啟。
- 教學頁面重整後移除舊的 `tut_1`～`tut_5` 字串，改為語意化 key，避免重複與頁面順序混亂。
- 首頁插入新對話框時曾少一個 `if` 結尾大括號，建置前檢查並修正。

### 待辦／已知限制

- 本機仍無連接裝置與模擬器映像，尚未做實機與橫向版面操作驗證。
- 音效、落子二次確認與計分領地視覺化尚未實作。
- 既有已知限制不變（無 19 路、無線上對戰、死子需手動標記、悔棋最多 80 手、AI 非職棋等級）。

---

## 2026-10-30 - v1.1.0（versionCode 9）

### 目標

執行技術優化第 1～6 項：AI 可控性與時間預算、模擬落子不記錄歷史、存檔可靠性、棋盤自適應、Compose 重算優化，以及資料存取與導覽架構整理。

### 變更

- `ai/GoAi.kt`：新增 `timeBudgetMillis` 與 `shouldStop`；9 路 700ms、13 路 1.5s；中級前瞻會依時間/取消狀態提早結束。
- `engine/GameEngine.kt`：`play()` 新增 `recordHistory` 參數；`legalMoves()` 與 AI 模擬不再附加歷史；undo 歷史上限 80 手。
- `data/GameRepository.kt`：新增 `GameStore` 介面；改用 `AtomicFile`；`SavedGame` 增加 `version`；載入時可由 AtomicFile 還原備份。
- `ui/game/GameViewModel.kt`：改為一般 `ViewModel` 並注入 `GameStore`；新增提示計算中狀態、AI/提示可取消、存檔失敗事件；取消 AI 與提示時同步清理；離開對局或結束對局前先等待待寫入存檔，避免進度遺失或結束後被舊存檔寫回。
- `ui/game/GameScreen.kt`：棋盤尺寸依可用高度自適應；計分結果與死子集合以 `remember` 快取；修正續玩載入時多震一次的問題；顯示提示計算中與存檔失敗訊息。
- `ui/home/HomeViewModel.kt`：新增首頁 ViewModel，將存檔讀取移出 UI。
- `data/GameConfig.kt`：新增 `sessionId` 與 `withNewSession()`，新局不再需要先清除舊存檔。
- `ui/Destinations.kt`、`ui/MiniGoApp.kt`：改用 Navigation Compose 型別安全路由；建立 composition root 注入 `GameRepository`。
- 單元測試新增至 21 項（模擬不記歷史、undo 上限、AI 時間預算/取消、存檔 round-trip、導覽設定轉換）。
- 版本更新為 1.1.0（versionCode 9）。

### 問題與解法

- `GoAi.chooseMove` 的時間參數放在 lambda 前，trailing lambda 被解析成 `Long`；將 `shouldStop` 移至最後一個參數。
- `SavedGame` 加入 `version` 後，舊的位置參數建構順序錯誤；改用具名參數。
- 型別安全導覽若直接巢狀 `GameConfig` 需要額外 NavType；改用扁平 `GameDestination`（Int/String）再轉成 `GameConfig`。
- `AtomicFile` 載入時若只檢查 baseFile，可能忽略可還原的 `.bak`；改為直接 `readFully()` 並以 `runCatching` 處理不存在／損毀。
- 新局原先在 Setup 直接清除存檔，資料邏輯散落在 UI；改由 `sessionId` 判斷並將存取集中到 ViewModel/Repository。

### 待辦／已知限制

- 本機仍無連接裝置與模擬器映像，尚未做實機操作與橫向版面驗證。
- 悔棋最多回溯最近 80 手；重新啟動 App 後仍保留這 80 手。
- AI 為啟發式引擎，時間上限只確保反應時間，不代表棋力提升。
- 既有已知限制不變（無 19 路、無線上對戰、死子需手動標記、AI 非職棋等級）。

---

## 2026-10-30 - v1.0.1（versionCode 8）

### 目標

在下棋畫面右上角新增「更多」選單，提供「結束對弈」功能，讓玩家可以強制結束目前棋局並直接回到首頁。

### 變更

- `res/values/strings.xml`：新增更多選單與結束對弈相關文案。
- `ui/common/Components.kt`：`BackTopBar` 支援 `actions` 插槽，其他畫面不受影響。
- `ui/game/GameScreen.kt`：新增 `MoreVert` 圖示、下拉選單、結束對弈確認框與 `Ended` 事件處理；返回鍵會優先關閉選單／對話框。
- `ui/game/GameViewModel.kt`：新增 `endGame()`，取消 AI 與待寫入存檔，清除本局存檔後發出 `GameEvent.Ended`。
- `ui/MiniGoApp.kt`：串接 `onEndGame`，結束後回首頁。
- 版本更新為 1.0.1（versionCode 8）。

### 問題與解法

- 若只導回首頁，debounce 中的存檔可能在清除後把棋局寫回；因此在 `endGame()` 先取消 `saveJob`，清除期間顯示載入中，完成後才導頁。
- AI 思考中若直接離開，背景運算可能套用落子；先取消 `aiJob` 再結束。
- 多個對話框與下拉選單同時存在時，返回鍵需依序關閉；`BackHandler` 已加入對應優先順序。

### 待辦／已知限制

- 本機仍無連接裝置與模擬器映像，尚未做實機操作驗證。
- 「結束對弈」不顯示勝負，也不進入計分；需要結果時請使用虛手計分或認輸。
- 其他既有已知限制不變（無 19 路、無線上對戰、死子需手動標記、AI 非職棋等級）。

---

## 2026-10-30 - v1.0.0（versionCode 7）

### 目標

從零建立「mini圍棋」Android App，完成 M0–M6：專案骨架、規則引擎、棋盤 UI、AI、計分與輔助、規則教學與設定，並通過建置、測試與 lint。

### 變更

- **M0 專案骨架**：Gradle Kotlin DSL、Version Catalog、AGP 8.13.0／Kotlin 2.2.20、Compose BOM 2025.02.00、Material 3 主題、Navigation Compose、Edge-to-edge、自製圍棋圖示。
- **M1 規則引擎**：`engine/` 的 `Board`、`GameState`、`GameEngine`、`Scoring`；支援提子、自殺手、簡易劫、虛手、悔棋快照、中國數子法與死子移除。
- **M2 對弈流程**：`GameViewModel` + `StateFlow`、對弈設定頁、Canvas 棋盤（星位、座標、最後一手、提示、死子標記）、雙人對弈。
- **M3 電腦 AI**：`GoAi` 三難度（入門／初級／中級）、真眼過濾、提子與叫吃評估、中級一手前瞻；提示功能重用 AI 評估。
- **M4 計分與續玩**：連續虛手進入計分、手動標記死子、即時分數預覽、結果對話框、認輸、悔棋、對局 JSON 暫存、程序重建還原與首頁續玩。
- **M5 學習與設定**：5 頁圖文規則教學、DataStore 設定（主題／動態配色／座標／震動／預設棋盤與難度）、關於頁（版本、授權、隱私）。
- **M6 品質**：字串集中於 `strings.xml`、系統列圖示依主題調整、棋盤座標留白與按鈕尺寸調整、計分狀態顯示、單元測試 16 項（含存檔 JSON round-trip）、`assembleDebug`、`testDebugUnitTest`、`lintDebug` 通過；新增 README、CHANGELOG、RELEASE_NOTES。

### 問題與解法

- Gradle 第一次建置因工具端程序中斷，但實際已完成；重新執行後為 UP-TO-DATE。
- 自殺手測試原先誤用 `X`（黑棋）包圍中央，導致落子合法；改用 `O`（白棋），並把「合法手」測試改成 5×5 棋盤，避免所有空點皆為自殺手。
- 程序被系統回收後重建對局頁時，原先會直接開新局並覆蓋存檔；改為 `GameViewModel` 初始化時比對存檔設定並還原，且從設定頁開始新局前先清除舊存檔。
- AI 在只剩填自己真眼的合法手時原本會隨機落子；改為直接虛手，避免破壞自己的地盤。
- lint 回報 `local.properties` 的 Windows 磁碟機路徑未轉義；改為 `C\:/...`。
- 把 adaptive icon 從 `mipmap-anydpi-v26` 移到 `mipmap-anydpi` 後 AAPT 找不到資源；還原為 `-v26`，接受 lint 的 `ObsoleteSdkInt` 警告。
- PowerShell 5.1 讀寫 UTF-8 字串檔造成亂碼；改用編輯工具重寫 `strings.xml`，後續不再用 shell 修改含中文檔案。

### 待辦／已知限制

- 本機無連接裝置與模擬器映像，尚未做實機／模擬器 UI 操作驗證。
- AI 為啟發式引擎，中級棋力仍屬休閒練習等級。
- 僅支援 9 路與 13 路，未提供 19 路。
- 死子需玩家手動標記；尚無自動死活判斷。
- 尚未提供線上對戰、帳號或雲端同步。
