package com.afoxxvi.alopex.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import com.afoxxvi.alopex.AlopexView;
import com.afoxxvi.alopex.component.filter.AlopexFilter;
import com.afoxxvi.alopex.component.filter.AlopexFilterManager;
import com.afoxxvi.alopex.databinding.DialogFilterBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FilterDialog extends BaseDialog {
    private final DialogFilterBinding binding;
    private final AlopexFilter filter;
    private final List<AlopexFilter.Match> matchList;

    public FilterDialog(Context context, AlopexFilter filter) {
        super(context, "Edit filter");
        this.filter = filter;
        this.matchList = new ArrayList<>();
        for (AlopexFilter.Match match : filter.getMatchList()) {
            this.matchList.add(AlopexFilter.Match.copyOf(match));
        }
        binding = DialogFilterBinding.inflate(LayoutInflater.from(context));
        setContent(binding.getRoot());

        binding.editPackage.setText(filter.getPackageName());
        binding.editPackage.setOnClickListener(v -> new AppListDialog(context) {
            @Override
            public void onSelect(AppInfo appInfo) {
                super.onSelect(appInfo);
                binding.editPackage.setText(appInfo.getPackageName());
                dialog.dismiss();
            }
        }.show());
        binding.checkCancel.setChecked(filter.getCancelFiltered());
        binding.checkNotify.setChecked(filter.getNotifyUnfiltered());
        for (AlopexFilter.Match match : matchList) {
            addChip(binding.chipsBlacklist, match, matchList);
        }
        binding.buttonAddBlacklist.setOnClickListener(v -> addMatchRule(context, binding.editNewBlacklist, matchList, binding.chipsBlacklist, "enter match name."));
    }

    private void addMatchRule(Context context, EditText editFrom, List<AlopexFilter.Match> matchList, ChipGroup targetGroup, String emptyMsg) {
        String text = editFrom.getText().toString();
        if (text.length() > 0) {
            AlopexFilter.Match match = new AlopexFilter.Match(text);
            editFrom.setText("");
            new MatchRuleDialog(context, match) {
                @Override
                public void onConfirm() {
                    super.onConfirm();
                    matchList.add(match);
                    FilterDialog.this.addChip(targetGroup, match, matchList);
                }
            }.show();
        } else {
            AlopexView.showToast(context, emptyMsg, Toast.LENGTH_SHORT);
            editFrom.requestFocus();
        }
    }

    @Override
    public void onCancel() {
        super.onCancel();
    }

    @Override
    public void onConfirm() {
        AlopexFilterManager manager = AlopexFilterManager.getInstance();
        String pkgName = binding.editPackage.getText().toString();
        if (pkgName.isEmpty()) {
            binding.editPackage.requestFocus();
            AlopexView.showToast(context, "package name is empty.", Toast.LENGTH_SHORT);
            return;
        }
        filter.setPackageName(pkgName);
        filter.setMatchList(matchList);
        filter.setCancelFiltered(binding.checkCancel.isChecked());
        filter.setNotifyUnfiltered(binding.checkNotify.isChecked());
        filter.notifyChange();
        if (!manager.getFilters().contains(filter)) {
            manager.getFilters().add(filter);
        }
        manager.save(context);
        this.dialog.dismiss();
    }

    @SuppressLint("DefaultLocale")
    private void addChip(ChipGroup target, AlopexFilter.Match match, List<AlopexFilter.Match> list) {
        Chip chip = new Chip(context);
        chip.setText(String.format("%s(%d)", match.getName(), match.getMatchCount()));
        chip.setOnClickListener(v -> chip.setCloseIconVisible(!chip.isCloseIconVisible()));
        chip.setOnLongClickListener(v -> {
            new MatchRuleDialog(context, match).show();
            return true;
        });
        chip.setOnCloseIconClickListener(v ->
                new BaseDialog(context, "Delete match rule?") {
                    @Override
                    public void onConfirm() {
                        for (Iterator<AlopexFilter.Match> iterator = list.listIterator(); iterator.hasNext(); ) {
                            AlopexFilter.Match m = iterator.next();
                            if (m == match) {
                                iterator.remove();
                                target.removeView(chip);
                                break;
                            }
                        }
                        super.onConfirm();
                    }
                }.setButtonText("Yes", "No").show());
        target.addView(chip);
    }

    @Override
    public void show() {
        super.show();
        dialog.setCancelable(false);
    }
}
