package com.afoxxvi.alopex.ui.fragment

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afoxxvi.alopex.Alopex
import com.afoxxvi.alopex.R
import com.afoxxvi.alopex.databinding.FragmentInformationBinding
import com.afoxxvi.alopex.databinding.LiErrorSummaryBinding
import com.afoxxvi.alopex.ui.dialog.TextDialog
import com.afoxxvi.alopex.util.AlViewHolder
import com.afoxxvi.alopex.util.FoxFiles
import java.io.File
import java.io.FileInputStream

class InformationFragment : Fragment() {
    private var binding: FragmentInformationBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_information, container, false)
        binding = FragmentInformationBinding.bind(view)
        binding!!.alopex = Alopex
        if (context != null) {
            ErrorAdapter.init(requireContext())
            binding!!.recyclerErrors.adapter = ErrorAdapter
            binding!!.recyclerErrors.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
        return view
    }

    companion object {
        fun newInstance(): InformationFragment {
            return InformationFragment()
        }
    }


    internal class ErrorInfo(
        @get:Bindable
        val time: String,
        @get:Bindable
        val message: String,
        @get:Bindable
        val all: String,
    ) : BaseObservable()

    internal object ErrorAdapter : RecyclerView.Adapter<AlViewHolder<LiErrorSummaryBinding>>() {
        private val errorList: MutableList<ErrorInfo> = arrayListOf()

        fun init(context: Context) {
            val dir = File(context.dataDir, "error_logs")
            val files = dir.listFiles()
            if (files != null && files.isNotEmpty()) {
                for (file in files) {
                    val content = FoxFiles.inputStream(FileInputStream(file))
                    val message = content.substring(0, content.indexOf('\n'))
                    val time = file.nameWithoutExtension
                    errorList.add(ErrorInfo(time, message, content))
                }
                errorList.sortBy { it.time }
                errorList.reverse()
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): AlViewHolder<LiErrorSummaryBinding> {
            val holder = AlViewHolder<LiErrorSummaryBinding>(
                LayoutInflater.from(parent.context).inflate(R.layout.li_error_summary, parent, false)
            )
            holder.binding = LiErrorSummaryBinding.bind(holder.itemView.rootView)
            return holder
        }

        override fun onBindViewHolder(holder: AlViewHolder<LiErrorSummaryBinding>, position: Int) {
            val errorInfo = errorList[position]
            holder.binding.info = errorInfo
            holder.binding.root.setOnClickListener { v: View ->
                object : TextDialog(
                    v.context, errorInfo.message, SpannableStringBuilder.valueOf(errorInfo.all)
                ) {}.show()
            }
        }

        override fun getItemCount(): Int = errorList.size
    }
}