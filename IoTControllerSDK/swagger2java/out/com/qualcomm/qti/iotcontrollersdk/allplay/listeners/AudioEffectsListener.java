/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.AudioEffectsAttr;

public interface AudioEffectsListener {

    void OnGetAudioEffectsCompleted(AudioEffectsAttr attribute, boolean status);
}









