package com.example.markdown

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin

class PreviewFragment : Fragment() {
    private lateinit var previewTextView: TextView
    private val viewModel: MarkdownViewModel by activityViewModels()
    private lateinit var markwon: Markwon

    companion object {
        fun newInstance() = PreviewFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        previewTextView = view.findViewById(R.id.previewTextView)
        
        // 初始化 Markwon
        markwon = Markwon.builder(requireContext())
            .usePlugin(ImagesPlugin.create())
            .usePlugin(HtmlPlugin.create())
            .usePlugin(TablePlugin.create(requireContext()))
            .build()
            
        // 觀察 ViewModel 中的 Markdown 內容變更
        viewModel.markdownContent.observe(viewLifecycleOwner) { markdownContent ->
            if (markdownContent.isNotEmpty()) {
                // 使用 Markwon 將 Markdown 轉換為帶格式的文字
                markwon.setMarkdown(previewTextView, markdownContent)
            }
        }
    }
} 