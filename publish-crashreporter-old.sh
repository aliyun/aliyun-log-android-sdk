#!/bin/sh
./gradlew :aliyun_sls_android_crashreporter_old:clean
./gradlew :aliyun_sls_android_crashreporter_old:assembleRelease
./gradlew :aliyun_sls_android_crashreporter_old:publishToSonatype closeAndReleaseStagingRepository
#./gradlew :aliyun_sls_android_crashreporter:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}