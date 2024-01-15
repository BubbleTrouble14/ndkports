#!/bin/bash

CLANG=$(xcrun --sdk iphoneos --find clang)
AR=$(xcrun --sdk iphoneos --find ar)
BITCODE_FLAGS=" -fembed-bitcode"
LIB_NAME="utf8proc" # Set your library name here
LIB_VERSION="2.9.0" # Set your library version here
DOWNLOAD_URL="https://github.com/JuliaStrings/utf8proc/releases/download/v2.9.0/utf8proc-2.9.0.tar.gz" # Set the download URL here

function realpath() {
  [[ $1 = /* ]] && echo "$1" || echo "$PWD/${1#./}"
}

download() {
  local CURRENT=`pwd`

  if [ ! -d "${CURRENT}/${LIB_NAME}" ]; then
    echo "Downloading GMP ${LIB_VERSION}..."
    curl -L -o "${CURRENT}/${LIB_NAME}-${LIB_VERSION}.tar.bz2" ${DOWNLOAD_URL}
    tar xfj "${LIB_NAME}-${LIB_VERSION}.tar.bz2"
    mv ${LIB_NAME}-${LIB_VERSION} ${LIB_NAME}
    rm "${LIB_NAME}-${LIB_VERSION}.tar.bz2"
  else
    echo "${LIB_NAME} ${LIB_VERSION} is already downloaded."
  fi
}

build() {
  cd ${LIB_NAME}

  build_for_ios
  build_for_simulator
}

build_for_ios() {
  echo "Building library for iOS..."

  local PREFIX=$(realpath "./lib/iphoneos")
  local ARCH='arm64'
  local SDK_DEVICE_PATH=$(xcrun --sdk iphoneos --show-sdk-path)
  local MIN_VERSION='-miphoneos-version-min=15.0'

  build_scheme $PREFIX $ARCH $SDK_DEVICE_PATH $MIN_VERSION
}

build_for_simulator() {
  echo "Building library for iOS simulator..."
  local PREFIX=$(realpath "./lib/iphonesimulator-arm64")
  local SDK_SIMULATOR_PATH=$(xcrun --sdk iphonesimulator --show-sdk-path)
  local MIN_VERSION='-miphonesimulator-version-min=15.0'

  build_scheme $PREFIX 'arm64' $SDK_SIMULATOR_PATH $MIN_VERSION

  local PREFIX=$(realpath "./lib/iphonesimulator-x86_64")
  build_scheme $PREFIX 'x86_64' $SDK_SIMULATOR_PATH $MIN_VERSION
}

build_scheme() {
  clean

  local PREFIX="$1"
  local ARCH="$2"
  local SYS_ROOT="$3"
  local MIN_VERSION="$4"

  export SDKROOT=${SYS_ROOT}
  export LIBRARY_PATH="$LIBRARY_PATH:$SDKROOT/usr/lib"

  local EXTRAS="--target=${ARCH}-apple-darwin ${MIN_VERSION}"
  local CFLAGS="${EXTRAS} ${BITCODE_FLAGS} -isysroot ${SYS_ROOT} -Wno-error -Wno-implicit-function-declaration"

  echo "Building for prefix: ${PREFIX}"
  echo "Building using CMake..."

  if [ ! -e "${PREFIX}" ]; then
    mkdir -p "${PREFIX}"
  fi
  make CC="${CLANG}" CFLAGS="${CFLAGS}" prefix=${PREFIX} install

  # make check

  # if [ ! -e "${PREFIX}" ]; then
    # mkdir -p "${PREFIX}"
    # if [ "$USE_CMAKE" = "1" ]; then
    #     echo "Building using CMake..."
    #     cmake -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX="${PREFIX}"

    #     make
    #     make install
    # else
    #     echo "Building using configure script..."
    #     ./configure \
    #       --prefix="${PREFIX}" \
    #       CC="${CLANG}" \
    #       CPPFLAGS="${CFLAGS}" \
    #       --host=arm64-apple-darwin \
    #       --disable-assembly --enable-static --disable-shared --enable-cxx

    #     make
    #     make install
    # fi
  # fi
}

create_framework() {
  echo "Merging libraries in XCFramework..."

	local SIMULATOR_PATH="./lib/iphonesimulator"
  local BUILD_PATH="./build/Clib${LIB_NAME}.xcframework"
  local HEADER_PATH="./headers"

    # Clean up existing directories
  rm -rf $HEADER_PATH
  mkdir -p $HEADER_PATH

  # Copy headers
  cp ${LIB_NAME}.h $HEADER_PATH/
  # cp ${LIB_NAME}xx.h $HEADER_PATH/

  mkdir -p $SIMULATOR_PATH/lib

  lipo -create -output ${SIMULATOR_PATH}/lib/lib${LIB_NAME}.a \
		-arch arm64 ${SIMULATOR_PATH}-arm64/lib/lib${LIB_NAME}.a \
		-arch x86_64 ${SIMULATOR_PATH}-x86_64/lib/lib${LIB_NAME}.a

	xcodebuild -create-xcframework \
		-library ./lib/iphoneos/lib/lib${LIB_NAME}.a -headers $HEADER_PATH \
		-library ${SIMULATOR_PATH}/lib/lib${LIB_NAME}.a -headers $HEADER_PATH \
		-output $BUILD_PATH

  echo "GMP.xcframework saved to 'build' folder"
}

clean() {
  make clean
  # make distclean
}

download
build
create_framework