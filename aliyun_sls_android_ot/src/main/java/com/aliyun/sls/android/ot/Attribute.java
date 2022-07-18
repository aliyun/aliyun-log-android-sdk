package com.aliyun.sls.android.ot;

import java.util.ArrayList;
import java.util.List;

import android.util.Pair;

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

    @Override
    public int compareTo(Attribute o) {
        return this.key.compareTo(o.key);
    }
}
