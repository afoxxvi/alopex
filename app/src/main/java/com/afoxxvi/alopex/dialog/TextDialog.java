package com.afoxxvi.alopex.dialog;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;

import com.afoxxvi.alopex.R;
import com.afoxxvi.alopex.databinding.DialogSingleTextBinding;

public abstract class TextDialog extends BaseDialog {
    private final DialogSingleTextBinding binding;

    public TextDialog(Context context, String header, SpannableStringBuilder text) {
        super(context, header);
        binding = DialogSingleTextBinding.inflate(LayoutInflater.from(context));
        setContent(binding.getRoot());
        binding.content.setText(text);
        binding.content.setMovementMethod(ScrollingMovementMethod.getInstance());
    }
}
