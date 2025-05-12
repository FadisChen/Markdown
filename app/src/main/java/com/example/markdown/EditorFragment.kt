package com.example.markdown

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.OutputStream
import java.io.OutputStreamWriter

class EditorFragment : Fragment() {
    private lateinit var editorTextArea: EditText
    private lateinit var fabSave: FloatingActionButton
    private val viewModel: MarkdownViewModel by activityViewModels()
    
    // 標記變數，用於防止 TextWatcher 和 LiveData 觀察者之間的循環更新
    private var isUpdatingFromViewModel = false
    
    // 用於跟踪內容是否變更
    private var isContentChanged = false
    
    // 用於保存文件的啟動器
    private val saveFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                saveMarkdownToUri(uri, editorTextArea.text.toString())
            }
        }
    }

    companion object {
        fun newInstance() = EditorFragment()
        private const val DEFAULT_FILENAME = "untitled.md"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        editorTextArea = view.findViewById(R.id.editorTextArea)
        fabSave = view.findViewById(R.id.fabSave)
        
        // 設定存檔按鈕點擊事件
        fabSave.setOnClickListener {
            showSaveFileDialog()
        }
        
        // 設定文字變更監聽器，當編輯器內容變更時更新 ViewModel
        editorTextArea.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                if (!isUpdatingFromViewModel) {
                    viewModel.updateMarkdownContent(s.toString())
                    if (!isContentChanged) { // 只有在內容真正從「未變更」變為「已變更」時才更新
                        isContentChanged = true
                        updateSaveButtonVisibility()
                    }
                }
            }
        })
        
        // 觀察 ViewModel 中的 markdown 內容變化
        viewModel.markdownContent.observe(viewLifecycleOwner) { content ->
            // 檢查是否真的需要更新，避免不必要的setText和游標移動
            if (editorTextArea.text.toString() != content) {
                isUpdatingFromViewModel = true
                editorTextArea.setText(content)
                // 移動游標到開頭並確保捲軸在頂部
                editorTextArea.setSelection(0)
                editorTextArea.scrollTo(0, 0)
                isUpdatingFromViewModel = false
                
                // 從 ViewModel 載入內容後，視為未修改狀態
                isContentChanged = false
                updateSaveButtonVisibility()
            }
        }
        
        // 載入初始示範內容
        val initialMarkdown = "# Markdown Editor\n\n" +
                "This is a **bold** text.\n\n" +
                "This is an *italic* text.\n\n" +
                "## List Example\n" +
                "* Item 1\n" +
                "* Item 2\n" +
                "* Item 3\n\n" +
                "[Link Example](https://example.com)"
        
        // 只有在 ViewModel 尚未有內容時才設定初始內容
        // 並且，初始載入不應直接觸發fabSave的顯示
        if (viewModel.markdownContent.value.isNullOrEmpty()) {
            isUpdatingFromViewModel = true // 防止TextWatcher觸發
            editorTextArea.setText(initialMarkdown)
            editorTextArea.setSelection(0) // 初始游標在開頭
            editorTextArea.scrollTo(0,0)   // 初始捲軸在頂部
            isUpdatingFromViewModel = false
            isContentChanged = false // 初始內容視為未變更
            updateSaveButtonVisibility() // 確保fabSave初始為隱藏
        }
    }
    
    // 更新存檔按鈕可見性
    private fun updateSaveButtonVisibility() {
        fabSave.visibility = if (isContentChanged) View.VISIBLE else View.GONE
    }
    
    // 顯示儲存檔案對話框
    private fun showSaveFileDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_save_file, null)
        val textInputLayout = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editTextFileName = dialogView.findViewById<TextInputEditText>(R.id.editTextFileName)
        
        // 預設檔名
        editTextFileName.setText(DEFAULT_FILENAME)
        
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_title_save)
            .setView(dialogView)
            .setPositiveButton(R.string.dialog_button_save) { _, _ ->
                val fileName = editTextFileName.text.toString().trim()
                if (fileName.isNotEmpty()) {
                    openFileSavePicker(fileName)
                } else {
                    Toast.makeText(requireContext(), "請輸入檔案名稱", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.dialog_button_cancel, null)
            .show()
    }
    
    // 開啟檔案儲存選擇器
    private fun openFileSavePicker(fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/markdown"
            putExtra(Intent.EXTRA_TITLE, ensureMarkdownExtension(fileName))
        }
        
        saveFileLauncher.launch(intent)
    }
    
    // 確保檔名有 .md 副檔名
    private fun ensureMarkdownExtension(fileName: String): String {
        return if (fileName.endsWith(".md", true) || fileName.endsWith(".markdown", true)) {
            fileName
        } else {
            "$fileName.md"
        }
    }
    
    // 將 Markdown 內容儲存到指定的 URI
    private fun saveMarkdownToUri(uri: Uri, content: String) {
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                writeTextToOutputStream(outputStream, content)
                Toast.makeText(requireContext(), R.string.save_success, Toast.LENGTH_SHORT).show()
                
                // 儲存成功後，重置內容變更狀態
                isContentChanged = false
                updateSaveButtonVisibility()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.save_error, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    // 將文字寫入輸出流
    private fun writeTextToOutputStream(outputStream: OutputStream, text: String) {
        OutputStreamWriter(outputStream).use { writer ->
            writer.write(text)
        }
    }
} 