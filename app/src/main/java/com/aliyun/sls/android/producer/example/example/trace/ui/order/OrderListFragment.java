package com.aliyun.sls.android.producer.example.example.trace.ui.order;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.aliyun.sls.android.producer.example.databinding.TraceItemCartLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.model.CartItemModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListFragment;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseRecyclerAdapter;

/**
 * @author gordon
 * @date 2021/10/17
 */
public class OrderListFragment extends BaseListFragment<TraceItemCartLayoutBinding, CartItemModel, OrderViewModel> {

    @Override
    protected BaseRecyclerAdapter.IViewContract<TraceItemCartLayoutBinding, CartItemModel> onCreateViewUpdater() {
        return new BaseRecyclerAdapter.IViewContract<TraceItemCartLayoutBinding, CartItemModel>() {
            @Override
            public TraceItemCartLayoutBinding onCreateBinding(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return TraceItemCartLayoutBinding.inflate(inflater, parent, false);
            }

            @Override
            public void onUpdate(TraceItemCartLayoutBinding binding, CartItemModel cartItemModel, int pos) {

            }
        };
    }
}
