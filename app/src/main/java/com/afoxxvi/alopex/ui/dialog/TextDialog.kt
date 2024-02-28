package com.afoxxvi.alopex.ui.dialog

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import com.afoxxvi.alopex.databinding.DialogSingleTextBinding

abstract class TextDialog(context: Context, header: String?, text: SpannableStringBuilder?) : BaseDialog(context, header) {
    private val binding: DialogSingleTextBinding

    init {
        binding = DialogSingleTextBinding.inflate(LayoutInflater.from(context))
        setContent(binding.root)
        binding.content.text = text
        binding.content.movementMethod = ScrollingMovementMethod.getInstance()
    }
}