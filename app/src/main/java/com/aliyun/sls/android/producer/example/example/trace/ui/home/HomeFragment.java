package com.aliyun.sls.android.producer.example.example.trace.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.sls.android.producer.example.databinding.FragmentHomeBinding;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.VisibilityFragment;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;

import java.util.List;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class HomeFragment extends VisibilityFragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final RecyclerView recyclerView = binding.homeRecyclerview;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        final HomeRecyclerAdapter adapter = new HomeRecyclerAdapter();
        recyclerView.setAdapter(adapter);

        homeViewModel.getList().observe(getViewLifecycleOwner(), new Observer<List<ItemModel>>() {
            @Override
            public void onChanged(List<ItemModel> list) {
                adapter.updateData(list);
            }
        });

        return root;
    }

    @Override
    protected void onVisibilityChanged(boolean visible) {
        super.onVisibilityChanged(visible);
        if (visible) {
            homeViewModel.requestData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}