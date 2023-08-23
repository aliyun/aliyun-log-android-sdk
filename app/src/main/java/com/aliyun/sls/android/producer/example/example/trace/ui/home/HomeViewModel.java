package com.aliyun.sls.android.producer.example.example.trace.ui.home;

import java.util.List;

import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListViewModel;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class HomeViewModel extends BaseListViewModel<ItemModel> {

    public HomeViewModel() {
        super("home");
    }

    @Override
    protected void fetchItemsFromServer() {
        ApiClient.getCategory(new ApiClient.ApiCallback<List<ItemModel>>() {
            @Override
            public void onSuccess(List<ItemModel> response) {
                items.setValue(response);
                status.setValue(Status.success());
            }

            @Override
            public void onError(int code, String error) {
                status.setValue(Status.error(String.valueOf(code), error));
            }
        });
    }
}