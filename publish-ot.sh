#!/bin/sh
# 定义版本号
version=1.0.6
moduleName=sls-android-ot

./gradlew :aliyun_sls_android_ot:clean -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_ot:assembleRelease -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_ot:publishToSonatype closeAndReleaseStagingRepository -Pversion=${version} -PmoduleName=${moduleName}
#./gradlew :aliyun_sls_android_ot:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}