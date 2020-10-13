####################################################################################################
# Copyright 2018 Qualcomm Technologies International, Ltd.                                         #
####################################################################################################

set(CFLAGS "-fno-threadsafe-statics")
add_compile_definitions(RD_CLIENT)
add_compile_definitions(__JAVA__)


add_library(ca-interface SHARED "${IoTivity_DIR}/java/jni/JniCaInterface.c")

target_include_directories(ca-interface PUBLIC
  "${IoTivity_DIR}/resource/csdk/logger/include"
  "${IoTivity_DIR}/resource/csdk/connectivity/inc"
  "${IoTivity_DIR}/resource/csdk/connectivity/common/inc"
  "${IoTivity_DIR}/java/jni"
  "${PROJECT_SOURCE_DIR}/include"
  "${IoTivity_DIR}/resource/c_common"
  "${IoTivity_DIR}/resource/csdk/connectivity/api"
  "${PROJECT_SOURCE_DIR}/extlibs/libcoap/include/"
  "${PROJECT_SOURCE_DIR}/extlibs/tinycbor/tinycbor/src"
  "${IoTivity_DIR}/resource/c_common/ocrandom/include"
  )
target_link_libraries (ca-interface resource-directory logger c_common coap)

add_library(ocstack-jni SHARED
    "${IoTivity_DIR}/java/jni/JniOcStack.cpp"
    "${IoTivity_DIR}/java/jni/JniUtils.cpp"
    "${IoTivity_DIR}/java/jni/JniEntityHandler.cpp"
    "${IoTivity_DIR}/java/jni/JniOnResourceFoundListener.cpp"
    "${IoTivity_DIR}/java/jni/JniOnResourcesFoundListener.cpp"
    "${IoTivity_DIR}/java/jni/JniOnDeviceInfoListener.cpp"
    "${IoTivity_DIR}/java/jni/JniOnPlatformInfoListener.cpp"
    "${IoTivity_DIR}/java/jni/JniOnPresenceListener.cpp"
    "${IoTivity_DIR}/java/jni/JniOnGetListener.cpp"
    "${IoTivity_DIR}/java/jni/JniOnPutListener.cpp"
    "${IoTivity_DIR}/java/jni/JniOnPostListener.cpp"
    "${IoTivity_DIR}/java/jni/JniOnDeleteListener.cpp"
    "${IoTivity_DIR}/java/jni/JniOnObserveListener.cpp"
    "${IoTivity_DIR}/java/jni/JniOcRepresentation.cpp"
    "${IoTivity_DIR}/java/jni/JniOcResourceHandle.cpp"
    "${IoTivity_DIR}/java/jni/JniOcPresenceHandle.cpp"
    "${IoTivity_DIR}/java/jni/JniOcRequestHandle.cpp"
    "${IoTivity_DIR}/java/jni/JniOcResourceRequest.cpp"
    "${IoTivity_DIR}/java/jni/JniOcResourceResponse.cpp"
    "${IoTivity_DIR}/java/jni/JniOcPlatform.cpp"
    "${IoTivity_DIR}/java/jni/JniOcResource.cpp"
    "${IoTivity_DIR}/java/jni/JniOcResourceIdentifier.cpp"
    "${IoTivity_DIR}/java/jni/JniOcSecurity.cpp"
    "${IoTivity_DIR}/java/jni/JniOnPublishResourceListener.cpp"
    "${IoTivity_DIR}/java/jni/JniOnDeleteResourceListener.cpp"
    "${IoTivity_DIR}/java/jni/JniOcRDClient.cpp"
)

target_include_directories (ocstack-jni PUBLIC
    "${IoTivity_DIR}/resource/csdk/connectivity/api"
    "${IoTivity_DIR}/resource/include"
    "${IoTivity_DIR}/resource/c_common"
    "${IoTivity_DIR}/resource/c_common/oic_string/include"
    "${IoTivity_DIR}/resource/c_common/oic_malloc/include"
    "${IoTivity_DIR}/resource/csdk/include"
    "${IoTivity_DIR}/resource/csdk/connectivity/inc"
    "${IoTivity_DIR}/resource/csdk/connectivity/common/inc"
    "${IoTivity_DIR}/resource/csdk/stack/include"
    "${IoTivity_DIR}/resource/csdk/ocsocket/include"
    "${IoTivity_DIR}/resource/csdk/resource-directory/include"
    "${IoTivity_DIR}/resource/oc_logger/include"
    "${IoTivity_DIR}/resource/csdk/logger/include"
    "${Boost_DIR}"
    "${IoTivity_DIR}/resource/../build_common/android/compatibility"
    "${IoTivity_DIR}/resource/csdk/security/provisioning/include"
    "${IoTivity_DIR}/resource/csdk/security/provisioning/include/cloud/"
    "${IoTivity_DIR}/resource/csdk/security/provisioning/include/oxm"
    "${IoTivity_DIR}/resource/csdk/security/provisioning/include/internal"
    "${IoTivity_DIR}/resource/csdk/security/include")

add_library(boost_system  STATIC IMPORTED  GLOBAL)
set_target_properties( boost_system
                       PROPERTIES IMPORTED_LOCATION
                       ${IoTivity_DIR}/extlibs/boost/boost_clang_lib/lib/libboost_system.a )

add_library(boost_thread  STATIC IMPORTED  GLOBAL)
set_target_properties( boost_thread
                       PROPERTIES IMPORTED_LOCATION
                       ${IoTivity_DIR}/extlibs/boost/boost_clang_lib/lib/libboost_thread.a )

add_library(boost_date_time  STATIC IMPORTED  GLOBAL)
set_target_properties( boost_date_time
                       PROPERTIES IMPORTED_LOCATION
                       ${IoTivity_DIR}/extlibs/boost/boost_clang_lib/lib/libboost_date_time.a )

add_library(boost_atomic  STATIC IMPORTED  GLOBAL)
set_target_properties( boost_atomic
                       PROPERTIES IMPORTED_LOCATION
                       ${IoTivity_DIR}/extlibs/boost/boost_clang_lib/lib/libboost_atomic.a )


target_link_libraries (ocstack-jni ca-interface resource-directory
                       boost_system boost_thread boost_date_time boost_atomic)
