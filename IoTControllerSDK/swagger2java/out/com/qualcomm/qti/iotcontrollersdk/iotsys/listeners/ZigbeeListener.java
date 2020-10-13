/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.iotsys.listeners;


import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.ZigbeeAttr;

public interface ZigbeeListener {

    void OnGetZigbeeCompleted(ZigbeeAttr attribute, boolean status);
}









