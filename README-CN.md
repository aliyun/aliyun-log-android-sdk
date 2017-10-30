# Aliyun SLS SDK for Android

## [README of English](https://github.com/aliyun/aliyun-log-android-sdk/blob/master/README.md)

## 简介

本文档主要介绍阿里云日志服务 Android SDK的安装和使用。本文档假设您已经开通了阿里云日志 服务。如果您还没有开通或者还不了解日志服务，请登录[日志服务产品主页](https://www.aliyun.com/product/sls/)获取更多的帮助。

## 环境要求
- Android系统版本：2.3 及以上。
- 必须注册有Aliyun.com用户账户，并开通日志服务。

## 安装

日志服务 Android SDK依赖于[fastjson](https://github.com/alibaba/fastjson)。

###通过Gradle获取依赖

```
compile 'com.aliyun.openservices:aliyun-log-android-sdk:0.4.0'
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
```

## 快速入门

以下演示了上传log文件的基本流程。更多细节用法可以参考本工程的：

sample目录: [点击查看](https://github.com/aliyun/aliyun-log-android-sdk/tree/master/app)。


## License
* Apache License 2.0


## 联系我们

* 阿里云日志服务官方文档中心：https://www.aliyun.com/product/sls/

