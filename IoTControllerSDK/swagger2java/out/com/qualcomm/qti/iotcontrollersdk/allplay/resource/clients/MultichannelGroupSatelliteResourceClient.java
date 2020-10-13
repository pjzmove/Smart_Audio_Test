/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.resource.clients;

import java.util.List;
import java.util.ArrayList;

import org.iotivity.base.OcException;
import org.iotivity.base.OcResource;

import com.qualcomm.qti.iotcontrollersdk.controller.IoTBaseResourceClient;

public class MultichannelGroupSatelliteResourceClient extends IoTBaseResourceClient {

    private MultichannelGroupSatelliteResourceClient() {
    }

    public MultichannelGroupSatelliteResourceClient(String host) throws OcException {
        super(host);
        construct();
    }

    public OcResource construct() throws OcException {
        List<String> interfacesList= new ArrayList<>();
        interfacesList.add("oic.if.baseline");
        interfacesList.add("oic.if.ll");

        List<String> typeList= new ArrayList<>();
        typeList.add("qti.allplay.r.multichannelGroupSatellite");

        return super.construct(mHost, "/qti/allplay/multichannelGroupSatellite",
                        typeList,
                        interfacesList,
                        isObserveSupported());
    }

    @Override
    public boolean isGetSupported() {
        return true;
    }

    @Override
    public boolean isPostSupported() {
        return false;
    }

    @Override
    public boolean isObserveSupported() {
        return true;
    }

}