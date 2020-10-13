/**************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 **************************************************************************************************/


package com.qualcomm.qti.iotcontrollersdk.allplay.listeners;

import com.qualcomm.qti.iotcontrollersdk.allplay.resource.attributes.JoinGroupOutAttr;

public interface JoinGroupListener {
    void OnJoinGroupOutCompleted(JoinGroupOutAttr attribute, boolean status);
}









