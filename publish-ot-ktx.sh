#!/bin/sh
# 定义版本号
version=1.0.1
moduleName=sls-android-ot-ktx

./gradlew :aliyun_sls_android_ot_ktx:clean -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_ot_ktx:assembleRelease -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_ot_ktx:publishToSonatype closeAndReleaseStagingRepository -Pversion=${version} -PmoduleName=${moduleName}
#./gradlew :aliyun_sls_android_ot_ktx:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}