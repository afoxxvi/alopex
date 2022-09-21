package com.afoxxvi.alopex.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afoxxvi.alopex.R;
import com.afoxxvi.alopex.databinding.DialogNotifyListBinding;
import com.afoxxvi.alopex.databinding.LiNotificationSummaryBinding;
import com.afoxxvi.alopex.filter.AlopexFilterManager;
import com.afoxxvi.alopex.notify.NotifyGroup;
import com.afoxxvi.alopex.notify.NotifyManager;

public class NotifyListDialog extends BaseDialog {
    private final NotifyGroup notifyGroup;
    private final DialogNotifyListBinding binding;

    public NotifyListDialog(Context context, NotifyGroup notifyGroup) {
        super(context, "Notification list");
        this.notifyGroup = notifyGroup;
        binding = DialogNotifyListBinding.inflate(LayoutInflater.from(context));
        setContent(binding.getRoot());
        setBottomVisible(false);
        binding.recyclerNotifyList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.recyclerNotifyList.setAdapter(new Adapter());
        binding.buttonLoad.setOnClickListener(v -> {
            int oldCount = notifyGroup.getNotifyCount();
            NotifyManager.getInstance().requestMore(notifyGroup, 10);
            int newCount = notifyGroup.getNotifyCount();
            if (oldCount == newCount) {
                v.setVisibility(View.GONE);
            } else {
                RecyclerView.Adapter<?> adapter = binding.recyclerNotifyList.getAdapter();
                if (adapter != null) {
                    adapter.notifyItemRangeInserted(oldCount, newCount - oldCount);
                }
            }
        });
    }

    @Override
    public void onCancel() {
        this.dialog.dismiss();
    }

    @Override
    public void onConfirm() {
        this.dialog.dismiss();
    }

    @Override
    public void show() {
        super.show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LiNotificationSummaryBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LiNotificationSummaryBinding.bind(itemView);
        }
    }

    public class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.li_notification_summary, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.binding.setNotify(notifyGroup.getNotifyList().get(position));
            if (AlopexFilterManager.getInstance().isFiltered(notifyGroup.getPackageName(), notifyGroup.getNotifyList().get(position), false).c) {
                TypedValue tv = new TypedValue();
                context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, tv, true);
                holder.binding.textTitle.setTextColor(ColorStateList.valueOf(tv.data));
                holder.binding.textContent.setTextColor(ColorStateList.valueOf(tv.data));
                holder.binding.textTime.setTextColor(ColorStateList.valueOf(tv.data));
            }
        }

        @Override
        public int getItemCount() {
            return notifyGroup.getNotifyCount();
        }
    }
}
