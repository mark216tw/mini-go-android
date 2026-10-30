# 建置與發行指南

本文件說明 mini圍棋的開發建置、Pre-release APK 與正式發行流程。

## 1. 環境需求

- JDK 17
- Android SDK Platform 36
- Android SDK Build-Tools 36.0.0
- Gradle Wrapper（專案已包含，不需另外安裝 Gradle）

Windows 範例：

```powershell
java -version
adb version
```

## 2. Build Type

專案在 `app/build.gradle.kts` 定義三種 Build Type：

| Build Type | R8 | 資源縮減 | 簽章 | 版本後綴 |
| --- | --- | --- | --- | --- |
| `debug` | 否 | 否 | Debug | `-debug` |
| `prerelease` | 是 | 是 | Debug | `-prerelease` |
| `release` | 是 | 是 | 需設定正式簽章 | 無 |

### prerelease

```kotlin
create("prerelease") {
    initWith(getByName("release"))
    isMinifyEnabled = true
    isShrinkResources = true
    signingConfig = signingConfigs.getByName("debug")
    versionNameSuffix = "-prerelease"
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
    )
}
```

用途：

- 測試 R8 壓縮與資源縮減。
- 使用 Debug 金鑰簽署，方便直接安裝。
- 可上傳到 GitHub Pre-release。
- **不可作為 Google Play 正式上架版本。**

## 3. 建置指令

```powershell
# Debug
.\gradlew.bat assembleDebug

# 單元測試
.\gradlew.bat testDebugUnitTest

# Lint
.\gradlew.bat lintDebug

# Pre-release（R8 + Debug 簽章）
.\gradlew.bat assemblePrerelease
```

輸出位置：

| 版本 | 路徑 |
| --- | --- |
| Debug | `app/build/outputs/apk/debug/app-debug.apk` |
| Pre-release | `app/build/outputs/apk/prerelease/app-prerelease.apk` |
| Release | `app/build/outputs/apk/release/app-release.apk` |

## 4. 版本規則

- `versionCode`：每次功能變更遞增。
- `versionName`：`主.次.修`。
- Build Type 會自動加上後綴：
  - Debug：`1.0.0-debug`
  - Pre-release：`1.0.0-prerelease`
  - Release：`1.0.0`

本次 GitHub Pre-release 依發行需求使用：

```text
versionCode = 12
versionName = "1.0.0"
prerelease APK versionName = "1.0.0-prerelease"
```

## 5. 驗證 Pre-release APK

### 版本資訊

```powershell
$aapt = "$env:ANDROID_HOME\build-tools\36.0.0\aapt2.exe"
& $aapt dump badging app\build\outputs\apk\prerelease\app-prerelease.apk
```

確認：

- `package name='com.vibecode.minigo'`
- `versionCode='12'`
- `versionName='1.0.0-prerelease'`
- `minSdkVersion:'26'`
- `targetSdkVersion:'36'`

### 簽章

```powershell
$apksigner = "$env:ANDROID_HOME\build-tools\36.0.0\apksigner.bat"
& $apksigner verify --verbose --print-certs app\build\outputs\apk\prerelease\app-prerelease.apk
```

應顯示 Android Debug 憑證。這代表 APK 可安裝測試，但不具正式發行簽章。

### R8

確認 mapping 檔存在：

```text
app/build/outputs/mapping/prerelease/mapping.txt
```

## 6. 安裝測試

```powershell
adb install -r app\build\outputs\apk\prerelease\app-prerelease.apk
adb shell am start -n com.vibecode.minigo/.MainActivity
```

若裝置已安裝相同 applicationId 的其他簽章版本，需先解除安裝。

## 7. GitHub Release 流程

本次發行資訊：

- Repository：<https://github.com/mark216tw/mini-go-android>
- Tag：`v1.0.0-prerelease`
- Release 類型：Pre-release
- 資產：`app-prerelease.apk`

步驟：

1. 確認工作目錄乾淨並已提交。
2. 建立 Git tag。
3. 上傳 APK。
4. 將 Release 標記為 Pre-release。
5. 在 Release Notes 明確標示「Pre-release 測試版本，非正式上線版本」。

範例：

```powershell
git tag -a v1.0.0-prerelease -m "mini圍棋 1.0.0-prerelease"
git push origin v1.0.0-prerelease

gh release create v1.0.0-prerelease `
  app/build/outputs/apk/prerelease/app-prerelease.apk `
  --repo mark216tw/mini-go-android `
  --title "mini圍棋 1.0.0-prerelease" `
  --notes-file RELEASE_NOTES.md `
  --prerelease `
  --latest=false
```

## 8. 正式發行前檢查

正式上架前必須另外處理：

- 建立專用 release keystore，不可使用 Debug 金鑰。
- 設定 `signingConfigs.release` 並妥善保管金鑰。
- 確認 `applicationId` 不再變更。
- 檢查隱私政策與商店說明。
- 在多尺寸裝置與不同 Android 版本測試。
- 考慮 Play App Signing。
- 重新檢視 R8 keep rules 與 mapping 檔保存。

## 9. 注意事項

- `local.properties`、keystore、`.gradle/`、`build/` 不可提交。
- Release APK 不應提交到 Git 歷史；應作為 GitHub Release 資產。
- Pre-release APK 使用 Debug 簽章，任何取得檔案的人都能重新簽署，不適合正式散布。
