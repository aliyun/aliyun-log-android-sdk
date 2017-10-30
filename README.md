# Alibaba Cloud SLS SDK for Android

## [README of Chinese](https://github.com/aliyun/aliyun-log-android-sdk/blob/master/README-CN.md)

## Introduction

This document mainly describes how to install and use the SLS Android SDK. If you have not yet activated or do not know about the SLS service, log on to the [SLS Product Homepage](https://www.aliyun.com/product/sls/) for more help.

## Environment requirements

- Android ***2.3*** or above
- You must have registered an Alibaba Cloud account with the SLS activated.

## Installation

SLS Android SDK is dependent on [fastjson](https://github.com/alibaba/fastjson). 

you can use it through the Gradle. 

### Gradle

```
compile 'com.aliyun.openservices:aliyun-log-android-sdk:0.4.0'
```
### or you can compile jar by source code
```
# clone工程
$ git clone https://github.com/aliyun/aliyun-log-android-sdk.git

# 进入目录
$ cd aliyun-log-android-sdk/aliyun-sls-android-sdk/

# run task(must jdk 1.7 above)
$ ../gradlew releaseJar

# location
$ cd build/libs && ls
```

### Configure permissions

The following are the Android permissions needed by the SLS Android SDK. Please make sure these permissions are already set in your `AndroidManifest.xml` file. Otherwise, the SDK will not work normally.

```
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
```

## Quick start

The basic processes for uploading log is demonstrated below. For details, you can refer to the following directories of this project:

the sample directory: [click to view details](https://github.com/aliyun/aliyun-log-android-sdk/tree/master/app).


## License

* Apache License 2.0.

## Contact us

* [Alibaba Cloud SLS official documentation center](https://www.aliyun.com/product/sls/).
