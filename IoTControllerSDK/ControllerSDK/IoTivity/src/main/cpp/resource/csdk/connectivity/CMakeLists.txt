add_compile_definitions(WITH_POSIX)
add_compile_definitions(WITH_TCP)
add_compile_definitions(WITH_MQ)
add_compile_definitions(WITH_BWT)
add_compile_definitions(IP_ADAPTER)
add_compile_definitions(TCP_ADAPTER)
#add_compile_definitions(TB_LOG)
add_compile_definitions(__JAVA__)

add_library(connectivity_abstraction SHARED
    "${IoTivity_DIR}/resource/csdk/connectivity/common/src/uarraylist.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/common/src/ulinklist.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/common/src/uqueue.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/common/src/caremotehandler.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/common/src/cathreadpool_pthreads"
	"${IoTivity_DIR}/resource/csdk/connectivity/src/caconnectivitymanager.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/cainterfacecontroller.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/camessagehandler.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/canetworkconfigurator.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/caprotocolmessage.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/caqueueingthread.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/caretransmission.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/adapter_util/caadapterutils.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/adapter_util/cacertprofile.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/caping.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/cablockwisetransfer.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/ip_adapter/caipadapter.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/ip_adapter/caipserver.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/ip_adapter/android/caipnwmonitor.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/ip_adapter/android/caifaddrs.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/tcp_adapter/catcpadapter.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/src/tcp_adapter/catcpserver.c"
    "${IoTivity_DIR}/resource/csdk/connectivity/util/src/cautilinterface.c"
    )

target_include_directories(connectivity_abstraction PUBLIC
    "${PROJECT_SOURCE_DIR}/include"
    "${IoTivity_DIR}/resource/c_common/octhread/include"
    "${IoTivity_DIR}/resource/csdk/connectivity/common/inc"
    "${IoTivity_DIR}/resource/csdk/connectivity/inc"
    "${IoTivity_DIR}/resource/c_common"
    "${IoTivity_DIR}/resource/csdk/connectivity/common/inc"
    "${IoTivity_DIR}/resource/csdk/connectivity/api"
    "${IoTivity_DIR}/resource/c_common/oic_malloc/include"
    "${IoTivity_DIR}/resource/c_common/oic_string/include"
    "${IoTivity_DIR}/resource/c_common/oic_time/include"
    "${IoTivity_DIR}/resource/c_common/oic_platform/include"
    "${IoTivity_DIR}/resource/c_common/ocrandom/include"
    "${IoTivity_DIR}/resource/c_common/octimer/include"
    "${IoTivity_DIR}/resource/csdk/logger/include"
    "${IoTivity_DIR}/resource/csdk/connectivity/util/inc"
    "${PROJECT_SOURCE_DIR}/extlibs/libcoap/include/"
    "${PROJECT_SOURCE_DIR}/extlibs/mbedtls/mbedtls/include/"
   )

target_link_libraries (connectivity_abstraction c_common coap logger log)