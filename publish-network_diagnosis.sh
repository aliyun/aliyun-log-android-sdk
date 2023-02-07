#!/bin/sh
# 定义版本号
version=2.0.9
moduleName=sls-android-network-diagnosis

./gradlew :aliyun_sls_android_network_diagnosis:clean -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_network_diagnosis:assembleRelease -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_network_diagnosis:publishToSonatype closeAndReleaseStagingRepository -Pversion=${version} -PmoduleName=${moduleName}
#./gradlew :aliyun_sls_android_network_diagnosis:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}