package com.aliyun.sls.android.producer.example.example.trace.ui.order;

import java.util.List;

import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.OrderModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListViewModel;

/**
 * @author gordon
 * @date 2021/10/17
 */
public class OrderViewModel extends BaseListViewModel<OrderModel> {
    public OrderViewModel() {
        super("order");
    }

    @Override
    protected void fetchItemsFromServer() {
        ApiClient.getOrders(new ApiClient.ApiCallback<List<OrderModel>>() {
            @Override
            public void onSuccess(List<OrderModel> cartItemModels) {
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
