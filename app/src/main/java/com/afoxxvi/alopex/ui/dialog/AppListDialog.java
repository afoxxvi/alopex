package com.afoxxvi.alopex.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afoxxvi.alopex.R;
import com.afoxxvi.alopex.databinding.DialogAppListBinding;
import com.afoxxvi.alopex.databinding.LiAppInfoBinding;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class AppListDialog extends BaseDialog {
    private final List<AppInfo> appInfoList;
    private final DialogAppListBinding binding;

    private static final int WHAT_LOAD_COMPLETE = 1;

    private final Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == WHAT_LOAD_COMPLETE) {
                binding.containerLoading.setVisibility(View.GONE);
                RecyclerView.Adapter<?> adapter = binding.recyclerAppList.getAdapter();
                if (adapter != null) {
                    adapter.notifyItemRangeInserted(0, appInfoList.size());
                }
                binding.recyclerAppList.setVisibility(View.VISIBLE);
            }
        }
    };

    @SuppressLint("QueryPermissionsNeeded")
    public AppListDialog(Context context) {
        super(context, "Installed App List");
        binding = DialogAppListBinding.inflate(LayoutInflater.from(context));
        setContent(binding.getRoot());
        setBottomVisible(false);
        appInfoList = new ArrayList<>();
        binding.recyclerAppList.setVisibility(View.GONE);
        binding.recyclerAppList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        binding.recyclerAppList.setAdapter(new Adapter());
        new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            final ThreadFactory factory = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                return factory.newThread(r);
            }
        }).execute(() -> {
            PackageManager pm = context.getPackageManager();
            for (ApplicationInfo info : pm.getInstalledApplications(0)) {
                if ((info.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    continue;
                }
                String label = info.loadLabel(pm).toString();
                if (Objects.equals(label, info.packageName)) {
                    continue;
                }
                appInfoList.add(new AppInfo(info.packageName, label, info.loadIcon(pm)));
            }
            appInfoList.sort((i1, i2) -> Collator.getInstance(Locale.CHINA).compare(i1.appName, i2.appName));
            Message.obtain(handler, WHAT_LOAD_COMPLETE).sendToTarget();
        });
    }

    public void onSelect(AppInfo appInfo) {

    }

    public static class AppInfo extends BaseObservable {
        private final String packageName;
        private final String appName;
        private final Drawable appIcon;

        public AppInfo(String packageName, String appName, Drawable appIcon) {
            this.packageName = packageName;
            this.appName = appName;
            this.appIcon = appIcon;
        }

        @Bindable
        public String getPackageName() {
            return packageName;
        }

        @Bindable
        public String getAppName() {
            return appName;
        }

        @Bindable
        public Drawable getAppIcon() {
            return appIcon;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LiAppInfoBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LiAppInfoBinding.bind(itemView);
        }
    }

    public class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.li_app_info, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.binding.setInfo(appInfoList.get(position));
            holder.itemView.setOnClickListener(v -> onSelect(appInfoList.get(position)));
        }

        @Override
        public int getItemCount() {
            return appInfoList.size();
        }
    }
}
