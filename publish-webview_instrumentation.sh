#!/bin/sh
# 定义版本号
version=1.2.0-dev.1
moduleName=sls-android-webview-instrumentation

./gradlew :web_instrumentation:clean -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :web_instrumentation:assembleRelease -Pversion=${version} -PmoduleName=${moduleName}
#./gradlew :web_instrumentation:publishToSonatype closeAndReleaseStagingRepository -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :web_instrumentation:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}