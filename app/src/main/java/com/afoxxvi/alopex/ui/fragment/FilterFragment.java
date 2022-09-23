package com.afoxxvi.alopex.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afoxxvi.alopex.AlopexView;
import com.afoxxvi.alopex.R;
import com.afoxxvi.alopex.component.filter.AlopexFilter;
import com.afoxxvi.alopex.component.filter.AlopexFilterManager;
import com.afoxxvi.alopex.databinding.FragmentFilterBinding;
import com.afoxxvi.alopex.databinding.LiFilterInfoBinding;
import com.afoxxvi.alopex.ui.dialog.FilterDialog;

public class FilterFragment extends Fragment {
    private FragmentFilterBinding binding;

    public FilterFragment() {
    }

    public static FilterFragment newInstance() {
        return new FilterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        binding = FragmentFilterBinding.bind(view);
        binding.recyclerBroadcast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerBroadcast.setAdapter(new Adapter());
        binding.fabAddFilter.setOnClickListener(v -> new FilterDialog(getContext(), new AlopexFilter("")) {
            @Override
            public void onConfirm() {
                RecyclerView.Adapter<?> adapter = binding.recyclerBroadcast.getAdapter();
                int oldSize = -1;
                if (adapter != null) {
                    oldSize = adapter.getItemCount();
                }
                super.onConfirm();
                if (adapter != null && adapter.getItemCount() > oldSize) {
                    adapter.notifyItemInserted(adapter.getItemCount());
                }
            }
        }.show());
        return view;
    }

    private static final class ViewHolder extends RecyclerView.ViewHolder {
        LiFilterInfoBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LiFilterInfoBinding.bind(itemView);
        }
    }

    private static final class Adapter extends RecyclerView.Adapter<ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.li_filter_info, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AlopexFilter filter = AlopexFilterManager.getInstance().getFilters().get(position);
            holder.binding.setFilter(filter);
            holder.binding.getRoot().setOnClickListener(v -> new FilterDialog(holder.itemView.getContext(), filter).show());
            holder.binding.getRoot().setOnLongClickListener(v -> {
                String str = filter.toJsonObject().toString();
                Context context = holder.binding.getRoot().getContext();
                ClipboardManager clipboardManager = context.getSystemService(ClipboardManager.class);
                clipboardManager.setPrimaryClip(ClipData.newPlainText("filter json", str));
                AlopexView.showToast(context, "filter json copied", Toast.LENGTH_SHORT);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return AlopexFilterManager.getInstance().getFilters().size();
        }
    }
}