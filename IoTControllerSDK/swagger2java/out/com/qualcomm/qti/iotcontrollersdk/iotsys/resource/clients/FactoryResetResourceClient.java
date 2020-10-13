/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.iotsys.resource.clients;


import java.util.List;
import java.util.ArrayList;

import org.iotivity.base.OcException;
import org.iotivity.base.OcResource;

import com.qualcomm.qti.iotcontrollersdk.controller.IoTBaseResourceClient;

public class FactoryResetResourceClient extends IoTBaseResourceClient {

    private FactoryResetResourceClient() {
    }

    public FactoryResetResourceClient(String host) throws OcException {
        super(host);
        construct();
    }

    public OcResource construct() throws OcException {
        List<String> interfacesList= new ArrayList<>();
        interfacesList.add("oic.if.baseline");

        List<String> typeList= new ArrayList<>();
        typeList.add("qti.iotsys.r.factoryReset");

        return super.construct(mHost, "/qti/iotsys/system/factoryReset",
                        typeList,
                        interfacesList,
                        isObserveSupported());
    }

    @Override
    public boolean isGetSupported() {
        return false;
    }

    @Override
    public boolean isPostSupported() {
        return true;
    }

    @Override
    public boolean isObserveSupported() {
        return false;
    }

}