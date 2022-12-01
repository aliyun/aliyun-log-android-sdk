#!/bin/sh
./gradlew :aliyun_sls_android_blockdetection:clean
./gradlew :aliyun_sls_android_blockdetection:assembleRelease
./gradlew :aliyun_sls_android_blockdetection:publishToSonatype closeAndReleaseStagingRepository