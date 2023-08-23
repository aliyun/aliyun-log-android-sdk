#!/bin/sh
# 定义版本号
version=1.0.3
moduleName=sls-android-blockdetection

./gradlew :aliyun_sls_android_blockdetection:clean -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_blockdetection:assembleRelease -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_blockdetection:publishToSonatype closeAndReleaseStagingRepository -Pversion=${version} -PmoduleName=${moduleName}
#./gradlew :aliyun_sls_android_blockdetection:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}