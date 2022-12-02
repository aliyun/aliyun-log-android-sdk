#!/bin/sh
# 定义版本号
version=1.0.5
moduleName=sls-android-core

./gradlew :aliyun_sls_android_core:clean -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_core:assembleRelease -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_core:publishToSonatype closeAndReleaseStagingRepository -Pversion=${version} -PmoduleName=${moduleName}
#./gradlew :aliyun_sls_android_core:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}