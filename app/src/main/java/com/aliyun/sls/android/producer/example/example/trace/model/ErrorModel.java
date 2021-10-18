package com.aliyun.sls.android.producer.example.example.trace.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2021/10/17
 */
public class ErrorModel {

    public String error;
    public int code;
    public String status;

    @Override
    public String toString() {
        return "ErrorModel{" +
                "error='" + error + '\'' +
                ", code=" + code +
                ", status='" + status + '\'' +
                '}';
    }

    public static ErrorModel fromJSON(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            JSONObject jsonObject = new JSONObject(json);
            if (!jsonObject.has("error")) {
                return null;
            }

            ErrorModel model = new ErrorModel();
            model.error = jsonObject.optString("error");
            model.code = jsonObject.optInt("status_code");
            model.status = jsonObject.optString("status_text");

            return model;
        } catch (JSONException e) {
            return null;
        }
    }

}
