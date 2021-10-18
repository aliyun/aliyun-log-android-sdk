package com.aliyun.sls.android.producer.example.example.trace.ui.detail;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aliyun.sls.android.producer.example.example.trace.core.TraceViewModel;
import com.aliyun.sls.android.producer.example.example.trace.http.ApiClient;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;

import io.opentelemetry.api.trace.Span;

public class DetailViewModel extends TraceViewModel {

    private MutableLiveData<ItemModel> itemModelMutableLiveData;

    public DetailViewModel() {
        super("DetailViewModel");
        this.itemModelMutableLiveData = new MutableLiveData<>();
    }

    public LiveData<ItemModel> getItemModel() {
        return this.itemModelMutableLiveData;
    }

    public void requestData(final String id) {
        tracer.spanBuilder("requestDetailData").startSpan().end();

        ApiClient.getDetail(id, new ApiClient.ApiCallback<ItemModel>() {
            @Override
            public void onSuccess(ItemModel itemModel) {
                itemModelMutableLiveData.setValue(itemModel);
            }

            @Override
            public void onError(int code, String error) {

            }
        });
    }

    public void addToCart(Context context, final String id) {
        ApiClient.addToCart(id, new ApiClient.ApiCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                Toast.makeText(context, "加购成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int code, String error) {
                Toast.makeText(context, "加购失败", Toast.LENGTH_LONG).show();
            }
        });
    }
}