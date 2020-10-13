/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.DolbyAttr;

public interface DolbyListener {

    void OnGetDolbyCompleted(DolbyAttr attribute, boolean status);
    default void OnDolbyCompleted(boolean status){}
}









