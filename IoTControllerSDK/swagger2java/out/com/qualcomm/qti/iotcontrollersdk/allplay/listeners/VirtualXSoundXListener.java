/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.VirtualXSoundXAttr;

public interface VirtualXSoundXListener {

    void OnGetVirtualXSoundXCompleted(VirtualXSoundXAttr attribute, boolean status);
    default void OnVirtualXSoundXCompleted(boolean status){}
}









