package com.aliyun.sls.android.producer.example.example.trace.ui.cart;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.CartItemModel;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;

import java.util.List;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class CartViewModel extends ViewModel {

    private MutableLiveData<List<CartItemModel>> itemLiveData;

    public CartViewModel() {
        itemLiveData = new MutableLiveData<>();
    }

    public LiveData<List<CartItemModel>> getCartItems() {
        return itemLiveData;
    }

    public void requestCartList(Context context) {
        ApiClient.getCart(new ApiClient.ApiCallback<List<CartItemModel>>() {
            @Override
            public void onSuccess(List<CartItemModel> itemModels) {
                itemLiveData.setValue(itemModels);
            }

            @Override
            public void onError(int code, String error) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}