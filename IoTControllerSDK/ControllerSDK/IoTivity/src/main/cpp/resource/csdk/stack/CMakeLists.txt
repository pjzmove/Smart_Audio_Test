####################################################################################################
# Copyright 2018 Qualcomm Technologies International, Ltd.                                         #
####################################################################################################

add_compile_definitions(WITH_POSIX)
add_compile_definitions(RD_CLIENT)

add_library(logger STATIC
    "${IoTivity_DIR}/resource/csdk/logger/src/logger.c"
    "${IoTivity_DIR}/resource/csdk/logger/src/trace.c"
    )

target_include_directories(logger PUBLIC
    "${PROJECT_SOURCE_DIR}/include"
    "${IoTivity_DIR}/resource/c_common"
    "${IoTivity_DIR}/resource/csdk/logger/include"
    )

add_library(c_common STATIC
    "${IoTivity_DIR}/resource/c_common/oic_string/src/oic_string.c"
    "${IoTivity_DIR}/resource/c_common/oic_malloc/src/oic_malloc.c"
    "${IoTivity_DIR}/resource/c_common/oic_time/src/oic_time.c"
    "${IoTivity_DIR}/resource/c_common/ocrandom/src/ocrandom.c"
    "${IoTivity_DIR}/resource/c_common/oic_platform/src/oic_platform.c"
    "${IoTivity_DIR}/resource/c_common/octhread/src/posix/octhread.c"
    "${IoTivity_DIR}/resource/c_common/ocatomic/src/others/ocatomic.c"
    "${IoTivity_DIR}/resource/c_common/ocevent/src/others/ocevent.c"
    "${IoTivity_DIR}/resource/c_common/oic_platform/src/others/oic_otherplatforms.c"
    "${IoTivity_DIR}/resource/c_common/octimer/src/octimer.c"
    )

target_include_directories(c_common PUBLIC
    "${PROJECT_SOURCE_DIR}/include"
    "${IoTivity_DIR}/resource/c_common"
    "${IoTivity_DIR}/resource/c_common/oic_time/include"
    "${IoTivity_DIR}/resource/c_common/octimer/include"
    "${IoTivity_DIR}/resource/c_common/ocatomic/include"
    "${IoTivity_DIR}/resource/c_common/ocevent/include"
    "${IoTivity_DIR}/resource/c_common/oic_malloc/include"
    "${IoTivity_DIR}/resource/c_common/oic_string/include"
    "${IoTivity_DIR}/resource/c_common/oic_platform/include"
    "${IoTivity_DIR}/resource/c_common/ocrandom/include"
    "${IoTivity_DIR}/resource/c_common/octhread/include"
    "${IoTivity_DIR}/resource/csdk/logger/include"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/include/"
    )

add_library(ocsrm STATIC
    "${IoTivity_DIR}/resource/csdk/security/src/secureresourcemanager.c"
    "${IoTivity_DIR}/resource/csdk/security/src/resourcemanager.c"
    "${IoTivity_DIR}/resource/csdk/security/src/aclresource.c"
    "${IoTivity_DIR}/resource/csdk/security/src/amaclresource.c"
    "${IoTivity_DIR}/resource/csdk/security/src/pstatresource.c"
    "${IoTivity_DIR}/resource/csdk/security/src/spresource.c"
    "${IoTivity_DIR}/resource/csdk/security/src/doxmresource.c"
    "${IoTivity_DIR}/resource/csdk/security/src/credresource.c"
    "${IoTivity_DIR}/resource/csdk/security/src/policyengine.c"
    "${IoTivity_DIR}/resource/csdk/security/src/psinterface.c"
    "${IoTivity_DIR}/resource/csdk/security/src/srmresourcestrings.c"
    "${IoTivity_DIR}/resource/csdk/security/src/srmutility.c"
    "${IoTivity_DIR}/resource/csdk/security/src/iotvticalendar.c"
    "${IoTivity_DIR}/resource/csdk/security/src/base64.c"
    "${IoTivity_DIR}/resource/csdk/security/src/deviceonboardingstate.c"

    "${PROJECT_SOURCE_DIR}/extlibs/tinycbor/tinycbor/src/cborparser.c"
    "${PROJECT_SOURCE_DIR}/extlibs/tinycbor/tinycbor/src/cborparser_dup_string.c"
    "${PROJECT_SOURCE_DIR}/extlibs/tinycbor/tinycbor/src/cborencoder.c"
    "${PROJECT_SOURCE_DIR}/extlibs/tinycbor/tinycbor/src/cborerrorstrings.c"
   )
target_include_directories(ocsrm PUBLIC
    "${PROJECT_SOURCE_DIR}/include"
    "${IoTivity_DIR}/resource/c_common"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/include"
    "${IoTivity_DIR}/resource/csdk/logger/include"
    "${IoTivity_DIR}/resource/c_common/ocrandom/include"
    "${IoTivity_DIR}/resource/csdk/include"
    "${IoTivity_DIR}/resource/csdk/stack/include"
    "${IoTivity_DIR}/resource/csdk/stack/include/internal"
    "${IoTivity_DIR}/resource/oc_logger/include"
    "${IoTivity_DIR}/resource/csdk/connectivity/external/inc"
    "${IoTivity_DIR}/resource/csdk/connectivity/common/inc"
    "${IoTivity_DIR}/resource/csdk/connectivity/inc"
    "${IoTivity_DIR}/resource/csdk/connectivity/api"
    "${IoTivity_DIR}/resource/csdk/security/include"
    "${IoTivity_DIR}/resource/csdk/security/include/internal"
    "${IoTivity_DIR}/resource/csdk/security/provisioning/include"
    "${IoTivity_DIR}/resource/c_common/oic_malloc/include"
    "${IoTivity_DIR}/resource/c_common/oic_string/include"
    "${IoTivity_DIR}/resource/c_common/oic_platform/include"
    "${PROJECT_SOURCE_DIR}/extlibs/tinycbor/tinycbor/src"
    "${IoTivity_DIR}/resource/c_common/octhread/include"
)


