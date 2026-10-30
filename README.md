# mini圍棋

mini圍棋是一款以 Kotlin、Jetpack Compose 與 Material 3 開發的離線 Android 圍棋 App，提供 9 路與 13 路棋盤、電腦對弈、雙人對弈、規則教學與中國規則計分，適合初學與中階玩家練習。

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-green)
![Language](https://img.shields.io/badge/Language-Kotlin-blue)

> **Pre-release 提醒**：本專案目前的 GitHub Release 為 `v1.0.0-prerelease`，是使用 Debug 金鑰簽署的測試版本，**不是正式上線版本**，也不建議直接上架商店。

## 專案狀態

- 本次 GitHub Pre-release：`1.0.0-prerelease`（versionCode 12）
- 建置型態：`prerelease`
- 啟用 R8 程式壓縮與資源縮減
- 使用 Debug 金鑰簽署，僅供測試安裝
- 最新 Release：<https://github.com/mark216tw/mini-go-android/releases/tag/v1.0.0-prerelease>

## 功能

- **9 路／13 路棋盤**：預設 9 路，適合入門。
- **電腦對弈**：入門、初級、中級三種啟發式 AI 難度。
- **雙人對弈**：同一台裝置輪流落子。
- **對弈輔助**：悔棋、提示（顯示建議座標）、最後一手座標、非法手原因說明。
- **對局選單**：重新開始、規則教學、結束對弈。
- **完整規則**：提子、自殺手禁入、簡易劫、虛手。
- **中國規則計分**：數子法、白棋貼 6.5 目、手動標記死子、子／地明細。
- **終局檢視**：可關閉結果對話框查看終局棋盤，再重新開啟結果。
- **規則教學**：棋盤、氣、提子、打吃、禁入點、打劫、虛手、勝負判定，共 8 頁。
- **續玩與快速開始**：自動保留目前對局；可用預設設定一鍵開局。
- **設定**：主題、動態配色、座標、震動、預設模式／執子／棋盤／難度。
- **完全離線**：不需要網路權限，不含帳號、廣告或追蹤功能。

## 下載與安裝

1. 到 [GitHub Releases](https://github.com/mark216tw/mini-go-android/releases) 下載 `app-prerelease.apk`。
2. 在 Android 裝置上允許安裝未知來源 App。
3. 安裝後開啟「mini圍棋」。

也可使用 ADB：

```powershell
adb install -r app-prerelease.apk
adb shell am start -n com.vibecode.minigo/.MainActivity
```

系統需求：Android 8.0（API 26）以上。

## 建置

需求：

- JDK 17
- Android SDK Platform 36
- Windows / macOS / Linux 皆可

```powershell
# Debug 版本
.\gradlew.bat assembleDebug

# 單元測試
.\gradlew.bat testDebugUnitTest

# 靜態檢查
.\gradlew.bat lintDebug

# Pre-release 測試版（R8 + Debug 金鑰簽署）
.\gradlew.bat assemblePrerelease
```

輸出：

- Debug：`app/build/outputs/apk/debug/app-debug.apk`
- Pre-release：`app/build/outputs/apk/prerelease/app-prerelease.apk`
- Release：`app/build/outputs/apk/release/app-release.apk`（需自行設定正式簽章）

> `prerelease` 會啟用 R8 與資源縮減，但簽章使用 Debug 金鑰，**不可作為正式上架版本**。

## 專案結構

```text
app/src/main/java/com/vibecode/minigo/
  engine/     # 棋盤、規則、劫、虛手、計分（純 Kotlin）
  ai/         # 啟發式 AI 與難度設定
  data/       # DataStore 設定、AtomicFile 對局存檔
  ui/
    common/   # 共用 TopAppBar、ActionCard、座標工具
    game/     # 對弈畫面、棋盤 Canvas、GameViewModel、結果對話框
    home/     # 首頁與 HomeViewModel
    setup/    # 對弈設定
    settings/ # 設定畫面
    tutorial/ # 規則教學
    about/    # 關於
    theme/    # Material 3 主題與顏色
app/src/test/  # JVM 單元測試（規則、計分、AI、存檔、座標、導覽設定）
docs/          # 使用、架構、設計、建置發行、隱私與開發紀錄
```

## 文件

- [使用指南](docs/USAGE.md)
- [系統架構與技術文件](docs/ARCHITECTURE.md)
- [系統設計文件](docs/DESIGN.md)
- [建置與發行指南](docs/BUILD_AND_RELEASE.md)
- [隱私說明](docs/PRIVACY.md)
- [開發紀錄](docs/DEV_LOG.md)
- [版本異動](CHANGELOG.md)
- [Release Notes](RELEASE_NOTES.md)

## 規則實作

- 棋盤：9 路或 13 路。
- 提子：棋組沒有氣時從盤面移除。
- 禁入：自殺手不合法；能提子的落子不受此限。
- 打劫：採簡易劫，禁止立即回提。
- 結束：雙方連續虛手後進入計分。
- 計分：中國數子法，`己方棋子 + 圍空`；白棋加 6.5 目。
- 死子：由玩家在計分畫面手動標記。

## 隱私

本 App 完全離線運作，不需要網路權限，不收集、不傳送個人資料。詳細說明請見 [隱私說明](docs/PRIVACY.md)。

## 已知限制

- AI 為啟發式規則引擎，不是職棋等級。
- 僅支援 9 路與 13 路，未提供 19 路。
- 無線上對戰、帳號或雲端同步。
- 死子需由玩家手動標記。
- 悔棋最多回溯最近 80 手。
- `prerelease` APK 使用 Debug 金鑰簽署，僅供測試。

## 授權

本專案採用 [MIT License](LICENSE)。

Copyright (c) 2026 mark216tw
