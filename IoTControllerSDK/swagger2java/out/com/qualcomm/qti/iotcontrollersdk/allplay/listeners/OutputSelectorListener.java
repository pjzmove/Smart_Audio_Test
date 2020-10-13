/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.OutputSelectorAttr;

public interface OutputSelectorListener {

    void OnGetOutputSelectorCompleted(OutputSelectorAttr attribute, boolean status);
    default void OnOutputSelectorCompleted(boolean status){}
}









