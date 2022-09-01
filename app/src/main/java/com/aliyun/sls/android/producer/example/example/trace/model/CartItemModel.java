package com.aliyun.sls.android.producer.example.example.trace.model;

import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2021/09/06
 */
public class CartItemModel {

    public String id;
    public String itemId;
    public int quantity;
    public int unitPrice;

    @Override
    public String toString() {
        return "CartItemModel{" +
                "id='" + id + '\'' +
                ", itemId='" + itemId + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }

    public static List<CartItemModel> fromJSONArray(String json) {
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
            CartItemModel itemModel = CartItemModel.fromJSON(array.optJSONObject(i).toString());
            if (null != itemModel) {
                itemModelList.add(itemModel);
            }
        }

        return itemModelList;
    }

    public static CartItemModel fromJSON(String json) {
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
