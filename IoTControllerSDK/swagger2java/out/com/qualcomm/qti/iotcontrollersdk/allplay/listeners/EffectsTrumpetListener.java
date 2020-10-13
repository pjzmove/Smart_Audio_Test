/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.EffectsTrumpetAttr;

public interface EffectsTrumpetListener {

    void OnGetEffectsTrumpetCompleted(EffectsTrumpetAttr attribute, boolean status);
    default void OnEffectsTrumpetCompleted(boolean status){}
}









