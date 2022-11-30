#!/bin/sh
./gradlew :aliyun_sls_android_trace:clean
./gradlew :aliyun_sls_android_trace:assembleRelease
./gradlew :aliyun_sls_android_trace:publishToSonatype closeAndReleaseStagingRepository