/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/

package com.qualcomm.qti.iotcontrollersdk.iotsys.listeners;


import com.qualcomm.qti.iotcontrollersdk.iotsys.resource.attributes.VoiceUIAttr;

public interface VoiceUIListener {

    void OnGetVoiceUICompleted(VoiceUIAttr attribute, boolean status);
    default void OnVoiceUICompleted(boolean status){}
}









