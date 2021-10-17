package com.aliyun.sls.android.producer.example.example.trace.ui.mine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aliyun.sls.android.producer.example.databinding.FragmentMineBinding;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.VisibilityFragment;

/**
 * @author gordon
 * @date 2021/09/06
 */
public class MineFragment extends VisibilityFragment {

    private MineViewModel viewModel;
    private FragmentMineBinding mineBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new MineViewModel();
        mineBinding = FragmentMineBinding.inflate(inflater, container, false);

        return mineBinding.getRoot();
    }
}
