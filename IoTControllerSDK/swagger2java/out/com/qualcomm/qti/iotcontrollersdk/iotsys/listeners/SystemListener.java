/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.iotsys.listeners;


import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.SystemAttr;

public interface SystemListener {

    void OnGetSystemCompleted(SystemAttr attribute, boolean status);
    default void OnSystemCompleted(boolean status){}
}









