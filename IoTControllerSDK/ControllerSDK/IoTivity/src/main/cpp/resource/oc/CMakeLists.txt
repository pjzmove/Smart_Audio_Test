 #set(CMAKE_C_FLAGS "-std=c99")
 #set(CMAKE_C_FLAGS "-fPIC")
 #set(CMAKE_CXXF_FLAGS "-std=c++0x")
 #set(CMAKE_CXX_FLAGS_DEBUG "-fno-threadsafe-statics")
 #set(CMAKE_CXX_FLAGS "-fexceptions")
 #set(CMAKE_CXX_FLAGS "-frtti")
 #set(CMAKE_CXX_FLAGS "--no-undefined")


 #add_compile_definitions(TB_LOG)

 add_library(oc_logger SHARED
  "${IoTivity_DIR}/resource/oc_logger/c/oc_logger.c"
  "${IoTivity_DIR}/resource/oc_logger/c/oc_console_logger.c"
  "${IoTivity_DIR}/resource/oc_logger/cpp/oc_ostream_logger.cpp"
 )
 target_link_libraries (oc_logger  c_common)
 SET_TARGET_PROPERTIES(oc_logger PROPERTIES COMPILE_FLAGS  -Wall -std=c99 -fPIC )
 SET_TARGET_PROPERTIES(oc_logger PROPERTIES LINK_FLAGS -fexceptions -frtti -Wl,--no-undefined )

 target_include_directories(oc_logger PUBLIC
  "${PROJECT_SOURCE_DIR}/include"
  ${Boost_DIR}
  "${IoTivity_DIR}/resource/c_common"
  "${IoTivity_DIR}/resource/oc_logger/include"
  "${IoTivity_DIR}/resource/c_common/oic_string/include"
 )

 add_library(oc SHARED
	"${IoTivity_DIR}/resource/src/OCPlatform.cpp"
	"${IoTivity_DIR}/resource/src/OCPlatform_impl.cpp"
	"${IoTivity_DIR}/resource/src/OCResource.cpp"
	"${IoTivity_DIR}/resource/src/OCUtilities.cpp"
	"${IoTivity_DIR}/resource/src/OCException.cpp"
	"${IoTivity_DIR}/resource/src/OCRepresentation.cpp"
	"${IoTivity_DIR}/resource/src/InProcServerWrapper.cpp"
	"${IoTivity_DIR}/resource/src/InProcClientWrapper.cpp"
	"${IoTivity_DIR}/resource/src/OCResourceRequest.cpp"
	"${IoTivity_DIR}/resource/src/CAManager.cpp"
	)
SET_TARGET_PROPERTIES(oc PROPERTIES LINK_FLAGS -fexceptions -frtti -W1,-no-undefined)

target_include_directories(oc PUBLIC
    ${Boost_DIR}
    "${PROJECT_SOURCE_DIR}/include"
    "${IoTivity_DIR}/resource/c_common"
    "${IoTivity_DIR}/resource/c_common/oic_malloc/include"
    "${IoTivity_DIR}/resource/c_common/oic_string/include"
    "${IoTivity_DIR}/resource/include/"
    "${IoTivity_DIR}/resource/csdk/include"
    "${IoTivity_DIR}/resource/csdk/stack/include"
    "${IoTivity_DIR}/resource/csdk/security/include"
    "${IoTivity_DIR}/resource/c_common/ocrandom/include"
    "${IoTivity_DIR}/resource/csdk/logger/include"
    "${IoTivity_DIR}/resource/oc_logger/include"
    "${IoTivity_DIR}/resource/csdk/connectivity/api"
    )

target_link_libraries (oc  octbstack oc_logger log)