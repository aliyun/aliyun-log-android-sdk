package com.aliyun.sls.android.producer.example.example.trace.ui.core.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.aliyun.sls.android.producer.example.example.trace.core.TraceViewModel;

import java.util.ArrayList;
import java.util.List;

import io.opentelemetry.api.trace.Span;

/**
 * @author gordon
 * @date 2021/10/17
 */
public abstract class BaseListViewModel<ITEM> extends TraceViewModel {

    private MutableLiveData<List<ITEM>> items;

    public BaseListViewModel(String modelName) {
        super(modelName);
        items = new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<ITEM>> getItems() {
        return items;
    }

    public void requestItemsFromServer() {
        Span span = tracer.spanBuilder(String.format("request_%s_items_from_server", modelName)).startSpan();
        span.end();

        fetchItemsFromServer();
    }

    protected abstract void fetchItemsFromServer();

}
