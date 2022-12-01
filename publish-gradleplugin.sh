#!/bin/sh
./gradlew :aliyun_sls_android_gradle_plugin:clean
./gradlew :aliyun_sls_android_gradle_plugin:assemble
./gradlew :aliyun_sls_android_gradle_plugin:publishToSonatype closeAndReleaseStagingRepository