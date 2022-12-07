#!/bin/sh
# 定义版本号
version=0.0.2
moduleName=sls-android-gradle-plugin

./gradlew :aliyun_sls_android_gradle_plugin:clean -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_gradle_plugin:assemble -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :aliyun_sls_android_gradle_plugin:publishToSonatype closeAndReleaseStagingRepository -Pversion=${version} -PmoduleName=${moduleName}
#./gradlew :aliyun_sls_android_gradle_plugin:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}