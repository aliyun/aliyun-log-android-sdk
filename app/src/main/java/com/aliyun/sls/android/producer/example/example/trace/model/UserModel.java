package com.aliyun.sls.android.producer.example.example.trace.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class UserModel {

    public String id;
    public String firstName;
    public String lastName;
    public String username;

    public static UserModel fromJson(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        UserModel model = new UserModel();
        JSONObject object = null;
        try {
            object = new JSONObject(json);
        } catch (JSONException e) {
            return null;
        }

        model.id = object.optString("id");
        model.firstName = object.optString("firstName");
        model.lastName = object.optString("lastName");
        model.username = object.optString("username");

        return model;
    }
}
