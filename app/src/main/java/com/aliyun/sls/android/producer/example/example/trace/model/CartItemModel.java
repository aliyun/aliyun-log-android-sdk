package com.aliyun.sls.android.producer.example.example.trace.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gordon
 * @date 2021/09/06
 */
public class CartItemModel {

    public String id;
    public String itemId;
    public int quantity;
    public int unitPrice;

    public static List<CartItemModel> parseJson(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        JSONArray array;
        try {
            array = new JSONArray(json);
        } catch (JSONException e) {
            return null;
        }

        List<CartItemModel> itemModelList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            CartItemModel itemModel = CartItemModel.fromJson(array.optJSONObject(i).toString());
            if (null != itemModel) {
                itemModelList.add(itemModel);
            }
        }

        return itemModelList;
    }

    public static CartItemModel fromJson(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        JSONObject object;
        try {
            object = new JSONObject(json);
        } catch (JSONException e) {
            return null;
        }

        CartItemModel itemModel = new CartItemModel();
        itemModel.id = object.optString("id");
        itemModel.itemId = object.optString("itemId");
        itemModel.quantity = object.optInt("quantity");
        itemModel.unitPrice = object.optInt("unitPrice") * 100;

        return itemModel;
    }
}
