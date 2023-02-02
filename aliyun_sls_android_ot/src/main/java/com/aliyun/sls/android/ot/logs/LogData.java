package com.aliyun.sls.android.ot.logs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.Resource;
import com.aliyun.sls.android.ot.utils.JSONUtils;
import com.aliyun.sls.android.ot.utils.TimeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2023/2/1
 */
public class LogData {
    protected Resource resource;
    protected Scope scope;
    protected List<Record> logRecords;

    LogData() {
        this.resource = Resource.getDefault();
        this.scope = new Scope();
        this.logRecords = new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Resource getResource() {
        return resource;
    }

    public LogData setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public Scope getScope() {
        return this.scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public LogData addRecord(Record... records) {
        if (null == records) {
            return this;
        }

        this.logRecords.addAll(Arrays.asList(records));
        return this;
    }

    public LogData addRecord(List<Record> records) {
        if (null == records) {
            return this;
        }

        this.logRecords.addAll(records);
        return this;
    }

    public List<Record> getLogRecords() {
        return logRecords;
    }

    public JSONObject toJson() {
        JSONObject data = new JSONObject();
        JSONUtils.put(data, "resource", null != this.resource ? this.resource.toJson() : new JSONObject());

        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        JSONUtils.put(object, "scope", this.scope.toJson());
        JSONUtils.put(object, "logRecords", Record.toJson(this.logRecords));
        array.put(object);

        JSONUtils.put(data, "scopeLogs", array);
        return data;
    }

    public static class Builder {
        private Resource resource;
        private LogLevel logLevel = LogLevel.ERROR;
        private String severityText = LogLevel.ERROR.name();
        private Scope scope;
        private Long epochNanos;
        private String traceId;
        private String spanId;
        private String logContent;
        private List<Attribute> attributes;

        public Builder() {
        }

        public Builder setResource(Resource resource) {
            this.resource = resource;
            return this;
        }

        public Builder setLogLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public Builder setSeverityText(String severityText) {
            this.severityText = severityText;
            return this;
        }

        public Builder setScope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public Builder setEpochNanos(Long epochNanos) {
            this.epochNanos = epochNanos;
            return this;
        }

        public Builder setTraceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder setSpanId(String spanId) {
            this.spanId = spanId;
            return this;
        }

        public Builder setLogContent(String content) {
            this.logContent = content;
            return this;
        }

        public Builder setAttribute(List<Attribute> attribute) {
            this.attributes = attribute;
            return this;
        }

        public LogData build() {
            LogData data = new LogData();
            data.setResource(this.resource);
            data.setScope(null != this.scope ? this.scope : new Scope());

            Record record = new Record();
            record.timeUnixNano = null != this.epochNanos ? this.epochNanos : TimeUtils.instance.now();
            LogLevel level = null != this.logLevel ? logLevel : LogLevel.ERROR;
            record.severityNumber = level.getSeverityNumber();
            record.severityText = !TextUtils.isEmpty(this.severityText) ? this.severityText : level.name();
            record.body.stringValue = this.logContent;
            if (null != this.attributes) {
                record.attributes.addAll(this.attributes);
            }
            record.traceId = this.traceId;
            record.spanId = this.spanId;

            data.logRecords.add(record);

            return data;
        }

    }
}
