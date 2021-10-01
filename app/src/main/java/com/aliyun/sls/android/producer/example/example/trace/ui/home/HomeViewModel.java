package com.aliyun.sls.android.producer.example.example.trace.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aliyun.sls.android.producer.example.example.trace.core.TraceViewModel;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;

import java.util.ArrayList;
import java.util.List;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class HomeViewModel extends TraceViewModel {

    private MutableLiveData<List<ItemModel>> listData;

    public HomeViewModel() {
        super("HomeViewModel");
        listData = new MutableLiveData<>(new ArrayList<ItemModel>());
    }

    public void requestData() {
        Span span = tracer.spanBuilder("requestHomeData").startSpan();
        span.end();

        ApiClient.getCategory(new ApiClient.ApiCallback<List<ItemModel>>() {
            @Override
            public void onSuccess(List<ItemModel> response) {
                listData.setValue(response);
            }

            @Override
            public void onError(int code, String error) {

            }
        });
    }

    public LiveData<List<ItemModel>> getList() {
        return listData;
    }

}