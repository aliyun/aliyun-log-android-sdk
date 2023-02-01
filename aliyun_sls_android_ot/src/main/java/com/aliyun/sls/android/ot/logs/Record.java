package com.aliyun.sls.android.ot.logs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2023/2/1
 */
public class Record {
    protected Long timeUnixNano;
    protected String severityNumber;
    protected String severityText;
    protected Body body;
    protected List<Attribute> attributes;
    protected String traceId;
    protected String spanId;

    public Record() {
        this.body = new Body();
        this.attributes = new ArrayList<>();
    }

    public Record addAttribute(Attribute... attributes) {
        if (null == attributes) {
            return this;
        }

        this.attributes.addAll(Arrays.asList(attributes));
        return this;
    }

    public Record addAttribute(List<Attribute> attributes) {
        if (null == attributes) {
            return this;
        }

        this.attributes.addAll(attributes);
        return this;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        JSONUtils.put(object, "timeUnixNano", timeUnixNano / 1000);
        JSONUtils.put(object, "severityNumber", severityNumber);
        JSONUtils.put(object, "severityText", severityText);

        JSONObject body = new JSONObject();
        JSONUtils.put(body, "stringValue", this.body.stringValue);
        JSONUtils.put(object, "body", body);

        JSONUtils.put(object, "severityText", severityText);
        JSONUtils.put(object, "attributes", Attribute.toJsonArray(this.attributes));
        JSONUtils.put(object, "traceId", traceId);
        JSONUtils.put(object, "spanId", spanId);

        return object;
    }

    public static JSONArray toJson(Record... records) {
        if (null == records) {
            return null;
        }

        return toJson(Arrays.asList(records));
    }

    public static JSONArray toJson(List<Record> records) {
        if (null == records) {
            return null;
        }

        JSONArray array = new JSONArray();
        for (Record record : records) {
            array.put(record.toJson());
        }
        return array;
    }

    public static class Body {
        protected String stringValue;

        public String getStringValue() {
            return stringValue;
        }

        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }
    }
}
