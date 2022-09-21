package com.afoxxvi.alopex.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import com.afoxxvi.alopex.R;
import com.afoxxvi.alopex.databinding.DialogBaseBinding;

public abstract class BaseDialog {
    protected Dialog dialog;
    protected final Context context;
    protected final View view;
    protected final DialogBaseBinding baseBinding;

    public BaseDialog(Context context, String title) {
        this.context = context;
        //view = LayoutInflater.from(context).inflate(layout, null);
        baseBinding = DialogBaseBinding.inflate(LayoutInflater.from(context));
        view = baseBinding.getRoot();

        baseBinding.textTitle.setText(title);
        baseBinding.buttonConfirm.setOnClickListener(v -> onConfirm());
        baseBinding.buttonCancel.setOnClickListener(v -> onCancel());
    }

    protected BaseDialog setButtonText(String confirm, String cancel) {
        baseBinding.buttonCancel.setText(cancel);
        baseBinding.buttonConfirm.setText(confirm);
        return this;
    }

    protected BaseDialog setBottomVisible(boolean visible) {
        baseBinding.dialogBottom.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    protected BaseDialog setContent(View view) {
        baseBinding.contentLayout.addView(view);
        return this;
    }

    public void show() {
        dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();
        Window window = dialog.getWindow();
        window.setBackgroundDrawableResource(R.color.Transparent);
        dialog.show();
    }

    public void onConfirm() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void onCancel() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
