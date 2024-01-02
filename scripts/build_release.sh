#!/bin/bash
# set -e
# if [ -z "${ANDROID_NDK_ROOT}" ] ; then
#     echo "ANDROID_NDK_ROOT is not set."
#     exit 1
# elif [ ! -d "${ANDROID_NDK_ROOT}" ] ; then
#     echo "ANDROID_NDK_ROOT=${ANDROID_NDK_ROOT} is not a directory."
#     exit 1
# fi
# docker build --platform linux/amd64 -t ndkports .
# docker run --platform linux/amd64 --rm -u $(id -u ${USER}):$(id -g ${USER}) -v $(pwd):/src -v "${ANDROID_NDK_ROOT}":/ndk ndkports
# docker build -t ndkports .
# docker run --rm -u $(id -u ${USER}):$(id -g ${USER}) -v $(pwd):/src -v "${ANDROID_NDK_ROOT}":/ndk ndkports
docker build -t ndkports .
docker run --rm -u $(id -u ${USER}):$(id -g ${USER}) -v $(pwd):/src ndkports
