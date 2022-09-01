#!/bin/sh
./gradlew :aliyun_sls_android_producer:clean
./gradlew :aliyun_sls_android_producer:assembleRelease
./gradlew :aliyun_sls_android_producer:publish