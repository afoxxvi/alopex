package com.afoxxvi.alopex.util

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class AlViewHolder<T : ViewDataBinding>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    lateinit var binding: T
}