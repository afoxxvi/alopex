package com.afoxxvi.alopex.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.afoxxvi.alopex.AlopexView;
import com.afoxxvi.alopex.R;
import com.afoxxvi.alopex.databinding.DialogRuleBinding;
import com.afoxxvi.alopex.filter.AlopexFilter;
import com.afoxxvi.alopex.util.Pair;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MatchRuleDialog extends BaseDialog {
    private final AlopexFilter.Match match;
    private final DialogRuleBinding binding;
    private final List<Pair<AlopexFilter.Rule, String>> list;

    public MatchRuleDialog(Context context, AlopexFilter.Match match) {
        super(context, "Edit match rule");
        this.binding = DialogRuleBinding.inflate(LayoutInflater.from(context));
        setContent(binding.getRoot());
        this.match = match;
        this.binding.editName.setText(match.getName());
        this.list = new ArrayList<>();
        for (Pair<AlopexFilter.Rule, String> pair : match.getRuleList()) {
            this.list.add(pair.copy());
        }
        this.list.forEach(p -> addChip(this.binding.chipsRuleList, p, list));
        List<String> strings = new ArrayList<>();
        for (AlopexFilter.Rule rule : AlopexFilter.Rule.values()) {
            strings.add(rule.name());
        }
        this.binding.spinnerRuleMethod.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, strings));
        this.binding.buttonAddNewRule.setOnClickListener(v -> {
            String pattern = this.binding.editNewRulePattern.getText().toString();
            if (pattern.isEmpty()) {
                AlopexView.showToast(context, "Enter pattern", Toast.LENGTH_SHORT);
                this.binding.editNewRulePattern.requestFocus();
                return;
            }
            this.binding.editNewRulePattern.setText("");
            Pair<AlopexFilter.Rule, String> pair = new Pair<>(AlopexFilter.Rule.valueOf(this.binding.spinnerRuleMethod.getSelectedItem().toString()), pattern);
            list.add(pair);
            addChip(binding.chipsRuleList, pair, list);
        });
    }

    @Override
    public void onConfirm() {
        String name = this.binding.editName.getText().toString();
        if (name.isEmpty()) {
            AlopexView.showToast(context, "Enter rule name", Toast.LENGTH_SHORT);
            this.binding.editName.requestFocus();
            return;
        }
        match.setName(name);
        match.setRuleList(list);
        super.onConfirm();
    }

    public void addChip(ChipGroup group, Pair<AlopexFilter.Rule, String> pair, List<Pair<AlopexFilter.Rule, String>> list) {
        Chip chip = new Chip(context);
        chip.setText(String.format("[%s]%s", pair.a.display(), pair.b));
        chip.setOnClickListener(v -> chip.setCloseIconVisible(!chip.isCloseIconVisible()));
        chip.setOnCloseIconClickListener(v ->
                new BaseDialog(context, "Delete rule?") {
                    @Override
                    public void onConfirm() {
                        for (Iterator<Pair<AlopexFilter.Rule, String>> iterator = list.listIterator(); iterator.hasNext(); ) {
                            Pair<AlopexFilter.Rule, String> m = iterator.next();
                            if (m == pair) {
                                iterator.remove();
                                group.removeView(chip);
                                break;
                            }
                        }
                        super.onConfirm();
                    }
                }.setButtonText("Yes", "No").show());
        group.addView(chip, new ChipGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
}
