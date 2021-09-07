package com.aliyun.sls.android.producer.example.example.trace.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.sls.android.producer.example.databinding.FragmentCategoryBinding;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class CategoryFragment extends Fragment {

    private CategoryViewModel categoryViewModel;
    private FragmentCategoryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        categoryViewModel =
                new ViewModelProvider(this).get(CategoryViewModel.class);

        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        final RecyclerView recyclerView = binding.categoryRecyclerview;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final CategoryRecyclerAdapter adapter = new CategoryRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        categoryViewModel.getItemModelList().observe(getViewLifecycleOwner(), adapter::update);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        categoryViewModel.update();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}