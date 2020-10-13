/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.InputSelectorAttr;

public interface InputSelectorListener {

    void OnGetInputSelectorCompleted(InputSelectorAttr attribute, boolean status);
    default void OnInputSelectorCompleted(boolean status){}
}









