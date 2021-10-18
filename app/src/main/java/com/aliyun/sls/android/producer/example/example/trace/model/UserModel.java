package com.aliyun.sls.android.producer.example.example.trace.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2021/10/17
 */
public class UserModel {
    public String firstName;
    public String id;
    public String lastName;
    public String username;

    public static UserModel fromJSON(String json) {
        try {
            JSONObject object = new JSONObject(json);
            if (!object.has("id")) {
                return null;
            }

            UserModel model = new UserModel();
            model.firstName = object.optString("firstName");
            model.id = object.optString("id");
            model.lastName = object.optString("lastName");
            model.username = object.optString("username");
            return model;

        } catch (JSONException e) {
            return null;
        }
    }
}
