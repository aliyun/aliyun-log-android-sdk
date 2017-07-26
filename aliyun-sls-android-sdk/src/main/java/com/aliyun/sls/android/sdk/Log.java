package com.aliyun.sls.android.sdk;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangjwchn on 16/8/2.
 */
public class Log {
    Map<String,Object> mContent=new HashMap<String,Object>();
    public Log(){
        mContent.put("__time__",new Long(System.currentTimeMillis()/1000).intValue());
    }
    public void PutTime(int time){
        mContent.put("__time__",time);
    }
    public void PutContent(String key,String value) {
        if (key == null || key.isEmpty())
        {
            return;
        }
        if (value == null)
        {
            mContent.put(key,"");
        }
        else
        {
            mContent.put(key,value);
        }
    }
    public  Map<String,Object> GetContent(){
        return mContent;
    }
}
