# Aliyun SLS SDK for Android

## [README of English](https://github.com/aliyun/aliyun-log-android-sdk/blob/master/README.md)

## 简介

本文档主要介绍阿里云日志服务 Android SDK的安装和使用。本文档假设您已经开通了阿里云日志 服务。如果您还没有开通或者还不了解日志服务，请登录[日志服务产品主页](https://www.aliyun.com/product/sls/)获取更多的帮助。

## 环境要求
- Android系统版本：2.3 及以上。
- 必须注册有Aliyun.com用户账户，并开通日志服务。

## 安装

日志服务 Android SDK依赖于[fastjson](https://github.com/alibaba/fastjson)。

在项目中使用时可以通过maven依赖。

### Maven依赖

```
<dependency>
  <groupId>com.aliyun.openservices</groupId>
  <artifactId>aliyun-log-android-sdk</artifactId>
  <version>0.3.1</version>
</dependency>
```


### 权限设置

以下是日志服务 Android SDK所需要的Android权限，请确保您的AndroidManifest.xml文件中已经配置了这些权限，否则，SDK将无法正常工作。

```
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
```

## 快速入门

以下演示了上传log文件的基本流程。更多细节用法可以参考本工程的：

sample目录: [点击查看](https://github.com/aliyun/aliyun-log-android-sdk/tree/master/app)。

## 完整文档

SDK提供的更多功能，详见官方完整文档：[点击查看](https://help.aliyun.com/document_detail/43200.html)


## License
* Apache License 2.0


## 联系我们

* 阿里云日志服务官方文档中心：https://www.aliyun.com/product/sls/

