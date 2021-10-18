package com.aliyun.sls.android.producer.example.example.trace.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author gordon
 * @date 2021/10/18
 */
public class OrderModel {
    public String id;
    public String customerId;
    public String date;
    public String shipment;
    public String status;
    public long total;

    public List<CartItemModel> items;

    public static OrderModel fromJSON(String json) {
        try {
            JSONObject object = new JSONObject(json);

            OrderModel model = new OrderModel();
            final String href = object.optJSONObject("_links").optJSONObject("self").optString("href");
            model.id = href.substring(href.lastIndexOf("/") + 1);
            model.customerId = object.optString("customerId");
            model.date = convertTime(object.optString("date"));
            model.shipment = object.optString("shipment");
            model.status = object.optString("status");
            model.total = object.optLong("total");

            if (object.has("items")) {
                model.items = CartItemModel.fromJSONArray(object.optJSONArray("items").toString());
            }

            return model;
        } catch (JSONException e) {
            return null;
        }
    }

    public static List<OrderModel> fromJSONArray(String jsonArray) {
        JSONArray array;
        try {
            array = new JSONArray(jsonArray);
        } catch (JSONException e) {
            return null;
        }

        List<OrderModel> models = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            OrderModel model = OrderModel.fromJSON(array.optJSONObject(i).toString());
            if (null != model) {
                models.add(model);
            }
        }

        return models;

    }

    public String getItemId() {
        if (null == items || items.size() == 0) {
            return null;
        }

        for (CartItemModel item : items) {
            if (TextUtils.isEmpty(item.itemId)) {
                continue;
            }
            return item.itemId;
        }

        return null;
    }

    private static String convertTime(String time) {
        // 2021-10-17T16:12:24.671+0000
        DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        try {
            Date date = dtf.parse(time);
            dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            return dtf.format(date);
        } catch (ParseException e) {
            return time;
        }
    }
}
