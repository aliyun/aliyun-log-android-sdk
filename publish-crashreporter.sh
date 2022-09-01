#!/bin/sh
./gradlew :aliyun_sls_android_crashreporter:clean
./gradlew :aliyun_sls_android_crashreporter:assembleRelease
./gradlew :aliyun_sls_android_crashreporter:publish