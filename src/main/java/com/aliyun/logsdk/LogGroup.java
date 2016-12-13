package com.aliyun.logsdk;
import java.util.*;

/**
 * Created by wangjwchn on 16/8/2.
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class LogGroup {
    private String mTopic = "";
    private String mSource = "";
    private List<Map<String,Object>> mContent = new ArrayList<Map<String,Object>>();

    public LogGroup()
    {
    }
    public LogGroup(String topic,String source){
        mTopic = topic;
        mSource = source;
    }
    public void PutTopic(String topic){
        mTopic = topic;
    }
    public void PutSource(String source){
        mSource = source;
    }
    public void PutLog(Log log){
        mContent.add(log.GetContent());
    }

    public String LogGroupToJsonString(){
       JSONObject json_log_group = new JSONObject();
        json_log_group.put("__source__", mSource);
        json_log_group.put("__topic__", mTopic);
        JSONArray log_arrays = new JSONArray();

        for(Map<String,Object> obj:mContent) {
            Map<String, Object> map = (Map<String, Object>) obj;
            JSONObject json_log = new JSONObject(map);
            log_arrays.add(json_log);
        }
        json_log_group.put("__logs__",log_arrays );
        String s = json_log_group.toJSONString();
        return s;
    }

}
