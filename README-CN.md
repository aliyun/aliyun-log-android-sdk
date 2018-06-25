# Aliyun SLS SDK for Android

## [README of English](https://github.com/aliyun/aliyun-log-android-sdk/blob/master/README.md)

## 简介

本文档主要介绍阿里云日志服务 Android SDK的安装和使用。本文档假设您已经开通了阿里云日志 服务。如果您还没有开通或者还不了解日志服务，请登录[日志服务产品主页](https://www.aliyun.com/product/sls/)获取更多的帮助。

## 环境要求
- Android系统版本：2.3 及以上。
- 必须注册有Aliyun.com用户账户，并开通日志服务。

## 安装

日志服务 Android SDK依赖于[fastjson](https://github.com/alibaba/fastjson),[greenDAO](https://github.com/greenrobot/greenDAO)。

###通过Gradle获取依赖

```
compile 'com.aliyun.openservices:aliyun-log-android-sdk:2.0.0'
```

###或通过源码编译jar包

clone工程源码之后，运行gradle命令打包：

```
# clone工程
$ git clone https://github.com/aliyun/aliyun-log-android-sdk.git

# 进入目录
$ cd aliyun-log-android-sdk/aliyun-sls-android-sdk/

# 执行打包脚本，要求jdk 1.7
$ ../gradlew releaseJar

# 进入打包生成目录，jar包生成在该目录下
$ cd build/libs && ls
```


### 权限设置

以下是日志服务 Android SDK所需要的Android权限，请确保您的AndroidManifest.xml文件中已经配置了这些权限，否则，SDK将无法正常工作。

```
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>

```

## 快速入门

以下演示了上传log文件的基本流程。

1.首先如果没有自定义的Application,则新建一个XXApplication继承自Application，然后在AndroidManifest.xml中<application android:name=".XXApplication
/application>"。XXApplication代码如下：
```
import com.aliyun.sls.android.sdk.SLSDatabaseManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SLSDatabaseManager.getInstance().setupDB(getApplicationContext());
    }
}

```
2.初始化LOGClient,并且设置context
```
        // 配置信息
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        
        conf.setCachable(true);     // 设置日志发送失败时，是否支持本地缓存。
        conf.setConnectType(ClientConfiguration.NetworkPolicy.WIFI_ONLY);   // 设置缓存日志发送的网络策略。
        
        
        SLSLog.enableLog(); // log打印在控制台
        
        LOGClient logClient = new LOGClient(getApplicationContext(), endpoint, credentialProvider, conf);    // 初始化client

```

3.初始化日志，然后用client进行上传
```
        // 1. 创建logGroup
        LogGroup logGroup = new LogGroup("sls test", TextUtils.isEmpty(ip) ? " no ip " : ip);

        // 2. 创建一条log
        Log log = new Log();
        log.PutContent("current time ", "" + System.currentTimeMillis() / 1000);
        log.PutContent("content", "this is a log");
        
        // 3. 将log加入到group
        logGroup.PutLog(log);

        // 4. 发送log到sls服务器
        try {
            PostLogRequest request = new PostLogRequest(project, logStore, logGroup);
            logClient.asyncPostLog(request, new CompletedCallback<PostLogRequest, PostLogResult>() {
                @Override
                public void onSuccess(PostLogRequest request, PostLogResult result) {
                    Message message = Message.obtain(handler);
                    message.what = HANDLER_MESSAGE_UPLOAD_SUCCESS;
                    message.sendToTarget();
                }

                @Override
                public void onFailure(PostLogRequest request, LogException exception) {
                    Message message = Message.obtain(handler);
                    message.what = HANDLER_MESSAGE_UPLOAD_FAILED;
                    message.obj = exception.getMessage();
                    message.sendToTarget();
                }
            });
        } catch (LogException e) {
            e.printStackTrace();
        }

```



更多细节用法可以参考本工程的：

sample目录: [点击查看](https://github.com/aliyun/aliyun-log-android-sdk/tree/master/app)。


## License
* Apache License 2.0


## 联系我们

* 阿里云日志服务官方文档中心：https://www.aliyun.com/product/sls/

