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

# Maven
```
<dependency>
  <groupId>com.aliyun.openservices</groupId>
  <artifactId>aliyun-log-android-sdk</artifactId>
  <version>0.3.0</version>
</dependency>

```
