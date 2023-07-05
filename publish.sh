#!/bin/sh
#########################################################
# Function: publish aar by git tag                      #
# Date    : 2017-07-28                                  #
# Author  : yulong.yyl                                  #
#########################################################

# obtain current tag or branch or commit
function current_tag () {
    local folder="$(pwd)"
    [ -n "$1" ] && folder="$1"
    git -C "$folder" tag --points-at HEAD || \
    git -C "$folder" symbolic-ref -q --short HEAD || \
    git -C "$folder" rev-parse --short HEAD
}

# map tag to module name
function module_name() {
  case $1 in
    *blockdetection*)     echo "aliyun_sls_android_blockdetection";;
    *core*)               echo "aliyun_sls_android_core";;
    *crashreporter*)      echo "aliyun_sls_android_crashreporter";;
    *network_diagnosis*)  echo "aliyun_sls_android_network_diagnosis";;
    *okhttp*)             echo "aliyun_sls_android_okhttp";;
    *ot*)                 echo "aliyun_sls_android_ot";;
    *ot_ktx*)             echo "aliyun_sls_android_ot_ktx";;
    *producer*)           echo "aliyun_sls_android_producer";;
    *trace*)              echo "aliyun_sls_android_trace";;
    *)                    echo "not_support";;
  esac
}

for tag in $(current_tag .)
do
  module=$(module_name $tag)
  if [[ $module == not_support ]]; then
      echo "module: $module not supported, please upgrade project"
      exit 1
  fi

  echo "########################################################################"
  echo "#### start build $module"
  echo "########################################################################"

# android build command
  ./gradlew :$module:clean                                # clean project first
  ./gradlew :$module:assembleRelease                      # assembleRelease
  ./gradlew :$module:publishToMavenLocal                 # publish aar to maven local

done

