package com.aliyun.sls.android.producer.example.example.trace.ui.order;

import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.CartItemModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListViewModel;

import java.util.List;

/**
 * @author gordon
 * @date 2021/10/17
 */
public class OrderViewModel extends BaseListViewModel<CartItemModel> {
    public OrderViewModel() {
        super("order");
    }

    @Override
    protected void fetchItemsFromServer() {
        ApiClient.getOrders(new ApiClient.ApiCallback<List<CartItemModel>>() {
            @Override
            public void onSuccess(List<CartItemModel> cartItemModels) {
                items.setValue(cartItemModels);
                status.setValue(Status.success());
            }

            @Override
            public void onError(int code, String error) {
                status.setValue(Status.error(String.valueOf(code), error));
            }
        });
    }

}
