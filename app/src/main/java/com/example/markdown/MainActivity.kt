package com.example.markdown

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var viewModel: MarkdownViewModel
    private lateinit var btnImport: MaterialButton

    companion object {
        private const val TAG = "MainActivity"
    }

    // 創建檔案選擇器的結果回調
    private val pickMarkdownFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                readMarkdownFromUri(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[MarkdownViewModel::class.java]

        // 初始化視圖
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        btnImport = findViewById(R.id.btnImport)

        // 設定 ViewPager 適配器
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        // 連接 TabLayout 和 ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_edit)
                1 -> getString(R.string.tab_preview)
                else -> ""
            }
        }.attach()

        // 設定匯入按鈕點擊事件
        btnImport.setOnClickListener {
            openFilePicker()
        }

        // 處理可能從外部傳入的 Intent（開啟 .md 檔案）
        handleIncomingIntent(intent)
    }

    // 處理新的 Intent 傳入
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    // 處理傳入的 Intent，例如從檔案管理器開啟 .md 檔案
    private fun handleIncomingIntent(intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        intent.data?.let { uri ->
            Log.d(TAG, "URI: $uri, Type: ${intent.type}")
            
            val isMarkdownFile = isMarkdownFile(uri)
            if (isMarkdownFile) {
                readMarkdownFromUri(uri)
            }
        }
    }

    // 判斷是否為 Markdown 檔案
    private fun isMarkdownFile(uri: Uri): Boolean {
        val mimeType = contentResolver.getType(uri)
        val fileName = getFileNameFromUri(uri)
        
        Log.d(TAG, "File: $fileName, MimeType: $mimeType")
        
        return when {
            mimeType == "text/markdown" || mimeType == "text/x-markdown" -> true
            mimeType == "text/plain" && (fileName.endsWith(".md", true) || fileName.endsWith(".markdown", true)) -> true
            mimeType == "application/octet-stream" && (fileName.endsWith(".md", true) || fileName.endsWith(".markdown", true)) -> true
            fileName.endsWith(".md", true) || fileName.endsWith(".markdown", true) -> true
            else -> false
        }
    }

    // 開啟檔案選擇器
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"

            // 僅顯示 .md 和 .markdown 檔案
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "text/markdown",
                "text/x-markdown",
                "text/plain",
                "application/octet-stream"
            ))
        }

        pickMarkdownFile.launch(intent)
    }

    // 從 URI 讀取 Markdown 檔案
    private fun readMarkdownFromUri(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val content = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        content.append(line)
                        content.append("\n")
                    }

                    // 更新 ViewModel 中的內容
                    viewModel.updateMarkdownContent(content.toString())
                    
                    // 顯示成功訊息，並包含檔案名稱
                    val fileName = getFileNameFromUri(uri)
                    Toast.makeText(this, "$fileName ${getString(R.string.file_import_success)}", Toast.LENGTH_SHORT).show()
                    
                    // 切換到編輯頁面
                    viewPager.currentItem = 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading file", e)
            Toast.makeText(this, getString(R.string.file_import_error), Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    // 從 URI 取得檔案名稱
    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "未知檔案"
        
        try {
            // 嘗試從 ContentResolver 中獲取檔案名稱
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
            
            // 如果還是沒有檔案名稱，嘗試從 URI 路徑中獲取
            if (fileName == "未知檔案" && uri.path != null) {
                val path = uri.path!!
                val lastSlashIndex = path.lastIndexOf('/')
                if (lastSlashIndex != -1 && lastSlashIndex < path.length - 1) {
                    fileName = path.substring(lastSlashIndex + 1)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting filename", e)
        }
        
        return fileName
    }
} 