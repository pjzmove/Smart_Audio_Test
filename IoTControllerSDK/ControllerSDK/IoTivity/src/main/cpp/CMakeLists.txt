####################################################################################################
# Copyright 2018 Qualcomm Technologies International, Ltd.                                         #
####################################################################################################

cmake_minimum_required(VERSION 3.4.1)

project(iotivity LANGUAGES CXX C)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)
set(PROJECT_LIBNAME lib${PROJECT_NAME})
set(CMAKE_C_FLAGS_DEBUG "-Wall -Wextra -g")
set(CMAKE_CXX_FLAGS_DEBUG "-fno-threadsafe-statics")
set(IoTivity_DIR  "${PROJECT_SOURCE_DIR}/../../../../../iotivity")
set(Boost_DIR  "${IoTivity_DIR}/extlibs/boost/")
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/native-libs/${ANDROID_ABI})

add_subdirectory(extlibs)
add_subdirectory(resource/csdk/connectivity)
add_subdirectory(resource/csdk/stack)
add_subdirectory(resource/oc)
add_subdirectory(resource/csdk/resource-directory)
add_subdirectory(jni)

