package com.aliyun.sls.android.producer.example.example.trace.ui.core;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * @author gordon
 * @date 2021/10/18
 */
public class BaseFragment extends Fragment {
    private String title;

    protected void setTile(String title) {
        this.title = title;
        if (getActivity() != null) {
            getActivity().setTitle(title);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != getActivity()) {
            getActivity().setTitle(title);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
