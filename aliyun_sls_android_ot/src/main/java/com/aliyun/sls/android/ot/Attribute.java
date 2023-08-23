package com.aliyun.sls.android.ot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Pair;
import com.aliyun.sls.android.ot.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2022/3/31
 */
public class Attribute implements Comparable<Attribute> {
    public final String key;
    public final Object value;

    private Attribute(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    // region instance
    public static Attribute of(String key, boolean value) {
        return of(key, (Object)value);
    }

    public static Attribute of(String key, int value) {
        return of(key, (Object)value);
    }

    public static Attribute of(String key, long value) {
        return of(key, (Object)value);
    }

    public static Attribute of(String key, double value) {
        return of(key, (Object)value);
    }

    public static Attribute of(String key, String value) {
        return of(key, (Object)value);
    }

    public static Attribute of(final String key, final Object value) {
        final String k = null == key ? "null" : key;
        final String v = null == value ? "null" : value.toString();
        return new Attribute(k, v);
    }

    @SafeVarargs
    public static List<Attribute> of(Pair<String, Object>... kvs) {
        if (null == kvs) {
            return null;
        }

        List<Attribute> attributeList = new ArrayList<>();
        for (Pair<String, Object> kv : kvs) {
            attributeList.add(of(kv.first, kv.second));
        }
        return attributeList;
    }
    // endregion

    @Override
    public int compareTo(Attribute o) {
        return this.key.compareTo(o.key);
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        JSONUtils.put(object, this.key, this.value);
        return object;
    }

    public static JSONObject toJson(List<Attribute> attributes) {
        if (null == attributes) {
            return null;
        }

        Collections.sort(attributes);

        JSONObject object = new JSONObject();
        for (Attribute attribute : attributes) {
            JSONUtils.put(object, attribute.key, attribute.value);
        }
        return object;
    }

    public static JSONArray toJsonArray(List<Attribute> attributes) {
        if (null == attributes) {
            return null;
        }

        Collections.sort(attributes);

        JSONArray array = new JSONArray();
        JSONObject object;
        for (Attribute attribute : attributes) {
            object = new JSONObject();
            JSONUtils.put(object, "key", attribute.key);
            JSONUtils.put(object, "value", JSONUtils.object(Pair.create("stringValue", attribute.value)));
            array.put(object);
        }

        return array;
    }
}
