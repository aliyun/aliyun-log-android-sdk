package com.aliyun.sls.android.producer.example.example.trace.http;

import android.os.Handler;
import android.os.Looper;

import com.aliyun.sls.android.JsonUtil;
import com.aliyun.sls.android.plugin.trace.SLSTracePlugin;
import com.aliyun.sls.android.producer.example.example.trace.model.CartItemModel;
import com.aliyun.sls.android.producer.example.example.trace.model.ErrorModel;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.utils.ThreadUtils;

import org.json.JSONObject;

import java.util.List;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class ApiClient {

    private static final String API_BASE = "http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com";
    private static final String API_ORDER_LIST = API_BASE + "/orders";

    private static Tracer tracer = SLSTracePlugin.getInstance().getTelemetrySdk().getTracer("ApiClient");
    private static Handler handler = new Handler(Looper.getMainLooper());

    public interface ApiCallback<RESULT> {
        void onSuccess(RESULT result);

        void onError(int code, String error);
    }


    public static void getCategory(ApiCallback<List<ItemModel>> callback) {
        Span span = tracer.spanBuilder("getCategory").startSpan();
        span.end();

        ThreadUtils.exec(() -> {
            HttpTool.Response response = HttpTool.get("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com/catalogue?size=10", Context.current().with(span));
            if (response.success()) {
                List<ItemModel> modelList = ItemModel.parseJSON(response.data);
                if (null != modelList) {
                    postInMainThread(() -> callback.onSuccess(modelList));
                    return;
                }
            }
            postError(response, callback);
        });
    }

    public static void getDetail(final String id, ApiCallback<ItemModel> callback) {
        final Context context = Context.current();
        ThreadUtils.exec(() -> {
            HttpTool.Response response = HttpTool.get("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com/catalogue/" + id, context);
            if (response.success()) {
                ItemModel model = ItemModel.fromJSON(response.data);
                if (null != model) {
                    postInMainThread(() -> callback.onSuccess(model));
                    return;
                }
            }

            postError(response, callback);
        });
    }

    public static void addToCart(final String id, ApiCallback<Boolean> callback) {
        ThreadUtils.exec(() -> {
            JSONObject parameters = new JSONObject();
            JsonUtil.putOpt(parameters, "id", id);
            HttpTool.Response response = HttpTool.post("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com/cart", null, parameters.toString());
            if (response.success()) {
                postInMainThread(() -> callback.onSuccess(true));
            }

            postError(response, callback);
        });
    }

    public static void getCart(ApiCallback<List<CartItemModel>> callback) {
        final Context context = Context.current();
        ThreadUtils.exec(() -> {
            HttpTool.Response response = HttpTool.get("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com/cart", context);
            if (response.success()) {
                List<CartItemModel> itemModelList = CartItemModel.parseJson(response.data);
                if (null != itemModelList) {
                    postInMainThread(() -> callback.onSuccess(itemModelList));
                    return;
                }
            }

            postError(response, callback);
        });
    }

    public static void getOrders(ApiCallback<List<CartItemModel>> callback) {
        final Context context = Context.current();
        ThreadUtils.exec(() -> {
            HttpTool.Response response = HttpTool.get(API_ORDER_LIST, context);
            if (response.success()) {
                List<CartItemModel> itemModelList = CartItemModel.parseJson(response.data);
                if (null != itemModelList) {
                    postInMainThread(() -> callback.onSuccess(itemModelList));
                    return;
                }
            }

            postError(response, callback);
        });
    }

    private static void postError(HttpTool.Response response, ApiCallback callback) {
        ErrorModel errorModel = ErrorModel.fromJSON(response.data);
        if (null != errorModel) {
            postInMainThread(() -> callback.onError(errorModel.code, errorModel.error));
            return;
        }

        postInMainThread(() -> callback.onError(response.code, response.error));
    }

    private static void postInMainThread(Runnable r) {
        handler.post(r);
    }
}
