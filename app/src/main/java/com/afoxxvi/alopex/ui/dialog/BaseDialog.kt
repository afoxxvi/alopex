package com.afoxxvi.alopex.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.afoxxvi.alopex.R
import com.afoxxvi.alopex.databinding.DialogBaseBinding

abstract class BaseDialog(protected val context: Context, title: String?) {
    @JvmField
    protected var dialog: Dialog? = null
    protected val view: View
    private val baseBinding: DialogBaseBinding = DialogBaseBinding.inflate(LayoutInflater.from(context))

    init {
        view = baseBinding.root
        baseBinding.textTitle.text = title
        baseBinding.buttonConfirm.setOnClickListener { onConfirm() }
        baseBinding.buttonCancel.setOnClickListener { onCancel() }
    }

    fun setButtonText(confirm: String?, cancel: String?): BaseDialog {
        baseBinding.buttonCancel.text = cancel
        baseBinding.buttonConfirm.text = confirm
        return this
    }

    protected fun setBottomVisible(visible: Boolean): BaseDialog {
        baseBinding.dialogBottom.visibility = if (visible) View.VISIBLE else View.GONE
        return this
    }

    protected fun setContent(view: View?): BaseDialog {
        baseBinding.contentLayout.addView(view)
        return this
    }

    open fun show() {
        dialog = AlertDialog.Builder(context)
                .setView(view)
                .create()
        val ad = dialog as AlertDialog?
        ad?.window?.setBackgroundDrawableResource(R.color.Transparent)
        ad?.show()
    }

    open fun onConfirm() {
        dialog?.dismiss()
    }

    open fun onCancel() {
        dialog?.dismiss()
    }
}