add_compile_definitions(ROUTING_EP)
add_library(routingmanager STATIC "${IoTivity_DIR}/resource/csdk/routing/src/routingutility.c")

target_include_directories(routingmanager PUBLIC
        "${PROJECT_SOURCE_DIR}/include"
        "${IoTivity_DIR}/resource/csdk/stack/include"
        "${IoTivity_DIR}/resource/csdk/stack/internal/include"
        "${IoTivity_DIR}/resource/csdk/logger/include"
        "${IoTivity_DIR}/resource/csdk/connectivity/api"
        "${IoTivity_DIR}/resource/csdk/connectivity/common/inc"
        "${IoTivity_DIR}/resource/csdk/connectivity/external/inc"
        "${IoTivity_DIR}/resource/csdk/routing/include"
        "${IoTivity_DIR}/resource/c_common"
        "${IoTivity_DIR}/resource/csdk/include"
        "${IoTivity_DIR}/resource/csdk/stack/include/internal/"
        "${IoTivity_DIR}/resource/c_common/ocrandom/include"
        "${IoTivity_DIR}/resource/c_common/oic_malloc/include"
)

add_library(octbstack SHARED
    "${IoTivity_DIR}/resource/csdk/stack/src/ocstack.c"
	"${IoTivity_DIR}/resource/csdk/stack/src/ocpayload.c"
    "${IoTivity_DIR}/resource/csdk/stack/src/ocpayloadparse.c"
    "${IoTivity_DIR}/resource/csdk/stack/src/ocpayloadconvert.c"
    "${IoTivity_DIR}/resource/csdk/stack/src/occlientcb.c"
    "${IoTivity_DIR}/resource/csdk/stack/src/ocresource.c"
    "${IoTivity_DIR}/resource/csdk/stack/src/ocobserve.c"
    "${IoTivity_DIR}/resource/csdk/stack/src/ocserverrequest.c"
    "${IoTivity_DIR}/resource/csdk/stack/src/occollection.c"
    "${IoTivity_DIR}/resource/csdk/stack/src/oicgroup.c"
    "${IoTivity_DIR}/resource/csdk/stack/src/ocendpoint.c"
    "${IoTivity_DIR}/resource/csdk/stack/src/oickeepalive.c"
)

target_include_directories(octbstack PUBLIC
    "${PROJECT_SOURCE_DIR}/include"
    "${IoTivity_DIR}/resource/c_common"
    "${IoTivity_DIR}/resource/c_common/octimer/include"
    "${IoTivity_DIR}/resource/c_common/oic_time/include"
    "${IoTivity_DIR}/resource/c_common/ocatomic/include"
    "${IoTivity_DIR}/resource/c_common/oic_malloc/include"
    "${IoTivity_DIR}/resource/c_common/oic_string/include"
    "${IoTivity_DIR}/resource/c_common/oic_platform/include"
    "${IoTivity_DIR}/resource/c_common/ocrandom/include"
    "${IoTivity_DIR}/resource/c_common/octhread/include"
    "${IoTivity_DIR}/resource/csdk/logger/include"
    "${IoTivity_DIR}/resource/csdk/stack/include"
    "${IoTivity_DIR}/resource/csdk/include"
    "${IoTivity_DIR}/resource/csdk/stack/include/internal"
    "${IoTivity_DIR}/resource/oc_logger/include"
    "${IoTivity_DIR}/resource/csdk/connectivity/common/inc"
    "${IoTivity_DIR}/resource/csdk/connectivity/inc"
    "${IoTivity_DIR}/resource/csdk/connectivity/inc/pkix"
    "${IoTivity_DIR}/resource/csdk/connectivity/api"
    "${IoTivity_DIR}/resource/csdk/connectivity/external/inc"
    "${IoTivity_DIR}/resource/csdk/security/include"
    "${IoTivity_DIR}/resource/csdk/security/include/internal"
    "${IoTivity_DIR}/resource/csdk/security/provisioning/include"
    "${IoTivity_DIR}/resource/csdk/routing/include"
    "${PROJECT_SOURCE_DIR}/extlibs/libcoap/include/"
    "${PROJECT_SOURCE_DIR}/extlibs/tinycbor/tinycbor/src"
   )


target_link_libraries (octbstack c_common ocsrm connectivity_abstraction mbedtls mbedcrypto routingmanager log)

#include(ExternalProject)
#ExternalProject_Add(libcoap
#  GIT_REPOSITORY    https://github.com/dthaler/libcoap.git
#  GIT_TAG           IoTivity-1.4
#  SOURCE_DIR        "${PROJECT_SOURCE_DIR}/extlibs/libcoap"
#  BINARY_DIR        "${PROJECT_SOURCE_DIR}/extlibs/libcoap"
#  TEST_COMMAND      ""
#)

#add_dependencies(ocstack libcoap)

set(COAP_INCLUDE_DIRS "${PROJECT_SOURCE_DIR}/extlibs/libcoap/include")
include_directories(${COAP_INCLUDE_DIRS})