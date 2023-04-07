#!/bin/sh
# 定义版本号
version=2.6.12
moduleName=aliyun-log-android-sdk

./gradlew :aliyun_sls_android_producer:clean -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_producer:assembleRelease -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_producer:publishToSonatype closeAndReleaseStagingRepository -Pversion=${version} -PmoduleName=${moduleName}
#./gradlew :aliyun_sls_android_producer:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}