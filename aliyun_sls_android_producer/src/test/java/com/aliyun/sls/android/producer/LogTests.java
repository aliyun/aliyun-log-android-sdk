package com.aliyun.sls.android.producer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.aliyun.sls.testable.Utils.put;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author gordon
 * @date 2022/10/17
 */
@ThreadCount(100)
@RunWith(ConcurrentTestRunner.class)
public class LogTests {
    //private LogProducerConfig config;
    private LogProducerClient client;
    private Log log;

    @Before
    public void setup() {
        //this.config = mock(LogProducerConfig.class);
        this.client = mock(LogProducerClient.class);
        this.log = new Log();

        //LogProducerConfig config = new LogProducerConfig();
        //this.config = spy(config);
        //when(config.createLogProducerConfig()).thenReturn(1L);
        //
        //LogProducerClient client = new LogProducerClient(this.config);
        //this.client = spy(client);
    }

    @After
    public void destroy() {
        //client.destroyLogProducer();
    }

    @Test
    public void log_putContent_put_boolValue() {
        Log log = new Log();
        log.putContent("bool", true);
        assertThat(log.getContent()).containsEntry("bool", "true");
    }

    @Test
    public void log_putContent_put_floatValue() {
        Log log = new Log();
        log.putContent("float", 12.34f);
        assertThat(log.getContent()).containsEntry("float", "12.34");
    }

    @Test
    public void log_putContent_put_doubleValue() {
        Log log = new Log();
        log.putContent("double", 45.45d);
        assertThat(log.getContent()).containsEntry("double", "45.45");
    }

    @Test
    public void log_putContent_put_stringValue() {
        Log log = new Log();
        log.putContent("string", "abc");
        assertThat(log.getContent().get("string")).isEqualTo("abc");
    }

    @Test
    public void log_putContent_put_intValue() {
        Log log = new Log();
        log.putContent("int", 11);
        assertThat(log.getContent().get("int")).isEqualTo("11");
    }

    @Test
    public void log_putContent_put_longValue() {
        Log log = new Log();
        log.putContent("long", 11L);
        assertThat(log.getContent().get("long")).isEqualTo("11");
    }

    @Test
    public void log_putContent_put_jsonValue_withKey() {
        Log log = new Log();
        JSONObject object = new JSONObject();
        put(object, "intKey", 111);
        put(object, "stringKey", "test123");

        log.putContent("json", object);
        assertThat(log.getContent()).containsKey("json").containsValue(object.toString());
    }


    @Test
    public void log_putContent_put_jsonValue_withKey_withNull() {
        Log log = new Log();
        log.putContent("json", (JSONObject)null);
        assertThat(log.getContent()).containsEntry("json", "null");
    }

    @Test
    public void log_putContent_put_jsonValue() {
        Log log = new Log();
        JSONObject object = new JSONObject();
        put(object, "intKey", 111);
        put(object, "stringKey", "test123");

        log.putContent(object);
        assertThat(log.getContent()).containsEntry("intKey", "111").containsEntry("stringKey", "test123");
    }

    @Test
    public void log_putContent_put_jsonValue_withNull() {
        Log log = new Log();

        log.putContent(null);
        assertThat(log.getContent()).isEmpty();
    }

    @Test
    public void log_putContent_put_arrayValue() {
        Log log = new Log();
        JSONArray array = new JSONArray();
        array.put("a array element");
        log.putContent("json", array);
        assertThat(log.getContent()).containsEntry("json", array.toString());
    }

    @Test
    public void log_putContent_put_arrayValue_withNull() {
        Log log = new Log();
        log.putContent("json", (JSONArray)null);
        assertThat(log.getContent()).containsEntry("json", "null");
    }

    @Test
    public void log_putContents_put_mapValue() {
        Log log = new Log();
        Map<String, String> mapValue = new HashMap<String, String>() {
            {
                put("key", "value");
                put("key2", "value2");
            }
        };
        log.putContents(mapValue);
        assertThat(log.getContent()).containsEntry("key", "value").containsEntry("key2", "value2");
    }

    @Test
    public void log_putContents_put_mapValue_withNull() {
        Log log = new Log();
        log.putContents(null);
        assertThat(log.getContent()).isEmpty();
    }

    @Test
    public void log_numberToString_Correct() {
        assertThat(new Log().numberToString(111)).isEqualTo("111");
    }

    @Test
    public void log_numberToString_Incorrect() {
        assertThat(new Log().numberToString(null)).isEqualTo("");
    }

    @Test
    public void log_hasLogTime_setLogTime() {
        Log log = new Log();
        log.setLogTime(123456789);
        assertThat(log.getLogTime()).isEqualTo(123456789);
    }

    @Test
    public void log_add_concurrent() throws Throwable {
        Log log = new Log();
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<?> addFuture =
            es.submit(() -> {
                for (int i = 0; i < 10000 * 5; i++) {
                    log.putContent("key" + i, i);
                }
            });

        try {
            for (int i = 0; i < 10000 * 5; i++) {
                log.getContent();
            }
        } catch (Throwable t) {
            addFuture.cancel(true);
            throw t;
        }

        addFuture.get();
    }

    //@Test
    //public void log_multi_add() {
    //    System.out.println("thread: " + Thread.currentThread().getName());
    //    Log log = oneLog();
    //
    //    //doCallRealMethod().when(client).addLog(log, 0);
    //    //doCallRealMethod().when(client).addLog(log);
    //    Map<String, String> contents = log.getContent();
    //    when(client.addLogInternal(contents)).thenCallRealMethod();
    //    //when(client.addLogInternal(log, 0)).thenCallRealMethod();
    //
    //    //client.addLogInternal(log, 0);
    //    String[][] results = client.addLogInternal(contents);
    //    int expectedCount = log.getContent().size();
    //    int actualCount = (results != null && results[0] != null) ? results[0].length : -1;
    //    assertEquals(expectedCount, actualCount);
    //    //verify(client).addLog(log);
    //}

    private Log oneLog() {
        Log log = new Log();
        // multi-type input content
        log.putContent("1int", 11);
        log.putContent("1long", 12L);
        log.putContent("1float", 13.0f);
        log.putContent("1double", 14.44d);
        log.putContent("1boolean", true);
        log.putContent("1string", "string");
        JSONObject object = new JSONObject();
        try {
            object.put("int", 22);
            object.put("long", 32L);
            object.put("float", 33.0f);
            object.put("double", 43.33d);
            object.put("string", "string");
            object.put("boolean", true);
            JSONObject nest = new JSONObject();
            nest.put("kkk", "value");
            object.put("nest", nest);
            object.put("array", new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        log.putContent(object);
        log.putContent("json", object);
        log.putContent("array", new JSONArray());

        // common input content
        log.putContent("content_key_1",
            "1abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()"
                + "_+abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_2", "2abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_3", "3abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_4", "4abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_5", "5abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_6", "6abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_7", "7abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_8", "8abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("content_key_9", "9abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+");
        log.putContent("random", String.valueOf(Math.random()));
        log.putContent("content", "中文️");
        log.putContent(null, "null");
        log.putContent("null", (String)null);
        return log;
    }
}
