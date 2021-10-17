package com.aliyun.sls.android.producer.example.example.trace.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.aliyun.sls.android.producer.example.databinding.TraceItemCartLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.CartItemModel;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.DetailActivity;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListFragment;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseRecyclerAdapter;
import com.aliyun.sls.android.producer.example.example.trace.utils.ImageUtils;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class CartFragment extends BaseListFragment<TraceItemCartLayoutBinding, CartItemModel, CartViewModel> {

    private CartViewModel cartViewModel;

    @Override
    protected BaseRecyclerAdapter.IViewUpdater<TraceItemCartLayoutBinding, CartItemModel> onCreateViewUpdater() {
        return new BaseRecyclerAdapter.IViewUpdater<TraceItemCartLayoutBinding, CartItemModel>() {
            @Override
            public TraceItemCartLayoutBinding onCreateBinding(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return TraceItemCartLayoutBinding.inflate(inflater, parent, false);
            }

            @Override
            public void onUpdate(TraceItemCartLayoutBinding binding, CartItemModel cartModel, int pos) {
                binding.cartPriceText.setText(String.valueOf(cartModel.unitPrice));
                ApiClient.getDetail(cartModel.itemId, new ApiClient.ApiCallback<ItemModel>() {
                    @Override
                    public void onSuccess(ItemModel model) {
                        if (cartModel.itemId.equals(model.id)) {
                            ImageUtils.loadImage(model.imageUrl.get(0), binding.cartImage);
                            binding.cartTitleText.setText(model.name);
                        }
                    }

                    @Override
                    public void onError(int code, String error) {

                    }
                });
                binding.getRoot().setOnClickListener(v -> DetailActivity.start(CartFragment.this.getContext(), cartModel.itemId));
            }
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void onVisibilityChanged(boolean visible) {
        super.onVisibilityChanged(visible);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}