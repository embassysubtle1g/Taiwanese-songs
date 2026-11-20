# 奶奶音樂 Android App

原生 Android App，使用 Kotlin + Jetpack Compose，與網站共用 Google Apps Script API。

## 功能（第一階段）

- 從 Apps Script 載入歌手列表
- 點選歌手後顯示歌曲列表
- 點選歌曲後交由 YouTube App 或瀏覽器開啟
- 顯示載入中與錯誤重試狀態

## 專案資訊

- Application ID：`com.embassysubtle1g.taiwanesesongs`
- Minimum SDK：26
- Compile / Target SDK：36
- Kotlin + Jetpack Compose
- Apps Script API：與 `frontend/` 使用相同部署網址

## 開發

使用 Android Studio 開啟本目錄（`android/`）即可載入 Gradle 專案。
目前尚未加入 Gradle Wrapper；第一次建置前需使用 Android Studio 安裝相容的 Gradle 與 Android SDK。

## 目錄

```text
android/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/embassysubtle1g/taiwanesesongs/
│           └── MainActivity.kt
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── gradle.properties
└── settings.gradle.kts
```
