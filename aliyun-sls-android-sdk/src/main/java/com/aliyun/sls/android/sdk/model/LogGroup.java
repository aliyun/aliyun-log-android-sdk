package com.aliyun.sls.android.sdk.model;

import java.util.*;

/**
 * Created by wangjwchn on 16/8/2.
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.sls.android.sdk.model.Log;

public class LogGroup {
    private String mTopic = "";
    private String mSource = "";
    private LogTag mTag = new LogTag();
    protected List<Log> mContent = new ArrayList<Log>();

    public LogGroup() {
    }

    public LogGroup(String topic, String source) {
        mTopic = topic;
        mSource = source;
    }

    public LogGroup(String topic, String source, LogTag tag) {
        mTopic = topic;
        mSource = source;
        this.mTag = tag;
    }

    public void PutTopic(String topic) {
        mTopic = topic;
    }

    public void PutSource(String source) {
        mSource = source;
    }

    public void PutLog(Log log) {
        mContent.add(log);
    }

    public void PutTag(String key,String value) {
        mTag.PutContent(key, value);
    }

    public String LogGroupToJsonString() {
        JSONObject json_log_group = new JSONObject();
        json_log_group.put("__source__", mSource);
        json_log_group.put("__topic__", mTopic);
        JSONArray log_arrays = new JSONArray();

        for (Log log : mContent) {
            Map<String, Object> map = log.GetContent();
            JSONObject json_log = new JSONObject(map);
            log_arrays.add(json_log);
        }
        json_log_group.put("__logs__", log_arrays);
        Map<String, Object> map = mTag.GetContent();
        if (!map.isEmpty()){
            JSONObject json_log = new JSONObject(map);
            json_log_group.put("__tags__", json_log);
        }
        String s = json_log_group.toJSONString();
        return s;
    }

}
