#!/bin/sh
#########################################################
# Function: publish aar by git tag                      #
# Date    : 2017-07-28                                  #
# Author  : yulong.gyl                                  #
#########################################################

# obtain current tag or branch or commit
function current_tag () {
    local folder="$(pwd)"
    [ -n "$1" ] && folder="$1"
    git -C "$folder" tag --points-at HEAD || \
    git -C "$folder" symbolic-ref -q --short HEAD || \
    git -C "$folder" rev-parse --short HEAD
}

#function git_version() {
#    folder="$(pwd)"
#
#    # get tag name that points to the current HEAD commit
#    git_tag=$(git -C "$folder" tag --points-at HEAD 2>/dev/null)
#
#    # if no tag found, get the branch name
#    if [ -z "$git_tag" ]; then
#        git_branch=$(git -C "$folder" symbolic-ref -q --short HEAD 2>/dev/null)
#
#        # if not on a branch, get the short commit hash
#        if [ -z "$git_branch" ]; then
#            git_commit=$(git -C "$folder" rev-parse --short HEAD 2>/dev/null)
#            echo "$git_commit"
#        else
#            echo "$git_branch"
#        fi
#    else
#        echo "$git_tag"
#    fi
#}

# map tag to module name
function module_name() {
  case $1 in
    *webview-instrumentation*)    echo "webview_instrumentation";;
    *blockdetection*)             echo "aliyun_sls_android_blockdetection";;
    *core*)                       echo "aliyun_sls_android_core";;
    *crashreporter*)              echo "aliyun_sls_android_crashreporter";;
    *network_diagnosis*)          echo "aliyun_sls_android_network_diagnosis";;
    *okhttp*)                     echo "aliyun_sls_android_okhttp";;
    *otel_common*)                echo "aliyun_sls_android_otel_common";;
    *exporter_otlp*)              echo "aliyun_sls_android_exporter_otlp";;
    *ot-ktx*)                     echo "aliyun_sls_android_ot_ktx";;
    *ot*)                         echo "aliyun_sls_android_ot";;
    *producer*)                   echo "aliyun_sls_android_producer";;
    *trace*)                      echo "aliyun_sls_android_trace";;
    *)                            echo "not_support";;
  esac
}

for tag in $(current_tag .)
do
  module=$(module_name $tag)
  if [[ $module == not_support ]]; then
      echo "module: $module not supported, please upgrade project"
      continue
  fi

  echo "########################################################################"
  echo "#### start build $module"
  echo "########################################################################"

# android build command
  ./gradlew :$module:clean                                                               # clean project first
  ./gradlew :$module:assembleRelease                                                     # assembleRelease
#  ./gradlew :$module:publishToSonatype closeAndReleaseStagingRepository                  # publish aar to maven central
  ./gradlew :$module:publishToMavenLocal                                                # publish aar to maven central

done

