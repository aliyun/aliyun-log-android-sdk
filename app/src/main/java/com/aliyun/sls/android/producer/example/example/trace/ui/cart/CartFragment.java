package com.aliyun.sls.android.producer.example.example.trace.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.aliyun.sls.android.producer.example.databinding.FragmentCartBinding;
import com.aliyun.sls.android.producer.example.databinding.TraceItemCartLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.CartItemModel;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.FragmentActivity;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListFragment;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListViewModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseRecyclerAdapter;
import com.aliyun.sls.android.producer.example.example.trace.ui.order.detail.DetailActivity;
import com.aliyun.sls.android.producer.example.example.trace.utils.ImageUtils;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class CartFragment extends BaseListFragment<TraceItemCartLayoutBinding, CartItemModel, CartViewModel> {

    private FragmentCartBinding cartBinding;

    @Override
    protected View onCreateFooterView(LayoutInflater inflater, ViewGroup parent) {
        cartBinding = FragmentCartBinding.inflate(inflater, parent, false);
        return cartBinding.getRoot();
    }

    @Override
    protected BaseRecyclerAdapter.IViewContract<TraceItemCartLayoutBinding, CartItemModel> onCreateViewUpdater() {
        return new BaseRecyclerAdapter.IViewContract<TraceItemCartLayoutBinding, CartItemModel>() {
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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getStatus().observe(getViewLifecycleOwner(), status -> {
            if (status.type == CartViewModel.STATUS_TYPE_CREATE_ORDER) {
                if (status.success) {
                    FragmentActivity.startOrderListPage(getActivity());
                }
            }
        });
        cartBinding.cartPurchaseBtn.setOnClickListener(v -> viewModel.createOrder());
    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
    }

    @Override
    protected void onStatusChanged(BaseListViewModel.Status status) {
        super.onStatusChanged(status);
        if (status.success) {
            ((ViewGroup) cartBinding.getRoot().getParent()).setVisibility(View.VISIBLE);
        }
    }
}