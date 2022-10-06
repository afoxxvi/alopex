package com.afoxxvi.alopex.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afoxxvi.alopex.AlopexView;
import com.afoxxvi.alopex.R;
import com.afoxxvi.alopex.component.notify.Notify;
import com.afoxxvi.alopex.component.notify.NotifyGroup;
import com.afoxxvi.alopex.component.notify.NotifyManager;
import com.afoxxvi.alopex.databinding.FragmentNotificationBinding;
import com.afoxxvi.alopex.databinding.LiNotificationInfoBinding;
import com.afoxxvi.alopex.ui.dialog.NotifyListDialog;
import com.afoxxvi.alopex.util.FoxTools;

public class NotificationFragment extends Fragment {
    private FragmentNotificationBinding binding;

    public NotificationFragment() {
    }

    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlopexView.handlerNotification = handler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        binding = FragmentNotificationBinding.bind(view);
        binding.recyclerNotification.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerNotification.setAdapter(new Adapter());
        return view;
    }

    public static final int WHAT_NOTIFICATION_LISTENER_SERVICE = 1;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == WHAT_NOTIFICATION_LISTENER_SERVICE) {
                Bundle bd = msg.getData();
                Notify notify = new Notify(bd.getString("title"), bd.getString("content"), FoxTools.getLocalDateTimeFromMills(bd.getLong("time", System.currentTimeMillis())));
                int from = NotifyManager.getInstance().newNotify(getContext(), bd.getString("package"), notify);
                RecyclerView.Adapter<?> adapter = binding.recyclerNotification.getAdapter();
                if (adapter != null && from != -1) {
                    if (from == adapter.getItemCount()) {
                        adapter.notifyItemInserted(0);
                    } else {
                        adapter.notifyItemMoved(from, 0);
                    }
                    binding.recyclerNotification.scrollToPosition(0);
                }
            }
        }
    };


    private static final class ViewHolder extends RecyclerView.ViewHolder {
        LiNotificationInfoBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LiNotificationInfoBinding.bind(itemView.getRootView());
        }
    }

    private static class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.li_notification_info, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            NotifyGroup group = NotifyManager.getInstance().getNotifyGroupList().get(position);
            holder.binding.setGroup(group);
            holder.itemView.setOnClickListener(v -> new NotifyListDialog(v.getContext(), group).show());
        }

        @Override
        public int getItemCount() {
            return NotifyManager.getInstance().getNotifyGroupList().size();
        }
    }
}