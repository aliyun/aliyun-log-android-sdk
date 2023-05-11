#!/bin/sh
# 定义版本号
version=2.0.6
moduleName=sls-android-crashreporter

./gradlew :aliyun_sls_android_crashreporter:clean -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_crashreporter:assembleRelease -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_crashreporter:publishToSonatype closeAndReleaseStagingRepository -Pversion=${version} -PmoduleName=${moduleName}
#./gradlew :aliyun_sls_android_crashreporter:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}