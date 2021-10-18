package com.aliyun.sls.android.producer.example.example.trace.ui.cart;

import android.widget.Toast;

import com.aliyun.sls.android.producer.example.SLSGlobal;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.CartItemModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListViewModel;

import java.util.List;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class CartViewModel extends BaseListViewModel<CartItemModel> {
    public static final int STATUS_TYPE_CREATE_ORDER = 0x3011;

//    private MutableLiveData<List<CartItemModel>> itemLiveData;

    public CartViewModel() {
        super("cart");
    }

    @Override
    protected void fetchItemsFromServer() {
        ApiClient.getCart(new ApiClient.ApiCallback<List<CartItemModel>>() {
            @Override
            public void onSuccess(List<CartItemModel> itemModels) {
                items.setValue(itemModels);
                status.setValue(Status.success());
            }

            @Override
            public void onError(int code, String error) {
                status.setValue(Status.error(String.valueOf(code), error));
                Toast.makeText(SLSGlobal.applicationContext, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createOrder() {
        ApiClient.createOrder(new ApiClient.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                status.setValue(Status.success(STATUS_TYPE_CREATE_ORDER));
            }

            @Override
            public void onError(int code, String error) {
                status.setValue(Status.error(STATUS_TYPE_CREATE_ORDER, String.valueOf(code), error));
            }
        });
    }
}