####################################################################################################
# Copyright 2018 Qualcomm Technologies International, Ltd.                                         #
####################################################################################################

target_sources(oc PUBLIC
     "../../../../../iotivity/resource/csdk/stack/src/ocstack.c"
     "../../../../../iotivity/resource/csdk/stack/src/ocpayload.c"
     "../../../../../iotivity/resource/csdk/stack/src/ocpayloadparse.c"
     "../../../../../iotivity/resource/csdk/stack/src/ocpayloadconvert.c"
     "../../../../../iotivity/resource/csdk/stack/src/occlientcb.c"
     "../../../../../iotivity/resource/csdk/stack/src/ocresource.c"
     "../../../../../iotivity/resource/csdk/stack/src/ocobserve.c"
     "../../../../../iotivity/resource/csdk/stack/src/ocserverrequest.c"
     "../../../../../iotivity/resource/csdk/stack/src/occollection.c"
     "../../../../../iotivity/resource/csdk/stack/src/oicgroup.c"
     "../../../../../iotivity/resource/csdk/stack/src/ocendpoint.c"

)

target_include_directories (oc PUBLIC
    "../../../../../iotivity/resource/csdk/stack/include"
    )
