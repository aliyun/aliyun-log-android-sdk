#!/bin/sh
./gradlew :aliyun_sls_android_ot_ktx:clean
./gradlew :aliyun_sls_android_ot_ktx:assembleRelease
./gradlew :aliyun_sls_android_ot_ktx:publishToSonatype closeAndReleaseStagingRepository