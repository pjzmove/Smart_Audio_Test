/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.VolumeControlAttr;

public interface GroupVolumeControlListener {

    void OnGetGroupVolumeControlCompleted(VolumeControlAttr attribute, boolean status);
    default void OnVolumeControlCompleted(boolean status){}
}









