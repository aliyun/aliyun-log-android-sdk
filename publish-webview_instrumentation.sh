#!/bin/sh

./gradlew :web_instrumentation:clean -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :web_instrumentation:assembleRelease -Pversion=${version} -PmoduleName=${moduleName}
./gradlew :web_instrumentation:publishToSonatype closeAndReleaseStagingRepository -Pversion=${version} -PmoduleName=${moduleName}
#./gradlew :web_instrumentation:publishToMavenLocal -Pversion=${version} -PmoduleName=${moduleName}