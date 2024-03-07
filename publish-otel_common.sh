#!/bin/sh
./gradlew :aliyun_sls_android_otel_common:clean
./gradlew :aliyun_sls_android_otel_common:assembleRelease
./gradlew :aliyun_sls_android_otel_common:publishToSonatype closeAndReleaseStagingRepository
#./gradlew :aliyun_sls_android_network_diagnosis:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}