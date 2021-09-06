package com.aliyun.sls.android.producer.example.example.trace.http;

import android.os.Handler;
import android.os.Looper;

import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.utils.ThreadUtils;

import java.util.List;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class ApiClient {

    private static Handler handler = new Handler(Looper.getMainLooper());

    public interface ApiCallback<RESULT> {
        void onSuccess(RESULT result);

        void onError(int code, String error);
    }


    public static void getCategory(ApiCallback<List<ItemModel>> callback) {
        ThreadUtils.exec(() -> {
            HttpTool.Response response = HttpTool.get("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com/catalogue?size=5");
            if (response.success()) {
                List<ItemModel> modelList = ItemModel.parseJSON(response.data);
                if (null != modelList) {
                    postInMainThread(() -> callback.onSuccess(modelList));
                    return;
                }

                postInMainThread(() -> callback.onError(400, "json parser error"));
            }

            postInMainThread(() -> callback.onError(response.code, response.error));
        });
    }

    private static void postInMainThread(Runnable r) {
        handler.post(r);
    }
}
