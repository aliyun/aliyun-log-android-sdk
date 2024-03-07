#!/bin/sh
# 定义版本号
./gradlew :aliyun_sls_android_exporter_otlp:clean
./gradlew :aliyun_sls_android_exporter_otlp:assembleRelease
./gradlew :aliyun_sls_android_exporter_otlp:publishToSonatype closeAndReleaseStagingRepository
#./gradlew :aliyun_sls_android_network_diagnosis:publishToMavenLocal