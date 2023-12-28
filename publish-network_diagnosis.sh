#!/bin/sh
./gradlew :aliyun_sls_android_network_diagnosis:clean
./gradlew :aliyun_sls_android_network_diagnosis:assembleRelease
./gradlew :aliyun_sls_android_network_diagnosis:publishToSonatype closeAndReleaseStagingRepositor
#./gradlew :aliyun_sls_android_network_diagnosis:publishToMavenLocal