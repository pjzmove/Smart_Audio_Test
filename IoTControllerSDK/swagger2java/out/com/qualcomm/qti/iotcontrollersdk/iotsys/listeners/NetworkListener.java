/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.iotsys.listeners;


import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.NetworkAttr;

public interface NetworkListener {

    void OnGetNetworkCompleted(NetworkAttr attribute, boolean status);
}









