package com.aliyun.sls.android.producer.example.example.trace.ui.order;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.aliyun.sls.android.producer.example.databinding.TraceItemOrderLayoutBinding;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.example.example.trace.model.OrderModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListFragment;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseRecyclerAdapter;
import com.aliyun.sls.android.producer.example.example.trace.utils.ImageUtils;

/**
 * @author gordon
 * @date 2021/10/17
 */
public class OrderListFragment extends BaseListFragment<TraceItemOrderLayoutBinding, OrderModel, OrderViewModel> {

    @Override
    protected BaseRecyclerAdapter.IViewContract<TraceItemOrderLayoutBinding, OrderModel> onCreateViewUpdater() {
        return new BaseRecyclerAdapter.IViewContract<TraceItemOrderLayoutBinding, OrderModel>() {
            @Override
            public TraceItemOrderLayoutBinding onCreateBinding(LayoutInflater inflater, ViewGroup parent, int viewType) {
                return TraceItemOrderLayoutBinding.inflate(inflater, parent, false);
            }

            @Override
            public void onUpdate(TraceItemOrderLayoutBinding binding, OrderModel model, int pos) {
                binding.orderIdText.setText("#" + model.id);
                binding.orderStatusText.setText(model.status);
                binding.orderDateText.setText(model.date);
                binding.getRoot().setOnClickListener(v -> {
                    //
                });

                final String itemId = model.getItemId();
                if (TextUtils.isEmpty(itemId)) {
                    return;
                }

                ApiClient.getDetail(itemId, new ApiClient.ApiCallback<ItemModel>() {
                    @Override
                    public void onSuccess(ItemModel model) {
                        if (itemId.equals(model.id)) {
                            ImageUtils.loadImage(model.imageUrl.get(0), binding.orderImage);
                        }
                    }

                    @Override
                    public void onError(int code, String error) {

                    }
                });
            }
        };
    }
}
