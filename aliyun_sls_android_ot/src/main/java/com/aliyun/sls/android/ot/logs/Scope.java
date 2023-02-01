package com.aliyun.sls.android.ot.logs;

import java.util.LinkedList;
import java.util.List;

import com.aliyun.sls.android.ot.Attribute;
import com.aliyun.sls.android.ot.utils.JSONUtils;
import org.json.JSONObject;

/**
 * @author gordon
 * @date 2023/2/1
 */
public class Scope {
    protected String name;
    protected Integer version;
    protected List<Attribute> attributes;

    Scope() {
        this("log", 1, new LinkedList<>());
    }

    public Scope(String name, Integer version, List<Attribute> attributes) {
        this.name = name;
        this.version = version;
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        JSONUtils.put(object, "name", this.name);
        JSONUtils.put(object, "version", this.version);
        JSONUtils.put(object, "attributes", Attribute.toJson(attributes));
        return object;
    }
}
