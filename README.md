# Android Markdown 編輯/瀏覽器

這是一款簡易的 Android Markdown 編輯器應用程式，可讓使用者編輯 Markdown 文件並即時預覽渲染結果。

## 功能特色

- **雙分頁介面**：
    - **編輯**：提供一個文字輸入區域，用於撰寫或貼上 Markdown 內容。
    - **預覽**：即時顯示編輯區 Markdown 內容的渲染結果，此區域不可編輯。
- **Markdown 渲染**：使用 [Markwon](https://github.com/noties/Markwon) 函式庫進行 Markdown 到 HTML 的轉換與顯示，支援常見的 Markdown 語法，包括：
    - 標題 (H1-H6)
    - 粗體與斜體
    - 清單 (有序與無序)
    - 連結
    - 圖片 (需要網路連線以載入外部圖片)
    - 表格
    - HTML 標籤
- **檔案操作**：
    - **匯入檔案**：可以從裝置儲存空間匯入 `.md` 或 `.markdown` 檔案到編輯區。
    - **儲存檔案**：
        - 編輯區內容有異動時，右下角會出現懸浮儲存按鈕。
        - 點擊儲存按鈕後，允許使用者選擇儲存位置並自訂檔名。
        - 預設檔名為 `untitled.md`，並會自動確保副檔名為 `.md`。
    - **開啟方式**：可以設定為裝置上 `.md` 和 `.markdown` 檔案的預設開啟應用程式。
- **使用者介面**：
    - 明亮、低飽和的 UI 風格。
    - 頂部不顯示應用程式名稱，以提供更沉浸的編輯體驗。
    - 支援日間模式與夜間模式。
- **應用程式圖示**：使用專屬的 Markdown 相關圖示。

## 技術棧

- **程式語言**：Kotlin
- **架構**：MVVM (Model-View-ViewModel) 
    - 使用 `ViewModel` 和 `LiveData` 在 Fragment 之間共享資料。
- **UI 元件**：
    - `ViewPager2` 與 `TabLayout`：用於實現「編輯」和「預覽」分頁。
    - `Fragment`：模組化 UI 元件。
    - `FloatingActionButton`：用於儲存按鈕。
    - `Material Components`：提供現代化的 UI 元素與風格。
- **Markdown 處理**：`io.noties.markwon`
- **非同步處理**：`ActivityResultContracts` 用於處理檔案選擇和儲存的結果。

## 如何建置與執行

1.  **取得原始碼**：複製此專案到您的本機。
2.  **開啟專案**：使用 Android Studio 開啟專案。
3.  **建置專案**：Android Studio 應會自動下載必要的依賴項並建置專案。
    - 若有需要，可手動執行 Gradle 同步 (File > Sync Project with Gradle Files)。
4.  **執行應用程式**：
    - 連接一台 Android 裝置或啟動一個 Android 模擬器。
    - 在 Android Studio 中點擊 "Run 'app'" 按鈕。

## 檔案結構 (主要檔案)

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/markdown/
│   │   │   ├── MainActivity.kt           # 主活動，管理分頁與匯入/開啟檔案邏輯
│   │   │   ├── ViewPagerAdapter.kt       # ViewPager2 的適配器
│   │   │   ├── MarkdownViewModel.kt      # ViewModel，用於共享 Markdown 內容
│   │   │   ├── EditorFragment.kt         # 編輯分頁的 Fragment，處理文字輸入與儲存邏輯
│   │   │   └── PreviewFragment.kt        # 預覽分頁的 Fragment，使用 Markwon 渲染內容
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml     # 主活動的佈局
│   │   │   │   ├── fragment_editor.xml   # 編輯分頁的佈局 (包含懸浮儲存按鈕)
│   │   │   │   ├── fragment_preview.xml  # 預覽分頁的佈局
│   │   │   │   └── dialog_save_file.xml  # 儲存檔案時輸入檔名的對話框佈局
│   │   │   ├── values/                   # 資源檔案 (顏色、字串、樣式)
│   │   │   ├── drawable/                 # Drawable 資源 (圖示前景/背景)
│   │   │   └── mipmap-anydpi/            # 自適應圖示
│   │   └── AndroidManifest.xml         # 應用程式的清單檔案，包含權限與意圖過濾器
│   └── ...
├── build.gradle.kts                # 應用程式層級的 Gradle 建置腳本 (含依賴項)
...
```