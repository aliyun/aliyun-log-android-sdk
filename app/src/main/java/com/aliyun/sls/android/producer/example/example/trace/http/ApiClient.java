package com.aliyun.sls.android.producer.example.example.trace.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import com.aliyun.sls.android.JsonUtil;
import com.aliyun.sls.android.producer.example.example.trace.model.CartItemModel;
import com.aliyun.sls.android.producer.example.example.trace.model.ErrorModel;
import com.aliyun.sls.android.producer.example.example.trace.model.ItemModel;
import com.aliyun.sls.android.producer.example.example.trace.model.OrderModel;
import com.aliyun.sls.android.producer.example.example.trace.model.UserModel;
import com.aliyun.sls.android.producer.utils.ThreadUtils;

import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class ApiClient {

    private static final String API_BASE = "http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com";

    private static final String API_CATEGORY = "/catalogue";
    private static final String API_CART = "/cart";
    private static final String API_ORDER_LIST = "/orders";
    private static final String API_USER_LOGIN = "/login";
    private static final String API_USER_CUSTOMER = "/customers";
    private static final String API_ORDER_CREATE = "/orders";

    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    public interface ApiCallback<RESULT> {
        void onSuccess(RESULT result);

        void onError(int code, String error);
    }

    public static void getCategory(ApiCallback<List<ItemModel>> callback) {
        HttpTool.get(API_BASE, API_CATEGORY + "?size=10", response -> {
            if (response.success()) {
                List<ItemModel> modelList = ItemModel.fromJSONArray(response.data);
                if (null != modelList) {
                    postInMainThread(() -> callback.onSuccess(modelList));
                    return;
                }
            }
            postError(response, callback);
        });
    }

    public static void getDetail(final String id, ApiCallback<ItemModel> callback) {
        HttpTool.get(API_BASE, API_CATEGORY + "/" + id, response -> {
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
        JSONObject parameters = new JSONObject();
        JsonUtil.putOpt(parameters, "id", id);
        HttpTool.post(API_BASE, API_CART, parameters.toString(), response -> {
            if (response.success()) {
                postInMainThread(() -> callback.onSuccess(true));
                return;
            }

            postError(response, callback);
        });
    }

    public static void getCart(ApiCallback<List<CartItemModel>> callback) {
        HttpTool.get(API_BASE, API_CART, response -> {
            if (response.success()) {
                List<CartItemModel> itemModelList = CartItemModel.fromJSONArray(response.data);
                if (null != itemModelList) {
                    postInMainThread(() -> callback.onSuccess(itemModelList));
                    return;
                }
            }

            postError(response, callback);
        });
    }

    public static void getOrders(ApiCallback<List<OrderModel>> callback) {
        HttpTool.get(API_BASE, API_ORDER_LIST, response -> {
            if (response.success()) {
                List<OrderModel> itemModelList = OrderModel.fromJSONArray(response.data);
                if (null != itemModelList) {
                    postInMainThread(() -> callback.onSuccess(itemModelList));
                    return;
                }
            }

            postError(response, callback);
        });
    }

    public static void login(String userName, String password, ApiCallback<String> callback) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + base64(userName + ":" + password));

        HttpTool.get(API_BASE, API_USER_LOGIN, headers, response -> {
            if (response.success()) {
                final String loginId = Utils.getLoginId(response.headers);
                SLSCookieManager.setCookie(response.headers);
                postInMainThread(() -> callback.onSuccess(loginId));
            } else {
                postError(response, callback);
            }
        });
    }

    public static void getCustomerInfo(String loginId, ApiCallback<UserModel> callback) {
        HttpTool.get(API_BASE, API_USER_CUSTOMER + "/" + loginId, response -> {
            if (response.success()) {
                UserModel model = UserModel.fromJSON(response.data);
                if (null != model) {
                    postInMainThread(() -> callback.onSuccess(model));
                    return;
                }
            }

            postError(response, callback);
        });
    }

    public static void createOrder(ApiCallback<Boolean> callback) {
        ThreadUtils.exec(() -> {
            HttpTool.get(API_BASE, API_ORDER_CREATE, response -> {
                if (response.success()) {
                    postInMainThread(() -> callback.onSuccess(true));
                    return;
                }

                postError(response, callback);
            });
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
        HANDLER.post(r);
    }

    private static String base64(String content) {
        return Base64.encodeToString(content.getBytes(Charset.forName("UTF-8")), Base64.DEFAULT);
    }
}
