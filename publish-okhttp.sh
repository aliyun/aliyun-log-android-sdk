#!/bin/sh
./gradlew :aliyun_sls_android_okhttp:clean
./gradlew :aliyun_sls_android_okhttp:assembleRelease
./gradlew :aliyun_sls_android_okhttp:publishToSonatype closeAndReleaseStagingRepository