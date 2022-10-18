//package com.aliyun.sls.android.producer;
//
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
//import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
///**
// * @author gordon
// * @date 2022/10/18
// */
//@RunWith(ConcurrentTestRunner.class)
////@RunWith(AndroidJUnit4.class)
//@ThreadCount(1)
//public class LogTest {
//    private LogProducerConfig config;
//    private LogProducerClient client;
//
//    @Before
//    public void setup() throws Throwable{
//        config = new LogProducerConfig();
//        client = new LogProducerClient(config);
//    }
//
//    @After
//    public void destroy() {
//        client.destroyLogProducer();
//    }
//
//    @Test
//    public void log_multi_add() {
//        System.err.println("thread: " + Thread.currentThread().getName());
//        Log log = oneLog();
//        client.addLog(log);
//    }
//
//    private Log oneLog() {
//        Log log = new Log();
//        // multi-type input content
//        log.putContent("1int", 11);
//        log.putContent("1long", 12L);
//        log.putContent("1float", 13.0f);
//        log.putContent("1double", 14.44d);
//        log.putContent("1boolean", true);
//        log.putContent("1string", "string");
//        JSONObject object = new JSONObject();
//        try {
//            object.put("int", 22);
//            object.put("long", 32L);
//            object.put("float", 33.0f);
//            object.put("double", 43.33d);
//            object.put("string", "string");
//            object.put("boolean", true);
//            JSONObject nest = new JSONObject();
//            nest.put("kkk", "value");
//            object.put("nest", nest);
//            object.put("array", new JSONArray());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        log.putContent(object);
//        log.putContent("json", object);
//        log.putContent("array", new JSONArray());
//
//        // common input content
//        log.putContent("content_key_1", "1abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
//        log.putContent("content_key_2", "2abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
//        log.putContent("content_key_3", "3abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
//        log.putContent("content_key_4", "4abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
//        log.putContent("content_key_5", "5abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
//        log.putContent("content_key_6", "6abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
//        log.putContent("content_key_7", "7abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
//        log.putContent("content_key_8", "8abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
//        log.putContent("content_key_9", "9abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
//        log.putContent("random", String.valueOf(Math.random()));
//        log.putContent("content", "中文️");
//        log.putContent(null, "null");
//        log.putContent("null", (String) null);
//        return log;
//    }
//}