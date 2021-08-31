# log service android producer

基于[aliyun-log-c-sdk](https://github.com/aliyun/aliyun-log-c-sdk/tree/persistent)
，提供了在Android平台上将日志采集到日志服务的接口。

## 功能特点

* 异步
    * 异步写入，客户端线程无阻塞
* 聚合&压缩 上传
    * 支持按超时时间、日志数、日志size聚合数据发送
    * 支持lz4压缩
* 多客户端
	* 可同时配置多个客户端，每个客户端可独立配置采集优先级、缓存上限、目的project/logstore、聚合参数等
* 缓存
    * 支持缓存上限可设置
    * 超过上限后日志写入失败
* 自定义标识
    * 支持设置自定义tag、topic
* 断点续传功能
    * 每次发送前会把日志保存到本地的binlog文件，只有发送成功才会删除，保证日志上传At Least Once（配置多个客户端时，不应设置相同持久化文件）
* 上下文
    * 采集上来的日志，支持查看某条日志的上下文

![image.png](https://test-lichao.oss-cn-hangzhou.aliyuncs.com/pic/099B6EC1-7305-4C18-A1CF-BA2CCD1FBDBC.png)

## 性能测试

* 开启断点续传

| 发送 条/每秒 | cpu占用 |  内存占用(MB) | 上传速(MB/min) |
| --- | --- | --- | --- |
| 1 | <1% | 49 | 0.046 |
| 10 | 2% | 53 | 0.442 |
| 100 | 8% | 57 | 4.393 |
| 200 | 10% | 70 | 7.90 |

* 不开启断点续传

| 发送 条/每秒 | cpu占用 |  内存占用(MB) | 上传速(MB/min) |
| --- | --- | --- | --- |
| 1 | <1% | 49 | 0.046 |
| 10 | 2% | 58 | 0.442 |
| 100 | 10% | 60 | 4.393 |
| 200 | 15% | 70 | 7.90 |

## SDK包体积

* aar包体积

![aar包体积.png](https://test-lichao.oss-cn-hangzhou.aliyuncs.com/pic/2020.12.4.4.png)

* 引用SDK包前后体积对比

![引用SDK包前后体积对比.png](https://test-lichao.oss-cn-hangzhou.aliyuncs.com/pic/2020.12.4.2.png)

## 配置说明

### 环境要求
- Android版本4.0(API 14)及以上。
- 必须注册有Aliyun.com用户账户，并开通相应的服务（LOG）。

### Gradle配置
```
jcenter()
implementation 'com.aliyun.openservices:aliyun-log-android-sdk:2.6.2'
```

### 混淆配置
```
-keep class com.aliyun.sls.android.producer.* { *; }
-keep interface com.aliyun.sls.android.producer.* { *; }
```

### Android权限

上传日志需要
```
<uses-permission android:name="android.permission.INTERNET" />
```

开启断点续传功能需要有持久化文件的读写权限

### 创建config


https://help.aliyun.com/document_detail/29064.html

```
// endpoint前需要加 https://
String endpoint = "project's_endpoint";
String project = "project_name";
String logstore = "logstore_name";
String accesskeyid = "your_accesskey_id";
String accesskeysecret = "your_accesskey_secret";
LogProducerConfig config = new LogProducerConfig(endpoint, project, logstore, accesskeyid, accesskeysecret);
// 指定sts token 创建config，过期之前调用resetSecurityToken重置token
// LogProducerConfig config = new LogProducerConfig(endpoint, project, logstore, accesskeyid, accesskeysecret, securityToken);
```

### 配置config
```
// 设置主题
config.setTopic("test_topic");
// 设置tag信息，此tag会附加在每条日志上
config.addTag("test", "test_topic");
// 每个缓存的日志包的大小上限，取值为1~5242880，单位为字节。默认为1024 * 1024
config.setPacketLogBytes(1024*1024);
// 每个缓存的日志包中包含日志数量的最大值，取值为1~4096，默认为1024
config.setPacketLogCount(1024);
// 被缓存日志的发送超时时间，如果缓存超时，则会被立即发送，单位为毫秒，默认为3000
config.setPacketTimeout(3000);
// 单个Producer Client实例可以使用的内存的上限，超出缓存时add_log接口会立即返回失败
// 默认为64 * 1024 * 1024
config.setMaxBufferLimit(64*1024*1024);
// 发送线程数，默认为1
config.setSendThreadCount(1);

// 1 开启断点续传功能， 0 关闭
// 每次发送前会把日志保存到本地的binlog文件，只有发送成功才会删除，保证日志上传At Least Once
config.setPersistent(0);
// 持久化的文件名，需要保证文件所在的文件夹已创建。配置多个客户端时，不应设置相同文件
config.setPersistentFilePath(getFilesDir() + "/log.dat");
// 是否每次AddLog强制刷新，高可靠性场景建议打开
config.setPersistentForceFlush(1);
// 持久化文件滚动个数，建议设置成10。
config.setPersistentMaxFileCount(10);
// 每个持久化文件的大小，建议设置成1-10M
config.setPersistentMaxFileSize(1024 * 1024);
// 本地最多缓存的日志数，不建议超过1M，通常设置为65536即可
config.setPersistentMaxLogCount(65536);
```

### 动态配置
2.6.0版本开始， sdk 新增 endpoint、project、logstore 参数的动态配置。

```
// 动态更新 endpoint
config.setEndpoint(endpoint);
// 动态更新 logproject
config.setProject(project);
// 动态更新 logstore 
config.setLogstore(logstore);
```

#### config其他参数设置说明

| 参数设置                   | 说明                                                                             | 取值                                                                |
| ------------------------- | ------------------------------------------------------------------------------- | ------------------------------------------------------------------ |
| setTopic                  | __topic__ 字段的值                                                               | 字符串，默认为空串                                                    |
| addTag                    | __tag__:xxxx                                                                    | 字符串，默认为空串                                                    |
| setSource                 | __source__ 字段的值                                                              | 字符串，默认为 Android                                                |
| setPacketLogBytes         | 每个缓存的日志包的大小上限                                                          | 整数，取值为1~5242880，单位为字节。默认为1024 * 1024                    |
| setPacketLogCount         | 每个缓存的日志包中包含日志数量的最大值                                                | 整数，取值为1~4096，默认为1024                                        |
| setPacketTimeout          | 被缓存日志的发送超时时间，如果缓存超时，则会被立即发送                                  | 整数，单位为毫秒，默认为3000                                           |
| setMaxBufferLimit         | 单个Producer Client实例可以使用的内存的上限，超出缓存时add_log接口会立即返回失败         | 整数，默认为1                                                        |
| setSendThreadCount        | 发送线程数                                                                        | 整数，64 * 1024 * 1024                                              |
| setPersistent             | 每次发送前会把日志保存到本地的binlog文件，只有发送成功才会删除，保证日志上传At Least Once  | 整数，1 开启断点续传功能， 0 关闭                                      |
| setPersistentFilePath     | 持久化的文件名，需要保证文件所在的文件夹已创建。配置多个客户端时，不应设置相同文件           | 字符串，默认为空                                                     |
| setPersistentForceFlush   | 是否每次AddLog强制刷新，高可靠性场景建议打开                                           | 整数，默认为0                                                       |
| setPersistentMaxFileCount | 持久化文件滚动个数，建议设置成10                                                      | 整数，默认为0                                                         |
| setPersistentMaxFileSize  | 每个持久化文件的大小，建议设置成1-10M                                                 | 整数，默认为0                                                         |
| setPersistentMaxLogCount  | 本地最多缓存的日志数，不建议超过1M，通常设置为65536即可                                  | 整数，默认为65536                                                     |
| setConnectTimeoutSec      | 网络连接超时时间                                                                    | 整数，单位秒，默认为10                                                |
| setSendTimeoutSec         | 日志发送超时时间                                                                    | 整数，单位秒，15                                                      |
| setDestroyFlusherWaitSec  | flusher线程销毁最大等待时间                                                          | 整数，单位秒，1                                                       |
| setDestroySenderWaitSec   | sender线程池销毁最大等待时间                                                         | 整数，单位秒，1                                                       |
| setCompressType           | 数据上传时的压缩类型，默认为LZ4压缩                                                   | 整数，0 不压缩，1 LZ4压缩，默认为1                                    |
| setNtpTimeOffset          | 设备时间与标准时间之差，值为标准时间-设备时间，一般此种情况用户客户端设备时间不同步的场景     | 整数，单位秒，默认为0                                                 |
| setMaxLogDelayTime        | 日志时间与本机时间之差，超过该大小后会根据 `setDropDelayLog` 选项进行处理。               | 整数，单位秒，默认为7*24*3600，即7天                                  |
| setDropDelayLog           | 对于超过 `setMaxLogDelayTime` 日志的处理策略                                         | 整数，0 不丢弃，把日志时间修改为当前时间; 1 丢弃，默认为 0 （不丢弃）      |
| setDropUnauthorizedLog    | 是否丢弃鉴权失败的日志                                                               | 整数，0 不丢弃，1丢弃，默认为 0                                       |
| setCallbackFromSenderThread | 是否从 sender 线程回调 callback                                                  | 布尔值，true，从 sender 线程回调；false，从主线程回调。默认为 true       |

### 设置回调函数，创建client
```
// 回调函数不填，默认无回调
LogProducerClient client = new LogProducerClient(config, new LogProducerCallback() {
    @Override
    public void onCall(int resultCode, String reqId, String errorMessage, int logBytes, int compressedBytes) {
        // 回调
        // resultCode       返回结果代码
        // reqId            请求id
        // errorMessage     错误信息，没有为null
        // logBytes         日志大小
        // compressedBytes  压缩后日志大小
        android.util.Log.d("LogProducerCallback",String.format("%s %s %s %s %s",
                                LogProducerResult.fromInt(resultCode), reqId, errorMessage, logBytes, compressedBytes));
    }
});
```

### 写数据
```
Log log = new Log();
log.putContent("k1", "v1");
log.putContent("k2", "v2");
// addLog第二个参数flush，是否立即发送，1代表立即发送，不设置时默认为0
LogProducerResult res = client.addLog(log, 0);
```
