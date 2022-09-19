package com.aliyun.sls.android.producer.example.example.trace.core;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
//import com.aliyun.sls.android.plugin.trace.SLSTelemetry;
//import com.aliyun.sls.android.plugin.trace.SLSTracePlugin;
import com.aliyun.sls.android.producer.example.example.trace.ui.core.list.BaseListViewModel;
//import io.opentelemetry.api.trace.Tracer;

public class TraceViewModel extends ViewModel {
    //protected SLSTelemetry slsTelemetry = SLSTracePlugin.getInstance().getSLSTelemetry();
    //protected Tracer tracer;
    protected String modelName;

    protected MutableLiveData<BaseListViewModel.Status> status;

    public TraceViewModel(String modelName) {
        this.modelName  = modelName;
        //this.tracer = slsTelemetry.getTracer(modelName);
        this.status = new MutableLiveData<>();
    }

    public MutableLiveData<BaseListViewModel.Status> getStatus() {
        return status;
    }

    //public Tracer getTracer() {
    //    return tracer;
    //}

    public String getModelName() {
        return modelName;
    }

    public String generatorSpanName(String name) {
        return  String.format("%s_%s", modelName, name);
    }

}
