/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.resource.clients;

import java.util.List;
import java.util.ArrayList;

import org.iotivity.base.OcException;
import org.iotivity.base.OcResource;

import com.qualcomm.qti.iotcontrollersdk.controller.IoTBaseResourceClient;

public class GroupRemoveFromGroupResourceClient extends IoTBaseResourceClient {

    private GroupRemoveFromGroupResourceClient() {
    }

    public GroupRemoveFromGroupResourceClient(String host) throws OcException {
        super(host);
        construct();
    }

    public OcResource construct() throws OcException {
        List<String> interfacesList= new ArrayList<>();
        interfacesList.add("oic.if.baseline");

        List<String> typeList= new ArrayList<>();
        typeList.add("qti.allplay.r.groupRemoveFromGroup");

        return super.construct(mHost, "/qti/allplay/group/groupRemoveFromGroup",
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