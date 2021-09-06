package com.aliyun.sls.android.producer.example.example.trace.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gordon
 * @date 2021/09/01
 */
public class ItemModel {
    public String id;
    public String name;
    public String description;
    public List<String> imageUrl;
    public long price;
    public int count;
    public List<String> tag;

    public static ItemModel fromJSON(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            ItemModel model = new ItemModel();
            JSONObject jsonObject = new JSONObject(json);
            model.id = jsonObject.optString("id");
            model.name = jsonObject.optString("name");
            model.description = jsonObject.optString("description");
            model.price = (long) (jsonObject.optDouble("price") * 100);
            model.count = jsonObject.optInt("count");

            JSONArray array = jsonObject.optJSONArray("imageUrl");
            if (null != array) {
                model.imageUrl = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    model.imageUrl.add("http://sls-mall.caa227ac081f24f1a8556f33d69b96c99.cn-beijing.alicontainer.com" + array.optString(i));
                }
            }

            array = jsonObject.optJSONArray("tag");
            if (null != array) {
                model.tag = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    model.tag.add(array.optString(i));
                }
            }

            return model;
        } catch (JSONException e) {
            return null;
        }
    }

    public static List<ItemModel> parseJSON(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            List<ItemModel> modelList = new ArrayList<>();
            JSONArray array = new JSONArray(json);

            JSONObject object;
            ItemModel model;
            for (int i = 0; i < array.length(); i++) {
                object = array.optJSONObject(i);
                model = ItemModel.fromJSON(object.toString());
                if (null != model) {
                    modelList.add(model);
                }
            }

            return modelList;
        } catch (JSONException e) {
            return null;
        }
    }

}
