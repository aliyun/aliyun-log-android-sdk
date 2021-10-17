package com.aliyun.sls.android.producer.example.example.trace.ui.cart;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aliyun.sls.android.producer.example.SLSGlobal;
import com.aliyun.sls.android.producer.example.example.trace.core.TraceViewModel;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.CartItemModel;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListViewModel;

import java.util.List;

import io.opentelemetry.api.trace.Span;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class CartViewModel extends BaseListViewModel<CartItemModel> {

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
}