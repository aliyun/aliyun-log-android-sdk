# aliyun-log-android-sdk
aliyun日志服务Android SDK.

# Sample
```
      LOGClient myClient = new LOGClient("$aliyun_log_endpoint", "$your_access_key_id",
                "$your_access_key", "$projec_name");
        myClient.SetToken(""); //
        /* 创建logGroup */
        final LogGroup logGroup = new LogGroup("", "$host_ip");

        /* 存入一条log */
        Log log = new Log();
        log.PutContent("key_1", "value_1");
        log.PutContent("key_2", "value_2");

        logGroup.PutLog(log);

        /* 发送log */
        myClient.PostLog(logGroup, "$log_store");
       
```

# A sample for CachedLogGroup
```
CachedLogGroup logGroupWrapper = new CachedLogGroup("$topic", "$source");
ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
Runnable ioLoop = new Runnable() {
    @Override
    public void run() {
        LogGroup logGroup = null;
        try {
            logGroup = logGroupWrapper.takeOneLogGroup(100);
            if (logGroup != null) {
                client.PostLog(logGroup, "android-sdk-ack");
                android.util.Log.d(TAG,"send log ok");
            }
        } catch (LogException ex) {
            ex.printStackTrace();
            logGroupWrapper.addLogGroup(logGroup);
        }
    }
};
service.scheduleAtFixedRate(ioLoop, InitDelay , Period, TimeUnit.MILLISECONDS);

/* 存入一条log */
Log log = new Log();
log.PutContent("key_1", "value_1");
log.PutContent("key_2", "value_2");
logGroupWrapper.PutLog(log);
```

# Maven
```
<dependency>
  <groupId>com.aliyun.openservices</groupId>
  <artifactId>aliyun-log-android-sdk</artifactId>
  <version>0.3.1</version>
</dependency>

```
