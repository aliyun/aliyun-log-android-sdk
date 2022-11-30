#!/bin/sh
./gradlew :aliyun_sls_android_ot:clean
./gradlew :aliyun_sls_android_ot:assembleRelease
./gradlew :aliyun_sls_android_ot:publishToSonatype closeAndReleaseStagingRepository