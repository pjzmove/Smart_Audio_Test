/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.iotsys.listeners;


import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.BluetoothAttr;

public interface BluetoothListener {

    void OnGetBluetoothCompleted(BluetoothAttr attribute, boolean status);
}









