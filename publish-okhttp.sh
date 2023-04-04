#!/bin/sh
# 定义版本号
version=1.0.7
moduleName=sls-android-okhttp

./gradlew :aliyun_sls_android_okhttp:clean -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_okhttp:assembleRelease -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_okhttp:publishToSonatype closeAndReleaseStagingRepository -Pversion=${version} -PmoduleName=${moduleName}
#./gradlew :aliyun_sls_android_okhttp:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}