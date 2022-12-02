#!/bin/sh
# 定义版本号
version=1.0.3
moduleName=sls-android-trace

./gradlew :aliyun_sls_android_trace:clean -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_trace:assembleRelease -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_trace:publishToSonatype closeAndReleaseStagingRepository -Pversion=${version} -PmoduleName=${moduleName}
#./gradlew :aliyun_sls_android_trace:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}