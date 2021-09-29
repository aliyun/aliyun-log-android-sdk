package com.aliyun.sls.android.producer.example.example.trace.ui.cart;

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

import com.aliyun.sls.android.producer.example.databinding.FragmentCartBinding;
import com.aliyun.sls.android.producer.example.example.trace.core.VisibilityFragment;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class CartFragment extends VisibilityFragment {

    private CartViewModel cartViewModel;
    private FragmentCartBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cartViewModel =
                new ViewModelProvider(this).get(CartViewModel.class);

        binding = FragmentCartBinding.inflate(inflater, container, false);
        final RecyclerView recyclerView = binding.cartRecyclerview;
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final CartRecyclerAdapter adapter = new CartRecyclerAdapter();
        recyclerView.setAdapter(adapter);

        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), adapter::update);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cartViewModel.requestCartList(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}