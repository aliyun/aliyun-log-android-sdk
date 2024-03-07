#!/bin/sh
./gradlew :aliyun_sls_android_crashreporter:clean
./gradlew :aliyun_sls_android_crashreporter:assembleRelease
./gradlew :aliyun_sls_android_crashreporter:publishToSonatype closeAndReleaseStagingRepository
#./gradlew :aliyun_sls_android_crashreporter:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}