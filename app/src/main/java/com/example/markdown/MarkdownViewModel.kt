package com.example.markdown

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MarkdownViewModel : ViewModel() {
    private val _markdownContent = MutableLiveData<String>()
    val markdownContent: LiveData<String> = _markdownContent

    fun updateMarkdownContent(content: String) {
        _markdownContent.value = content
    }
} 