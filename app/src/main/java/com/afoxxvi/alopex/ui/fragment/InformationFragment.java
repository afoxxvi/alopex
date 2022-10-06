package com.afoxxvi.alopex.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.afoxxvi.alopex.AlopexView;
import com.afoxxvi.alopex.R;
import com.afoxxvi.alopex.databinding.FragmentInformationBinding;

public class InformationFragment extends Fragment {
    private FragmentInformationBinding binding;

    public InformationFragment() {
    }

    public static InformationFragment newInstance() {
        return new InformationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //printStream = FileUtils.getOutputStream(getContext(), "life.txt");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_information, container, false);
        binding = FragmentInformationBinding.bind(view);
        binding.setAlopex(AlopexView.getInstance());
        return view;
    }
}