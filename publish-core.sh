#!/bin/sh
./gradlew :aliyun_sls_android_core:clean
./gradlew :aliyun_sls_android_core:assembleRelease
./gradlew :aliyun_sls_android_core:publish