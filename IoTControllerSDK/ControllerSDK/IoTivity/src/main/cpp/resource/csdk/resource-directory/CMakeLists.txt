####################################################################################################
# Copyright 2018 Qualcomm Technologies International, Ltd.                                         #
####################################################################################################

add_compile_definitions(RD_CLIENT)

add_library(resource-directory SHARED
	"${IoTivity_DIR}/resource/csdk/resource-directory/src/rd_client.c"
	"${IoTivity_DIR}/resource/csdk/resource-directory/src/RDClient.cpp"
  )

target_include_directories(resource-directory PUBLIC
    "${PROJECT_SOURCE_DIR}/include"
    "${IoTivity_DIR}/resource/c_common"
    "${IoTivity_DIR}/resource/csdk/resource-directory/include"
    "${IoTivity_DIR}/resource/csdk/resource-directory/src/internal"
    "${IoTivity_DIR}/resource/csdk/include"
    "${IoTivity_DIR}/resource/c_common/oic_malloc/include"
    "${IoTivity_DIR}/resource/c_common/oic_string/include"
    "${IoTivity_DIR}/resource/c_common/ocrandom/include"
    "${IoTivity_DIR}/resource/csdk/stack/include"
    "${IoTivity_DIR}/resource/csdk/stack/include/internal"
    "${IoTivity_DIR}/resource/include"
    "${IoTivity_DIR}/resource/csdk/logger/include"
    "${IoTivity_DIR}/resource/oc_logger/include"
    "${IoTivity_DIR}/resource/csdk/connectivity/api"
    )

target_link_libraries (resource-directory octbstack oc oc_logger  connectivity_abstraction)
